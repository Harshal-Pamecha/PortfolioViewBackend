package com.portfolio.dto;

import com.portfolio.entity.Holding;
import com.portfolio.entity.HoldingCategory;
import java.math.BigDecimal;
import java.time.LocalDate;

public record HoldingDto(
    Integer id,
    String name,
    HoldingCategory category,
    BigDecimal quantity,
    BigDecimal units, // alias of quantity
    BigDecimal avgBuyPrice,
    BigDecimal averagePrice, // alias of avgBuyPrice
    String logoUrl,
    String tickerSymbol,
    HoldingAccountDto account,
    String countryCode,
    Boolean longTerm
) {
    public static HoldingDto from(Holding holding) {
        if (holding == null) return null;
        return new HoldingDto(
            holding.getId(),
            holding.getName(),
            holding.getCategory(),
            holding.getQuantity(),
            holding.getQuantity(), // units
            holding.getAvgBuyPrice(),
            holding.getAvgBuyPrice(), // averagePrice
            holding.getLogoUrl(),
            holding.getTickerSymbol(),
            HoldingAccountDto.from(holding.getAccount()),
            holding.getCountryCode(),
            calculateIsLongTerm(holding)
        );
    }

    private static Boolean calculateIsLongTerm(Holding holding) {
        if (holding.getCategory() == HoldingCategory.EQUITY && holding.getPurchaseDate() != null) {
            String cCode = holding.getCountryCode() != null ? holding.getCountryCode() : "IN";
            int threshold = 12; // default
            if (holding.getUser() != null && holding.getUser().getLongTermThresholds() != null 
                && holding.getUser().getLongTermThresholds().containsKey(cCode)) {
                threshold = holding.getUser().getLongTermThresholds().get(cCode);
            }
            return holding.getPurchaseDate().plusMonths(threshold).isBefore(LocalDate.now().plusDays(1));
        }
        return false;
    }
}
