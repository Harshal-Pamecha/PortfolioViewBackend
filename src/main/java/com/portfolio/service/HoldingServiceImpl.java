package com.portfolio.service;

import com.portfolio.entity.Holding;
import com.portfolio.entity.User;
import com.portfolio.entity.Transaction;
import com.portfolio.entity.TransactionType;
import com.portfolio.repository.HoldingRepository;
import com.portfolio.repository.UserRepository;
import com.portfolio.repository.TransactionRepository;
import com.portfolio.repository.TickerRepository;
import com.portfolio.security.UserContext;
import com.portfolio.entity.Currency;
import com.portfolio.event.PortfolioChangedEvent;
import org.springframework.context.ApplicationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Implementation of HoldingService for business logic.
 */
@Service
@RequiredArgsConstructor
public class HoldingServiceImpl implements HoldingService {

    private final HoldingRepository repository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final TickerRepository tickerRepository;
    private final CurrencyService currencyService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public List<Holding> getAll() {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User is not authenticated");
        }
        List<Holding> holdings = repository.findByUserIdWithRelations(userId);
        return enrichLogoUrls(holdings);
    }

    @Override
    public Holding getById(final Integer id) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User is not authenticated");
        }
        Holding holding = repository.findByIdAndUserIdWithRelations(id, userId).orElseThrow();
        if (holding.getTickerSymbol() != null && !holding.getTickerSymbol().isEmpty()) {
            tickerRepository.findByTicker(holding.getTickerSymbol())
                .ifPresent(ticker -> {
                    holding.setLogoUrl(ticker.getLogoUrl());
                    holding.setCountryCode(ticker.getCountryCode());
                });
        }
        return holding;
    }

    @Override
    public List<Holding> getByTickerSymbol(final String tickerSymbol) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User is not authenticated");
        }
        List<Holding> holdings = repository.findByUserIdAndTickerSymbolWithRelations(userId, tickerSymbol);
        return enrichLogoUrls(holdings);
    }

    private static String formatNumber(BigDecimal value) {
        if (value == null) return "0";
        return value.stripTrailingZeros().toPlainString();
    }

    private List<Holding> enrichLogoUrls(List<Holding> holdings) {
        List<String> symbols = holdings.stream()
            .map(Holding::getTickerSymbol)
            .filter(s -> s != null && !s.trim().isEmpty())
            .distinct()
            .toList();

        if (symbols.isEmpty()) return holdings;

        java.util.Map<String, com.portfolio.entity.Ticker> tickerMap = tickerRepository.findByTickerIn(symbols)
            .stream()
            .collect(java.util.stream.Collectors.toMap(com.portfolio.entity.Ticker::getTicker, t -> t));

        for (Holding h : holdings) {
            com.portfolio.entity.Ticker ticker = tickerMap.get(h.getTickerSymbol());
            if (ticker != null) {
                h.setLogoUrl(ticker.getLogoUrl());
                h.setCountryCode(ticker.getCountryCode());
            }
        }
        return holdings;
    }

    @Override
    @Transactional
    public Holding create(final Holding holding) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User is not authenticated");
        }
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new SecurityException("User not found"));
        holding.setUser(user);
        
        if (holding.getQuantity() == null || holding.getAvgBuyPrice() == null || holding.getAccount() == null) {
            throw new IllegalArgumentException("Quantity, average buy price, and account must be provided.");
        }
        if (holding.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Holding quantity must be strictly positive.");
        }
        if (holding.getAvgBuyPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Holding average buy price cannot be negative.");
        }

        if (holding.getTickerSymbol() != null && !holding.getTickerSymbol().isEmpty()) {
            tickerRepository.findByTicker(holding.getTickerSymbol())
                .ifPresent(ticker -> {
                    if (ticker.getCompanyName() != null && !ticker.getCompanyName().isEmpty()) {
                        holding.setName(ticker.getCompanyName());
                    }
                });
        }
        
        Holding savedHolding = repository.save(holding);
        
        Currency holdingCurrency = Currency.INR;
        if ("US".equals(savedHolding.getCountryCode())) {
            holdingCurrency = Currency.USD;
        }

        Transaction tx = new Transaction();
        tx.setUser(user);
        tx.setDate(LocalDateTime.now());
        tx.setType(TransactionType.OUTFLOW);
        tx.setCurrency(holdingCurrency);
        tx.setAmount(holding.getQuantity().multiply(holding.getAvgBuyPrice()));
        tx.setSourceAccount(holding.getAccount());
        tx.setHolding(savedHolding);
        
        String assetName = holding.getName() != null && !holding.getName().isEmpty() ? holding.getName() : holding.getTickerSymbol();
        tx.setNotes("Auto-generated: Purchase of " + formatNumber(holding.getQuantity()) + " units of " + assetName);
        
        eventPublisher.publishEvent(new PortfolioChangedEvent(userId, tx));
        
        return savedHolding;
    }

    @Override
    @Transactional
    public Holding update(final Integer id, Holding holding) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User is not authenticated");
        }
        
        Holding existing = repository.findByIdAndUserId(id, userId).orElseThrow();
        
        if (holding.getTickerSymbol() != null && !holding.getTickerSymbol().isEmpty()) {
            tickerRepository.findByTicker(holding.getTickerSymbol())
                .ifPresent(ticker -> {
                    if (ticker.getCompanyName() != null && !ticker.getCompanyName().isEmpty()) {
                        existing.setName(ticker.getCompanyName());
                    }
                });
            existing.setTickerSymbol(holding.getTickerSymbol());
        }
        
        if (holding.getQuantity() != null) existing.setQuantity(holding.getQuantity());
        if (holding.getAvgBuyPrice() != null) existing.setAvgBuyPrice(holding.getAvgBuyPrice());
        if (holding.getPurchaseDate() != null) existing.setPurchaseDate(holding.getPurchaseDate());
        if (holding.getNotes() != null) existing.setNotes(holding.getNotes());

        Holding savedHolding = repository.save(existing);
        
        eventPublisher.publishEvent(new PortfolioChangedEvent(userId));
        
        return savedHolding;
    }

    @Override
    @Transactional
    public void delete(final Integer id) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User is not authenticated");
        }
        Holding existing = repository.findByIdAndUserId(id, userId).orElseThrow();
        
        List<Transaction> txs = transactionRepository.findByHoldingId(id);
        for (Transaction tx : txs) {
            tx.setHolding(null);
            transactionRepository.save(tx);
        }
        transactionRepository.flush();
        
        Transaction refundTx = null;
        if (existing.getQuantity() != null && existing.getAvgBuyPrice() != null && existing.getAccount() != null) {
            BigDecimal refundAmount = existing.getQuantity().multiply(existing.getAvgBuyPrice());
            if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                Currency holdingCurrency = Currency.INR;
                if ("US".equals(existing.getCountryCode())) {
                    holdingCurrency = Currency.USD;
                }
                Currency accountCurrency = existing.getAccount().getCurrency() != null ? existing.getAccount().getCurrency() : (existing.getAccount().getFamilyMember() != null && existing.getAccount().getFamilyMember().getCurrency() != null ? existing.getAccount().getFamilyMember().getCurrency() : Currency.INR);
                BigDecimal localRefundAmount = currencyService.convert(refundAmount, holdingCurrency, accountCurrency);

                refundTx = new Transaction();
                refundTx.setUser(existing.getUser());
                refundTx.setDate(LocalDateTime.now());
                refundTx.setType(TransactionType.INFLOW);
                refundTx.setCurrency(holdingCurrency);
                refundTx.setAmount(refundAmount);
                refundTx.setDestAccount(existing.getAccount());
                String assetName = existing.getName() != null && !existing.getName().isEmpty() ? existing.getName() : existing.getTickerSymbol();
                refundTx.setNotes("Holding Deleted (Reversal): " + formatNumber(existing.getQuantity()) + " units of " + assetName + " (" + formatNumber(localRefundAmount) + " " + accountCurrency.name() + " local equivalent)");
            }
        }
        
        repository.delete(existing);
        
        if (refundTx != null) {
            eventPublisher.publishEvent(new PortfolioChangedEvent(userId, refundTx));
        } else {
            eventPublisher.publishEvent(new PortfolioChangedEvent(userId));
        }
    }

    @Override
    @Transactional
    public void deleteByTickerSymbol(final String tickerSymbol) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User is not authenticated");
        }
        List<Holding> holdings = repository.findByUserIdAndTickerSymbol(userId, tickerSymbol);
        List<Transaction> refundTxs = new ArrayList<>();
        
        for (Holding holding : holdings) {
            List<Transaction> txs = transactionRepository.findByHoldingId(holding.getId());
            for (Transaction tx : txs) {
                tx.setHolding(null);
                transactionRepository.save(tx);
            }
            transactionRepository.flush();
            
            if (holding.getQuantity() != null && holding.getAvgBuyPrice() != null && holding.getAccount() != null) {
                BigDecimal refundAmount = holding.getQuantity().multiply(holding.getAvgBuyPrice());
                if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                    Currency holdingCurrency = Currency.INR;
                    if ("US".equals(holding.getCountryCode())) {
                        holdingCurrency = Currency.USD;
                    }
                    Currency accountCurrency = holding.getAccount().getCurrency() != null ? holding.getAccount().getCurrency() : (holding.getAccount().getFamilyMember() != null && holding.getAccount().getFamilyMember().getCurrency() != null ? holding.getAccount().getFamilyMember().getCurrency() : Currency.INR);
                    BigDecimal localRefundAmount = currencyService.convert(refundAmount, holdingCurrency, accountCurrency);

                    Transaction refundTx = new Transaction();
                    refundTx.setUser(holding.getUser());
                    refundTx.setDate(LocalDateTime.now());
                    refundTx.setType(TransactionType.INFLOW);
                    refundTx.setCurrency(holdingCurrency);
                    refundTx.setAmount(refundAmount);
                    refundTx.setDestAccount(holding.getAccount());
                    String assetName = holding.getName() != null && !holding.getName().isEmpty() ? holding.getName() : holding.getTickerSymbol();
                    refundTx.setNotes("Holding Deleted (Reversal): " + formatNumber(holding.getQuantity()) + " units of " + assetName + " (" + formatNumber(localRefundAmount) + " " + accountCurrency.name() + " local equivalent)");
                    refundTxs.add(refundTx);
                }
            }
        }
        repository.deleteByUserIdAndTickerSymbol(userId, tickerSymbol);
        
        eventPublisher.publishEvent(new PortfolioChangedEvent(userId, refundTxs));
    }

    @Override
    @Transactional
    public Transaction sell(final Integer id, final BigDecimal quantitySold, final BigDecimal sellPrice) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User is not authenticated");
        }
        Holding target = repository.findByIdAndUserId(id, userId).orElseThrow();
        
        if (quantitySold.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity sold must be greater than zero");
        }
        if (sellPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Sell price cannot be negative");
        }
        
        List<Holding> fifoHoldings;
        if (target.getTickerSymbol() != null && !target.getTickerSymbol().trim().isEmpty()) {
            fifoHoldings = repository.findByAccountIdAndTickerSymbolOrderByPurchaseDateAsc(target.getAccount().getId(), target.getTickerSymbol());
        } else {
            fifoHoldings = repository.findByAccountIdAndNameOrderByPurchaseDateAsc(target.getAccount().getId(), target.getName());
        }
        
        BigDecimal totalAvailable = fifoHoldings.stream()
                .map(Holding::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        if (totalAvailable.compareTo(quantitySold) < 0) {
            throw new IllegalArgumentException("Cannot sell more units than currently held across all lots (" + totalAvailable.toPlainString() + " available).");
        }
        
        BigDecimal remainingToSell = quantitySold;
        BigDecimal totalCostBasis = BigDecimal.ZERO;
        
        for (Holding lot : fifoHoldings) {
            if (remainingToSell.compareTo(BigDecimal.ZERO) == 0) break;
            
            if (lot.getQuantity().compareTo(BigDecimal.ZERO) <= 0) continue;
            
            BigDecimal qtyFromThisLot;
            if (lot.getQuantity().compareTo(remainingToSell) <= 0) {
                qtyFromThisLot = lot.getQuantity(); 
            } else {
                qtyFromThisLot = remainingToSell; 
            }
            
            totalCostBasis = totalCostBasis.add(qtyFromThisLot.multiply(lot.getAvgBuyPrice()));
            lot.setQuantity(lot.getQuantity().subtract(qtyFromThisLot));
            remainingToSell = remainingToSell.subtract(qtyFromThisLot);
            
            repository.save(lot);
        }
        
        BigDecimal proceeds = quantitySold.multiply(sellPrice);
        BigDecimal realizedPnl = proceeds.subtract(totalCostBasis);
        
        Currency holdingCurrency = Currency.INR;
        if ("US".equals(target.getCountryCode())) {
            holdingCurrency = Currency.USD;
        }
        Currency accountCurrency = target.getAccount().getCurrency() != null ? target.getAccount().getCurrency() : (target.getAccount().getFamilyMember() != null && target.getAccount().getFamilyMember().getCurrency() != null ? target.getAccount().getFamilyMember().getCurrency() : Currency.INR);
        
        BigDecimal localProceeds = currencyService.convert(proceeds, holdingCurrency, accountCurrency);
        BigDecimal localRealizedPnl = currencyService.convert(realizedPnl, holdingCurrency, accountCurrency);

        Transaction tx = new Transaction();
        tx.setUser(target.getUser());
        tx.setDate(LocalDateTime.now());
        tx.setType(TransactionType.INFLOW);
        tx.setCurrency(holdingCurrency);
        tx.setAmount(proceeds);
        tx.setDestAccount(target.getAccount()); 
        tx.setRealizedPnl(localRealizedPnl);
        
        String assetName = target.getName() != null && !target.getName().isEmpty() ? target.getName() : target.getTickerSymbol();
        tx.setNotes("Sold " + formatNumber(quantitySold) + " units of " + assetName + ". Realized P&L: " + formatNumber(realizedPnl) + " " + holdingCurrency.name());
        
        eventPublisher.publishEvent(new PortfolioChangedEvent(userId, tx));
        
        return tx;
    }
}
