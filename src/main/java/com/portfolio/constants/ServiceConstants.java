package com.portfolio.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constants for service layer operations.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServiceConstants {

    public static final String ERROR_USER_NOT_FOUND = "User not found";
    public static final String ERROR_REFRESH_TOKEN_EXPIRED = "Refresh token expired or revoked";
    public static final String ERROR_REFRESH_TOKEN_NOT_FOUND = "Refresh token not found";
    public static final String PROPERTY_JWT_REFRESH_TOKEN_EXPIRATION = "${jwt.refresh-token-expiration}";
}
