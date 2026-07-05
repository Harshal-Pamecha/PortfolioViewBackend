package com.portfolio.service;

import com.portfolio.constants.ServiceConstants;
import com.portfolio.entity.RefreshToken;
import com.portfolio.entity.User;
import com.portfolio.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service class for managing refresh token operations.
 * Handles creation, validation, and revocation of refresh tokens.
 */
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final long refreshTokenExpiration;

    public RefreshTokenService(
            RefreshTokenRepository repository,
            @Value(ServiceConstants.PROPERTY_JWT_REFRESH_TOKEN_EXPIRATION) long refreshTokenExpiration) {
        this.repository = repository;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    /**
     * Creates a new refresh token for the specified user.
     *
     * @param user the user for whom to create the token
     * @return the created refresh token
     */
    public RefreshToken createRefreshToken(final User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000));
        return repository.save(refreshToken);
    }

    /**
     * Verifies that a refresh token is not expired or revoked.
     *
     * @param token the refresh token to verify
     * @throws RuntimeException if token is expired or revoked
     */
    public void verifyExpiration(final RefreshToken token) {
        if (token.getExpiryDate().isBefore(LocalDateTime.now()) || token.isRevoked()) {
            repository.delete(token);
            throw new RuntimeException(ServiceConstants.ERROR_REFRESH_TOKEN_EXPIRED);
        }
    }

    /**
     * Finds a refresh token by its token string.
     *
     * @param token the token string
     * @return the refresh token
     * @throws RuntimeException if token not found
     */
    public RefreshToken findByToken(final String token) {
        return repository.findByToken(token)
                .orElseThrow(() -> new RuntimeException(ServiceConstants.ERROR_REFRESH_TOKEN_NOT_FOUND));
    }

    /**
     * Revokes all refresh tokens for a specific user.
     *
     * @param userId the user ID
     */
    @Transactional
    public void revokeUserTokens(final Integer userId) {
        repository.deleteByUserId(userId);
    }
}
