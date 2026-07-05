package com.portfolio.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.portfolio.constants.DatabaseConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Convert;
import com.portfolio.converter.AttributeEncryptor;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

/**
 * Represents a family member in the portfolio system.
 */
@Getter
@Setter
@Entity
@Table(name = DatabaseConstants.TABLE_FAMILY_MEMBERS, uniqueConstraints = {
    @UniqueConstraint(columnNames = {DatabaseConstants.COLUMN_USER_ID, "name"})
})
public class FamilyMember {

    /** Unique identifier for the family member. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** The user who owns this family member record. */
    @JsonIgnore
    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = DatabaseConstants.COLUMN_USER_ID, nullable = false)
    private User user;

    /** The name of the family member. */
    @Convert(converter = AttributeEncryptor.class)
    @Column(nullable = false, length = 50)
    private String name;

    /** The cash balance controlled by the family member. */
    @Column(precision = 15, scale = 2)
    private BigDecimal balance;

    /** The currency of the cash balance. */
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Currency currency;

    /** Optimistic locking version. */
    @Version
    @Column(columnDefinition = "integer DEFAULT 0", nullable = false)
    private Integer version = 0;
}
