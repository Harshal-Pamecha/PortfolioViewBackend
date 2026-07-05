package com.portfolio.entity;

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
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Represents a refresh token for user authentication.
 */
@Getter
@Setter
@Entity
@Table(name = DatabaseConstants.TABLE_REFRESH_TOKENS)
public class RefreshToken {

    /** Unique identifier for the refresh token. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** The user who owns this refresh token. */
    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = DatabaseConstants.COLUMN_USER_ID, nullable = false)
    private User user;

    /** The refresh token string. */
    @Convert(converter = AttributeEncryptor.class)
    @Column(nullable = false, unique = true, length = 500)
    private String token;

    /** The expiration date of the refresh token. */
    @Column(nullable = false)
    private LocalDateTime expiryDate;

    /** Whether the token has been revoked. */
    @Column(nullable = false)
    private boolean revoked = false;
}
