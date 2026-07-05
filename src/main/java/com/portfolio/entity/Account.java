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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * Represents a financial account belonging to a family member.
 */
@Getter
@Setter
@Entity
@Table(name = DatabaseConstants.TABLE_ACCOUNTS)
public class Account {

    /** Unique identifier for the account. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** The user who owns this account. */
    @JsonIgnore
    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = DatabaseConstants.COLUMN_USER_ID, nullable = false)
    private User user;

    /** The family member who owns this account. */
    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = DatabaseConstants.COLUMN_FAMILY_MEMBER_ID, nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private FamilyMember familyMember;

    /** The name of the account. */
    @Convert(converter = AttributeEncryptor.class)
    @Column(nullable = false, length = 100)
    private String name;

    /** The type of the account. */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AccountType type = AccountType.OTHER;

    /** The balance present in this account. */
    @Column(precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    /** The timestamp when the account was created. */
    @Column(name = DatabaseConstants.COLUMN_CREATED_AT, nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Optimistic locking version. */
    @Version
    @Column(columnDefinition = "integer DEFAULT 0", nullable = false)
    private Integer version = 0;
}
