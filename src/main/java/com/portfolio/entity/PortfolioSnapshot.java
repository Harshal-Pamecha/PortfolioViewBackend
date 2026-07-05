package com.portfolio.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.portfolio.constants.DatabaseConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a daily snapshot of a user's total portfolio net worth.
 */
@Getter
@Setter
@Entity
@Table(name = DatabaseConstants.TABLE_PORTFOLIO_SNAPSHOTS, uniqueConstraints = {
    @UniqueConstraint(columnNames = {DatabaseConstants.COLUMN_USER_ID, "date"})
})
public class PortfolioSnapshot {

    /** Unique identifier for the snapshot. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** The user who owns this snapshot. */
    @JsonIgnore
    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = DatabaseConstants.COLUMN_USER_ID, nullable = false)
    private User user;

    /** The date of the snapshot. */
    @Column(nullable = false)
    private LocalDate date;

    /** The total equity / net worth value of the portfolio in the user's base currency. */
    @Column(name = "equity_value", nullable = false, precision = 15, scale = 2)
    @JsonProperty("equityValue")
    private BigDecimal equityValue;

    /** The total value of Indian equity holdings in USD. */
    @Column(name = "in_equity_value", nullable = false, precision = 15, scale = 2)
    @JsonProperty("inEquityValue")
    private BigDecimal inEquityValue = BigDecimal.ZERO;

    /** The total value of US equity holdings in USD. */
    @Column(name = "us_equity_value", nullable = false, precision = 15, scale = 2)
    @JsonProperty("usEquityValue")
    private BigDecimal usEquityValue = BigDecimal.ZERO;
}
