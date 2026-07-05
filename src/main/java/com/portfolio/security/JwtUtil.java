package com.portfolio.security;

import com.portfolio.constants.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utility class for JWT token operations.
 * Handles token generation, validation, and extraction of user information.
 */
@Component
public class JwtUtil {

    private final String secret;
    private final long accessTokenExpiration;

    public JwtUtil(
            @Value(SecurityConstants.PROPERTY_JWT_SECRET) String secret,
            @Value(SecurityConstants.PROPERTY_JWT_ACCESS_TOKEN_EXPIRATION) long accessTokenExpiration) {
        this.secret = secret;
        this.accessTokenExpiration = accessTokenExpiration;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates an access token for the specified user.
     *
     * @param userId the user ID to include in the token
     * @return the generated JWT access token
     */
    public String generateAccessToken(final Integer userId) {
        return Jwts.builder()
                .claim(SecurityConstants.CLAIM_USER_ID, userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validates a JWT token and extracts its claims.
     *
     * @param token the JWT token to validate
     * @return the claims contained in the token
     */
    public Claims validateToken(final String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts the user ID from a JWT token.
     *
     * @param token the JWT token
     * @return the user ID from the token
     */
    public Integer getUserIdFromToken(final String token) {
        Claims claims = validateToken(token);
        return claims.get(SecurityConstants.CLAIM_USER_ID, Integer.class);
    }
}
