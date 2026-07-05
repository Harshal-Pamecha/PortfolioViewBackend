package com.portfolio.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constants for security configuration.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityConstants {

    public static final String API_AUTH_LOGIN = "/api/auth/login";
    public static final String API_AUTH_REGISTER = "/api/auth/register";
    public static final String API_AUTH_REFRESH = "/api/auth/refresh";

    public static final String CLAIM_USER_ID = "userId";
    
    public static final String PROPERTY_JWT_SECRET = "${jwt.secret}";
    public static final String PROPERTY_JWT_ACCESS_TOKEN_EXPIRATION = "${jwt.access-token-expiration}";
    
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final int BEARER_PREFIX_LENGTH = 7;
    public static final String ERROR_INVALID_TOKEN = "Invalid or expired token";
    
    public static final String RESPONSE_KEY_ACCESS_TOKEN = "accessToken";
    public static final String RESPONSE_KEY_REFRESH_TOKEN = "refreshToken";
    public static final String RESPONSE_KEY_MESSAGE = "message";
    public static final String MESSAGE_LOGOUT_SUCCESS = "Logged out successfully";
    public static final String MESSAGE_REGISTRATION_SUCCESS = "User registered successfully";
    public static final String ERROR_INVALID_CREDENTIALS = "Invalid credentials";
    public static final String ERROR_INVALID_AUTH_PROVIDER = "Invalid auth provider";
    public static final String ERROR_INVALID_REGISTRATION = "Provide either password or authProviderId, not both";
}
