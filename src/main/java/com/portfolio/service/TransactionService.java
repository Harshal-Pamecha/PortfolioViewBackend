package com.portfolio.service;

import com.portfolio.entity.Account;
import com.portfolio.entity.FamilyMember;
import com.portfolio.entity.Transaction;
import com.portfolio.entity.Currency;

import com.portfolio.entity.AccountType;
import com.portfolio.repository.AccountRepository;
import com.portfolio.repository.FamilyMemberRepository;
import com.portfolio.repository.TransactionRepository;
import com.portfolio.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.portfolio.entity.User;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service layer for Transaction entity business logic.
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository repository;
    private final AccountRepository accountRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final CurrencyService currencyService;

    /** Retrieves paginated transactions. */
    public org.springframework.data.domain.Page<Transaction> getAll(org.springframework.data.domain.Pageable pageable) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User is not authenticated");
        }
        return repository.findByUserIdWithRelations(userId, pageable);
    }

    /** Retrieves a transaction by ID. */
    public Transaction getById(final Integer id) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User is not authenticated");
        }
        return repository.findByIdAndUserIdWithRelations(id, userId).orElseThrow();
    }

    /** Creates a new transaction. */
    @Transactional
    public Transaction create(final Transaction transaction) {
        Integer userId = (transaction.getUser() != null && transaction.getUser().getId() != null) 
            ? transaction.getUser().getId() 
            : UserContext.getCurrentUserId();
            
        if (userId == null) {
            throw new SecurityException("User is not authenticated");
        }
        
        if (transaction.getUser() == null || transaction.getUser().getId() == null) {
            User user = new User();
            user.setId(userId);
            transaction.setUser(user);
        }

        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be strictly positive.");
        }
        Transaction savedTransaction = repository.save(transaction);
        updateAccountBalances(savedTransaction, false);
        return savedTransaction;
    }

    /** Updates an existing transaction. */
    @Transactional
    public Transaction update(final Integer id, Transaction transaction) {
        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be strictly positive.");
        }
        Transaction existing = getById(id);
        updateAccountBalances(existing, true); // Reverse old transaction

        existing.setAmount(transaction.getAmount());
        existing.setType(transaction.getType());
        existing.setDate(transaction.getDate());
        existing.setNotes(transaction.getNotes());
        existing.setSourceAccount(transaction.getSourceAccount());
        existing.setDestAccount(transaction.getDestAccount());
        existing.setHolding(transaction.getHolding());

        Transaction savedTransaction = repository.save(existing);
        updateAccountBalances(savedTransaction, false); // Apply new transaction

        return savedTransaction;
    }

    /** Deletes a transaction by ID. */
    @Transactional
    public void delete(final Integer id) {
        Transaction existing = getById(id);
        delete(existing);
    }

    /** Deletes a transaction using an already fetched entity. */
    @Transactional
    public void delete(final Transaction existing) {
        updateAccountBalances(existing, true); // Reverse before deleting
        repository.deleteById(existing.getId());
    }

    /**
     * Updates the balance of the Account associated with the transaction.
     *
     * @param transaction The transaction to process.
     * @param isReversal If true, the operation is reversed (e.g. for update or delete).
     */
    private void updateAccountBalances(Transaction transaction, boolean isReversal) {
        BigDecimal amount = transaction.getAmount();
        if (amount == null) return;

        if (isReversal) {
            amount = amount.negate();
        }

        Account sourceAccount = null;
        if (transaction.getSourceAccount() != null && transaction.getSourceAccount().getId() != null) {
            sourceAccount = accountRepository.findByIdAndUserId(transaction.getSourceAccount().getId(), transaction.getUser().getId()).orElse(null);
        }
        
        Account destAccount = null;
        if (transaction.getDestAccount() != null && transaction.getDestAccount().getId() != null) {
            destAccount = accountRepository.findByIdAndUserId(transaction.getDestAccount().getId(), transaction.getUser().getId()).orElse(null);
        }

        // Determine primary transaction currency
        Currency txCurrency = getTransactionCurrency(transaction, sourceAccount, destAccount);

        // If money is coming from a source account, subtract it
        if (sourceAccount != null) {
            Currency accountCurrency = sourceAccount.getCurrency() != null ? sourceAccount.getCurrency() : (sourceAccount.getFamilyMember() != null && sourceAccount.getFamilyMember().getCurrency() != null ? sourceAccount.getFamilyMember().getCurrency() : Currency.INR);
            BigDecimal localAmount = currencyService.convert(amount, txCurrency, accountCurrency);

            if (sourceAccount.getType() == AccountType.BANK || sourceAccount.getType() == AccountType.WALLET) {
                BigDecimal currentBalance = sourceAccount.getBalance() != null ? sourceAccount.getBalance() : BigDecimal.ZERO;
                sourceAccount.setBalance(currentBalance.subtract(localAmount));
                accountRepository.save(sourceAccount);
            } else if (sourceAccount.getFamilyMember() != null) {
                List<Account> cashAccounts = accountRepository.findByFamilyMemberIdAndUserIdAndTypeIn(sourceAccount.getFamilyMember().getId(), transaction.getUser().getId(), List.of(AccountType.BANK, AccountType.WALLET));
                if (!cashAccounts.isEmpty()) {
                    Account targetAccount = cashAccounts.stream()
                            .filter(acc -> "Cash".equalsIgnoreCase(acc.getName()))
                            .findFirst()
                            .orElse(cashAccounts.get(0));
                    BigDecimal currentBalance = targetAccount.getBalance() != null ? targetAccount.getBalance() : BigDecimal.ZERO;
                    targetAccount.setBalance(currentBalance.subtract(localAmount));
                    accountRepository.save(targetAccount);
                } else {
                    // Fallback: update family member's stored database balance
                    FamilyMember member = sourceAccount.getFamilyMember();
                    if (member != null) {
                        BigDecimal currentBalance = member.getBalance() != null ? member.getBalance() : BigDecimal.ZERO;
                        member.setBalance(currentBalance.subtract(localAmount));
                        familyMemberRepository.save(member);
                    }
                }
            }
        }

        // If money is going into a destination account, add it
        if (destAccount != null) {
            Currency accountCurrency = destAccount.getCurrency() != null ? destAccount.getCurrency() : (destAccount.getFamilyMember() != null && destAccount.getFamilyMember().getCurrency() != null ? destAccount.getFamilyMember().getCurrency() : Currency.INR);
            BigDecimal localAmount = currencyService.convert(amount, txCurrency, accountCurrency);

            if (destAccount.getType() == AccountType.BANK || destAccount.getType() == AccountType.WALLET) {
                BigDecimal currentBalance = destAccount.getBalance() != null ? destAccount.getBalance() : BigDecimal.ZERO;
                destAccount.setBalance(currentBalance.add(localAmount));
                accountRepository.save(destAccount);
            } else if (destAccount.getFamilyMember() != null) {
                List<Account> cashAccounts = accountRepository.findByFamilyMemberIdAndUserIdAndTypeIn(destAccount.getFamilyMember().getId(), transaction.getUser().getId(), List.of(AccountType.BANK, AccountType.WALLET));
                if (!cashAccounts.isEmpty()) {
                    Account targetAccount = cashAccounts.stream()
                            .filter(acc -> "Cash".equalsIgnoreCase(acc.getName()))
                            .findFirst()
                            .orElse(cashAccounts.get(0));
                    BigDecimal currentBalance = targetAccount.getBalance() != null ? targetAccount.getBalance() : BigDecimal.ZERO;
                    targetAccount.setBalance(currentBalance.add(localAmount));
                    accountRepository.save(targetAccount);
                } else {
                    // Fallback: update family member's stored database balance
                    FamilyMember member = destAccount.getFamilyMember();
                    if (member != null) {
                        BigDecimal currentBalance = member.getBalance() != null ? member.getBalance() : BigDecimal.ZERO;
                        member.setBalance(currentBalance.add(localAmount));
                        familyMemberRepository.save(member);
                    }
                }
            }
        }
    }



    private Currency getTransactionCurrency(Transaction transaction, Account source, Account dest) {
        if (transaction.getCurrency() != null) {
            return transaction.getCurrency();
        }

        if (transaction.getHolding() != null && transaction.getHolding().getCountryCode() != null) {
            String cc = transaction.getHolding().getCountryCode();
            switch (cc) {
                case "US" -> {
                    return Currency.USD;
                }
                case "IN" -> {
                    return Currency.INR;
                }
            }
        }

        if (source != null) {
            if (source.getCurrency() != null) return source.getCurrency();
            if (source.getFamilyMember() != null && source.getFamilyMember().getCurrency() != null) return source.getFamilyMember().getCurrency();
        }
        if (dest != null) {
            if (dest.getCurrency() != null) return dest.getCurrency();
            if (dest.getFamilyMember() != null && dest.getFamilyMember().getCurrency() != null) return dest.getFamilyMember().getCurrency();
        }
        if (transaction.getUser() != null && transaction.getUser().getBaseCurrency() != null) {
            return transaction.getUser().getBaseCurrency();
        }
        return Currency.INR; // fallback
    }
}
