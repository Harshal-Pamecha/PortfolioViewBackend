package com.portfolio.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * Data Transfer Object for processing a sale of an asset (Holding).
 */
@Data
public class SellRequestDto {
    /** The number of units being sold. */
    private BigDecimal quantitySold;

    /** The price per unit at which the units were sold. */
    private BigDecimal sellPrice;
}
