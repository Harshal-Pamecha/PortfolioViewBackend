package com.portfolio.entity;

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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a financial transaction in the portfolio system.
 */
@Getter
@Setter
@Entity
@Table(name = DatabaseConstants.TABLE_TRANSACTIONS)
public class Transaction {

    /** Unique identifier for the transaction. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** The user who owns this transaction. */
    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = DatabaseConstants.COLUMN_USER_ID, nullable = false)
    private User user;

    /** The date and time when the transaction occurred. */
    @Column(nullable = false)
    private LocalDateTime date;

    /** The type of transaction. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    /** The monetary amount of the transaction. */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    /** The currency of the transaction amount. */
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Currency currency;

    /** The source account for the transaction, if applicable. */
    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = DatabaseConstants.COLUMN_SOURCE_ACCOUNT_ID)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Account sourceAccount;

    /** The destination account for the transaction, if applicable. */
    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = DatabaseConstants.COLUMN_DESTINATION_ACCOUNT_ID)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Account destAccount;

    /** Additional notes about the transaction. */
    @Convert(converter = AttributeEncryptor.class)
    @Column(columnDefinition = DatabaseConstants.COLUMN_DEF_TEXT)
    private String notes;

    /** The holding corresponding to this transaction, if applicable. */
    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = DatabaseConstants.COLUMN_HOLDING_ID)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Holding holding;
    
    /** The realized profit or loss from this transaction (if it is a sale). */
    @Column(precision = 19, scale = 4)
    private BigDecimal realizedPnl;
}
