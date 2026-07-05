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
import jakarta.persistence.Table;
import jakarta.persistence.Convert;
import com.portfolio.converter.AttributeEncryptor;
import lombok.Getter;
import lombok.Setter;
import com.portfolio.converter.MapToJsonConverter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a user (tenant) in the multi-tenant SaaS system.
 */
@Getter
@Setter
@Entity
@Table(name = DatabaseConstants.TABLE_USERS)
public class User {

    /** Unique identifier for the user. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** External authentication provider ID (e.g., Auth0, Keycloak). */
    @Convert(converter = AttributeEncryptor.class)
    @Column(unique = true)
    private String authProviderId;

    /** The user's email address. */
    @Convert(converter = AttributeEncryptor.class)
    @Column(unique = true, nullable = false)
    private String email;

    /** The user's password hash (for email/password auth). */
    @JsonIgnore
    @Convert(converter = AttributeEncryptor.class)
    @Column
    private String passwordHash;

    /** The user's subscription plan. */
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.FREE;

    /** The user's portfolio base currency. */
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Currency baseCurrency = Currency.INR;

    /** The user's long term threshold for equity in months mapped by country code. */
    @Convert(converter = MapToJsonConverter.class)
    @Column(name = "long_term_thresholds", columnDefinition = "text")
    private Map<String, Integer> longTermThresholds = new HashMap<>();

    /** The timestamp when the user was created. */
    @Column(name = DatabaseConstants.COLUMN_CREATED_AT, nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
