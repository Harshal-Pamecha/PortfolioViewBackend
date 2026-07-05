package com.portfolio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.entity.Currency;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to manage exchange rates and perform currency conversion.
 */
@Slf4j
@Service
public class CurrencyService {

    private final Map<Currency, BigDecimal> usdRates = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public CurrencyService() {
        // Initialize default hardcoded fallback rates relative to USD
        usdRates.put(Currency.USD, BigDecimal.ONE);
        usdRates.put(Currency.INR, new BigDecimal("95.00"));

        // Attempt background fetch immediately upon startup
        fetchLatestRatesAsync();
    }

    /**
     * Converts an amount from one currency to another using current exchange rates.
     *
     * @param amount The amount to convert
     * @param from   The currency to convert from
     * @param to     The currency to convert to
     * @return The converted amount
     */
    public BigDecimal convert(BigDecimal amount, Currency from, Currency to) {
        if (amount == null) return BigDecimal.ZERO;
        if (from == null || to == null || from == to) return amount;

        BigDecimal fromRate = usdRates.getOrDefault(from, BigDecimal.ONE);
        BigDecimal toRate = usdRates.getOrDefault(to, BigDecimal.ONE);

        // Convert to USD first, then to target currency
        BigDecimal amountInUsd = amount.divide(fromRate, 10, RoundingMode.HALF_UP);
        return amountInUsd.multiply(toRate).setScale(4, RoundingMode.HALF_UP);
    }

    private void fetchLatestRatesAsync() {
        new Thread(this::refreshLatestRates).start();
    }

    /**
     * Refreshes the in-memory exchange rates from the API once every day at midnight.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void refreshLatestRates() {
        log.info("Refreshing live exchange rates from API...");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://open.er-api.com/v6/latest/USD"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode rootNode = objectMapper.readTree(response.body());
                if ("success".equalsIgnoreCase(rootNode.path("result").asText())) {
                    JsonNode ratesNode = rootNode.path("rates");
                    for (Currency curr : Currency.values()) {
                        JsonNode rateNode = ratesNode.path(curr.name());
                        if (!rateNode.isMissingNode() && rateNode.isNumber()) {
                            usdRates.put(curr, BigDecimal.valueOf(rateNode.asDouble()));
                        }
                    }
                    log.info("Successfully refreshed live exchange rates relative to USD");
                }
            }
        } catch (Exception e) {
            log.warn("Failed to refresh live exchange rates: {}", e.getMessage());
        }
    }
}
