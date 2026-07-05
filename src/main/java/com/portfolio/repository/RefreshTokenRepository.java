package com.portfolio.repository;

import com.portfolio.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository interface for RefreshToken entity operations.
 * Provides database access methods for refresh token management.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    
    /**
     * Finds a refresh token by its token string.
     *
     * @param token the token string
     * @return an Optional containing the refresh token if found
     */
    Optional<RefreshToken> findByToken(String token);
    
    /**
     * Deletes all refresh tokens for a specific user.
     *
     * @param userId the user ID
     */
    void deleteByUserId(Integer userId);
}
