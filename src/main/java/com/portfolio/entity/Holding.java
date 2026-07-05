package com.portfolio.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import jakarta.persistence.Version;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents an investment or asset holding within an account.
 */
@Getter
@Setter
@Entity
@Table(name = DatabaseConstants.TABLE_HOLDINGS)
public class Holding {

    /** Unique identifier for the holding. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** The user who owns this holding. */
    @JsonIgnore
    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = DatabaseConstants.COLUMN_USER_ID, nullable = false)
    private User user;

    /** The account that owns this holding. */
    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = DatabaseConstants.COLUMN_ACCOUNT_ID, nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Account account;

    /** The name of the holding. */
    @Convert(converter = AttributeEncryptor.class)
    @Column(nullable = false)
    private String name;

    /** The category of the holding (e.g., stock, mutual fund). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HoldingCategory category;

    /** The ticker symbol for the holding, if applicable. */
    @Convert(converter = AttributeEncryptor.class)
    @Column(length = 50)
    private String tickerSymbol;

    /** The quantity of units held. */
    @Column(precision = 15, scale = 10)
    private BigDecimal quantity;

    /** The average purchase price per unit. */
    @Column(precision = 15, scale = 2)
    private BigDecimal avgBuyPrice;

    /** The date when the holding was purchased. */
    private LocalDate purchaseDate;

    /** Additional notes about the holding. */
    @Convert(converter = AttributeEncryptor.class)
    @Column(columnDefinition = DatabaseConstants.COLUMN_DEF_TEXT)
    private String notes;

    /** Transient field to include logo URL in API responses */
    @jakarta.persistence.Transient
    private String logoUrl;

    /** Persistent field to store country code for the holding */
    @Column(name = "country_code")
    private String countryCode;

    /** Optimistic locking version. */
    @Version
    @Column(columnDefinition = "integer DEFAULT 0", nullable = false)
    private Integer version = 0;
}
