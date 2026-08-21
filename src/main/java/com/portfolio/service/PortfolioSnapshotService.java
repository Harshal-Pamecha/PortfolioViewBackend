package com.portfolio.service;

import com.portfolio.entity.PortfolioSnapshot;
import com.portfolio.entity.User;
import com.portfolio.entity.Holding;
import com.portfolio.entity.HoldingCategory;
import com.portfolio.entity.Currency;
import com.portfolio.entity.Ticker;
import com.portfolio.repository.PortfolioSnapshotRepository;
import com.portfolio.repository.UserRepository;
import com.portfolio.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

/**
 * Service to calculate daily portfolio snapshots and manage historical seeder.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioSnapshotService {

    private final PortfolioSnapshotRepository snapshotRepository;
    private final UserRepository userRepository;
    private final HoldingRepository holdingRepository;
    private final TickerService tickerService;
    private final CurrencyService currencyService;

    @Autowired
    @Lazy
    private PortfolioSnapshotService self;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private java.util.concurrent.CompletableFuture<java.util.NavigableMap<LocalDate, BigDecimal>> fetchTickerHistoryAsync(String ticker, LocalDate startDate, LocalDate endDate) {
        long period1 = startDate.atStartOfDay(java.time.ZoneOffset.UTC).toEpochSecond();
        long period2 = endDate.plusDays(1).atStartOfDay(java.time.ZoneOffset.UTC).toEpochSecond();
        
        String url = String.format("https://query1.finance.yahoo.com/v8/finance/chart/%s?interval=1d&period1=%d&period2=%d",
            java.net.URLEncoder.encode(ticker, java.nio.charset.StandardCharsets.UTF_8), period1, period2);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    java.util.NavigableMap<LocalDate, BigDecimal> priceMap = new java.util.TreeMap<>();
                    if (response.statusCode() == 200) {
                        try {
                            JsonNode root = MAPPER.readTree(response.body());
                            JsonNode result = root.path("chart").path("result").get(0);
                            if (result != null) {
                                JsonNode timestampsNode = result.path("timestamp");
                                JsonNode closesNode = result.path("indicators").path("quote").get(0).path("close");
                                
                                if (timestampsNode.isArray() && closesNode.isArray()) {
                                    for (int i = 0; i < timestampsNode.size(); i++) {
                                        long timestamp = timestampsNode.get(i).asLong();
                                        JsonNode closeVal = closesNode.get(i);
                                        if (closeVal != null && !closeVal.isNull() && closeVal.isNumber()) {
                                            LocalDate date = java.time.Instant.ofEpochSecond(timestamp)
                                                .atZone(java.time.ZoneOffset.UTC)
                                                .toLocalDate();
                                            priceMap.put(date, BigDecimal.valueOf(closeVal.asDouble()));
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.error("Failed to parse history for ticker {}: {}", ticker, e.getMessage());
                        }
                    } else {
                        log.warn("Yahoo Finance returned status {} for ticker {}", response.statusCode(), ticker);
                    }
                    return priceMap;
                })
                .exceptionally(e -> {
                    log.error("Failed to fetch history for ticker {}: {}", ticker, e.getMessage());
                    return new java.util.TreeMap<>();
                });
    }

    private BigDecimal getPriceOnOrBefore(java.util.NavigableMap<LocalDate, BigDecimal> priceMap, LocalDate date, BigDecimal fallback) {
        java.util.Map.Entry<LocalDate, BigDecimal> entry = priceMap.floorEntry(date);
        if (entry != null && !date.minusDays(10).isAfter(entry.getKey())) {
            return entry.getValue();
        }
        return fallback;
    }



    /**
     * Calculates Indian and US equity values separately.
     * Index 0: Indian equities value (in USD)
     * Index 1: US equities value (in USD)
     */
    public BigDecimal[] calculateCurrentPortfolioValues(User user) {
        BigDecimal inTotal = BigDecimal.ZERO;
        BigDecimal usTotal = BigDecimal.ZERO;
        Currency base = Currency.USD; // Always compute snapshots in USD

        List<Holding> holdings = holdingRepository.findByUserIdWithRelations(user.getId());
        for (Holding h : holdings) {
            if (h.getCategory() != HoldingCategory.EQUITY) {
                continue;
            }
            BigDecimal qty = h.getQuantity() != null ? h.getQuantity() : BigDecimal.ZERO;
            BigDecimal price = h.getAvgBuyPrice() != null ? h.getAvgBuyPrice() : BigDecimal.ZERO;
            BigDecimal value = qty.multiply(price);
            
            // Determine holding currency
            Currency hCurr = Currency.INR;
            if (h.getAccount() != null) {
                if (h.getAccount().getCurrency() != null) {
                    hCurr = h.getAccount().getCurrency();
                } else if (h.getAccount().getFamilyMember() != null && h.getAccount().getFamilyMember().getCurrency() != null) {
                    hCurr = h.getAccount().getFamilyMember().getCurrency();
                }
            }
            
            // Resolve countryCode via Ticker table
            String country = "IN";
            if (h.getTickerSymbol() != null && !h.getTickerSymbol().trim().isEmpty()) {
                country = tickerService.findByTicker(h.getTickerSymbol())
                    .map(Ticker::getCountryCode)
                    .orElse("IN");
            }
            if (country.equalsIgnoreCase("US")) {
                hCurr = Currency.USD;
                usTotal = usTotal.add(currencyService.convert(value, hCurr, base));
            } else {
                inTotal = inTotal.add(currencyService.convert(value, hCurr, base));
            }
        }

        return new BigDecimal[]{inTotal, usTotal};
    }

    /**
     * Nightly scheduled task at 11:59 PM to capture snapshots for all users.
     */
    @SuppressWarnings("unused")
    @Scheduled(cron = "0 59 23 * * ?")
    public void captureAllSnapshots() {
        log.info("Starting daily portfolio snapshot capture...");
        LocalDate today = LocalDate.now();
        
        int page = 0;
        int size = 100;
        org.springframework.data.domain.Page<User> userPage;
        
        do {
            userPage = userRepository.findAll(org.springframework.data.domain.PageRequest.of(page, size));
            for (User user : userPage.getContent()) {
                try {
                    self.processUserSnapshot(user, today);
                } catch (Exception e) {
                    log.error("Failed to capture snapshot for user {}: {}", user.getId(), e.getMessage());
                }
            }
            page++;
        } while (userPage.hasNext());
    }

    @Transactional
    public void processUserSnapshot(User user, LocalDate date) {
        BigDecimal[] values = calculateCurrentPortfolioValues(user);
        saveSnapshot(user, date, values[0], values[1]);
    }

    private void saveSnapshot(User user, LocalDate date, BigDecimal inValue, BigDecimal usValue) {
        try {
            PortfolioSnapshot snapshot = snapshotRepository.findByUserIdAndDate(user.getId(), date)
                .orElse(new PortfolioSnapshot());
            snapshot.setUser(user);
            snapshot.setDate(date);
            snapshot.setInEquityValue(inValue);
            snapshot.setUsEquityValue(usValue);
            snapshot.setEquityValue(inValue.add(usValue));
            snapshotRepository.saveAndFlush(snapshot);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.warn("Snapshot already exists for user {} on date {}, skipping to prevent race condition.", user.getId(), date);
        }
    }

    private boolean shouldReSeed(List<PortfolioSnapshot> history, List<Holding> equities) {
        if (history.isEmpty()) {
            return true;
        }
        if (equities.isEmpty()) {
            return false;
        }

        // Check if all existing snapshots are zero
        boolean allZero = true;
        for (PortfolioSnapshot s : history) {
            BigDecimal val = s.getEquityValue() != null ? s.getEquityValue() : BigDecimal.ZERO;
            if (val.compareTo(BigDecimal.ZERO) > 0) {
                allZero = false;
                break;
            }
        }
        if (allZero) {
            return true;
        }

        // Check if any existing snapshot has null values for regional columns
        for (PortfolioSnapshot s : history) {
            if (s.getInEquityValue() == null || s.getUsEquityValue() == null) {
                return true;
            }
        }

        // Check if the earliest purchase date of active holdings is before the oldest snapshot date
        LocalDate earliestPurchase = equities.stream()
            .map(Holding::getPurchaseDate)
            .min(LocalDate::compareTo)
            .orElse(LocalDate.now());

        LocalDate oldestSnapshotDate = history.get(0).getDate();
        if (earliestPurchase.isBefore(oldestSnapshotDate)) {
            return true;
        }

        // Check if the latest snapshot is older than yesterday (e.g. cron job missed a day)
        LocalDate latestSnapshotDate = history.get(history.size() - 1).getDate();
        return latestSnapshotDate.isBefore(LocalDate.now().minusDays(1));
    }

    /**
     * Seeds daily historical snapshots for a user going back N days if history is empty.
     *
     * @param user the user whose history is loaded/seeded
     * @param daysBack number of days to retrieve
     * @return list of snapshots
     */
    @Transactional
    public List<PortfolioSnapshot> getOrSeedHistory(User user, int daysBack) {
        List<PortfolioSnapshot> history = snapshotRepository.findByUserIdAndDateGreaterThanEqualOrderByDateAsc(
            user.getId(), LocalDate.now().minusDays(daysBack)
        );

        List<Holding> holdings = holdingRepository.findByUserIdWithRelations(user.getId());
        List<Holding> equities = holdings.stream()
            .filter(h -> h.getCategory() == HoldingCategory.EQUITY && h.getPurchaseDate() != null)
            .toList();

        if (shouldReSeed(history, equities)) {
            log.info("Triggering re-seeding of portfolio snapshots for user {}", user.getId());
            snapshotRepository.deleteByUserId(user.getId());
            snapshotRepository.flush(); // Flush deletion immediately

            LocalDate today = LocalDate.now();

            if (equities.isEmpty()) {
                log.info("No equity holdings with valid purchase dates found. Seeding a fallback baseline for last {} days...", daysBack);
                BigDecimal[] currentVals = calculateCurrentPortfolioValues(user);
                for (int i = 0; i <= daysBack; i++) {
                    saveSnapshot(user, today.minusDays(i), currentVals[0], currentVals[1]);
                }
            } else {
                // Find the earliest purchase date
                LocalDate earliestPurchase = equities.stream()
                    .map(Holding::getPurchaseDate)
                    .min(LocalDate::compareTo)
                    .orElse(today);

                // Cap seeding to 3 years ago max to keep it performant and avoid API rate limits
                LocalDate limitDate = today.minusYears(3);
                LocalDate seedStartDate = earliestPurchase.isBefore(limitDate) ? limitDate : earliestPurchase;

                log.info("Earliest purchase date found: {}. Seeding history from: {} to today...", earliestPurchase, seedStartDate);

                // Fetch historical prices and country codes for each unique ticker symbol concurrently
                java.util.Map<String, java.util.NavigableMap<LocalDate, BigDecimal>> tickerHistories = new java.util.concurrent.ConcurrentHashMap<>();
                java.util.Map<String, String> tickerCountryCodes = new java.util.concurrent.ConcurrentHashMap<>();
                
                List<java.util.concurrent.CompletableFuture<Void>> futures = equities.stream()
                    .map(Holding::getTickerSymbol)
                    .filter(ticker -> ticker != null && !ticker.trim().isEmpty())
                    .distinct()
                    .map(ticker -> fetchTickerHistoryAsync(ticker, seedStartDate, today)
                        .thenAccept(priceMap -> {
                            tickerHistories.put(ticker, priceMap);
                            tickerCountryCodes.put(ticker, tickerService.findByTicker(ticker)
                                    .map(Ticker::getCountryCode)
                                    .orElse("IN"));
                        }))
                    .toList();
                
                java.util.concurrent.CompletableFuture.allOf(futures.toArray(new java.util.concurrent.CompletableFuture[0])).join();

                // Walk forward from seedStartDate to today day-by-day and calculate historical snapshot
                LocalDate current = seedStartDate;
                List<PortfolioSnapshot> batch = new java.util.ArrayList<>();
                while (!current.isAfter(today)) {
                    BigDecimal dailyInTotal = BigDecimal.ZERO;
                    BigDecimal dailyUsTotal = BigDecimal.ZERO;

                    for (Holding h : equities) {
                        // Check if this holding was bought on or before the current iteration date
                        if (!h.getPurchaseDate().isAfter(current)) {
                            BigDecimal qty = h.getQuantity() != null ? h.getQuantity() : BigDecimal.ZERO;
                            BigDecimal price = h.getAvgBuyPrice() != null ? h.getAvgBuyPrice() : BigDecimal.ZERO;
                            
                            // Try to get historical price for that day if ticker is available
                            String ticker = h.getTickerSymbol();
                            if (ticker != null && !ticker.trim().isEmpty() && tickerHistories.containsKey(ticker)) {
                                price = getPriceOnOrBefore(tickerHistories.get(ticker), current, price);
                            }

                            BigDecimal value = qty.multiply(price);

                            // Determine holding currency
                            Currency hCurr = Currency.INR;
                            if (h.getAccount() != null) {
                                if (h.getAccount().getCurrency() != null) {
                                    hCurr = h.getAccount().getCurrency();
                                } else if (h.getAccount().getFamilyMember() != null && h.getAccount().getFamilyMember().getCurrency() != null) {
                                    hCurr = h.getAccount().getFamilyMember().getCurrency();
                                }
                            }
                            
                            // Resolve countryCode via pre-fetched Ticker table
                            String country = "IN";
                            if (ticker != null && !ticker.trim().isEmpty() && tickerCountryCodes.containsKey(ticker)) {
                                country = tickerCountryCodes.get(ticker);
                            }
                            if (country.equalsIgnoreCase("US")) {
                                hCurr = Currency.USD;
                                dailyUsTotal = dailyUsTotal.add(currencyService.convert(value, hCurr, Currency.USD));
                            } else {
                                dailyInTotal = dailyInTotal.add(currencyService.convert(value, hCurr, Currency.USD));
                            }
                        }
                    }

                    PortfolioSnapshot snapshot = new PortfolioSnapshot();
                    snapshot.setUser(user);
                    snapshot.setDate(current);
                    snapshot.setInEquityValue(dailyInTotal);
                    snapshot.setUsEquityValue(dailyUsTotal);
                    snapshot.setEquityValue(dailyInTotal.add(dailyUsTotal));
                    batch.add(snapshot);

                    if (batch.size() >= 50) {
                        snapshotRepository.saveAll(batch);
                        snapshotRepository.flush();
                        batch.clear();
                    }

                    current = current.plusDays(1);
                }
                if (!batch.isEmpty()) {
                    snapshotRepository.saveAll(batch);
                    snapshotRepository.flush();
                }
            }

            // Retrieve again after seeding
            history = snapshotRepository.findByUserIdAndDateGreaterThanEqualOrderByDateAsc(
                user.getId(), LocalDate.now().minusDays(daysBack)
            );
        }
        return history;
    }
}
