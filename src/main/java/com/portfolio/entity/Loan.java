package com.portfolio.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.portfolio.constants.DatabaseConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Convert;
import com.portfolio.converter.AttributeEncryptor;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

/**
 * Represents a loan taken by a family member.
 */
@Getter
@Setter
@Entity
@Table(name = DatabaseConstants.TABLE_LOANS)
public class Loan {

    /** Unique identifier for the loan. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** The user who owns this loan. */
    @JsonIgnore
    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = DatabaseConstants.COLUMN_USER_ID, nullable = false)
    private User user;

    /** The name of the loan. */
    @Convert(converter = AttributeEncryptor.class)
    @Column(nullable = false, length = 100)
    @JsonProperty("loanName")
    private String name;

    /** The type or category of the loan (e.g. Home Loan) */
    @Convert(converter = AttributeEncryptor.class)
    @Column(length = 50)
    private String type;

    /** The loan provider. */
    @Convert(converter = AttributeEncryptor.class)
    @Column(nullable = false, length = 100)
    private String provider;

    /** The principal amount of the loan. */
    @Column(nullable = false, precision = 15, scale = 2)
    @JsonProperty("principal")
    private BigDecimal principleAmount;

    /** The outstanding amount remaining on the loan. */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal outstanding;

    /** The interest rate of the loan. */
    @Column(nullable = false, precision = 5, scale = 2)
    @JsonProperty("interestRate")
    private BigDecimal interest;

    /** The EMI (Equated Monthly Installment) amount. */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal emi;

    /** The currency of the loan. */
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Currency currency = Currency.INR;
}
