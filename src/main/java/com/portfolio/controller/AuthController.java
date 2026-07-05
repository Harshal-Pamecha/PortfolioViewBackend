package com.portfolio.controller;

import com.portfolio.constants.SecurityConstants;
import com.portfolio.entity.RefreshToken;
import com.portfolio.entity.User;
import com.portfolio.security.JwtUtil;
import com.portfolio.service.RefreshTokenService;
import com.portfolio.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.portfolio.dto.request.RegisterRequestDto;
import com.portfolio.dto.request.LoginRequestDto;
import com.portfolio.dto.request.RefreshTokenRequestDto;
import com.portfolio.dto.request.LogoutRequestDto;
import java.util.Map;

/**
 * REST controller for authentication operations.
 * Handles registration, login, token refresh, and logout functionality.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new user with email/password or OAuth.
     *
     * @param request containing email and either password or authProviderId
     * @return success message
     */
    @PostMapping("/register")
    public Map<String, String> register(@RequestBody final RegisterRequestDto request) {
        String email = request.email();
        String password = request.password();
        String authProviderId = request.authProviderId();
        
        User user = new User();
        user.setEmail(email);
        
        boolean hasPassword = password != null && !password.isEmpty();
        boolean hasAuthProvider = authProviderId != null && !authProviderId.isEmpty();
        
        if (hasPassword && hasAuthProvider) {
            throw new RuntimeException(SecurityConstants.ERROR_INVALID_REGISTRATION);
        }
        
        if (hasPassword) {
            user.setPasswordHash(passwordEncoder.encode(password));
        } else if (hasAuthProvider) {
            user.setAuthProviderId(authProviderId);
        } else {
            throw new RuntimeException(SecurityConstants.ERROR_INVALID_REGISTRATION);
        }
        userService.create(user);
        return Map.of(SecurityConstants.RESPONSE_KEY_MESSAGE, SecurityConstants.MESSAGE_REGISTRATION_SUCCESS);
    }

    /**
     * Authenticates a user with email/password or OAuth.
     *
     * @param request containing email and either password or authProviderId
     * @return access token and refresh token
     */
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody final LoginRequestDto request) {
        String email = request.email();
        String password = request.password();
        String authProviderId = request.authProviderId();
        
        User user = userService.findByEmail(email);
        
        if (password != null && user.getPasswordHash() != null) {
            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                throw new RuntimeException(SecurityConstants.ERROR_INVALID_CREDENTIALS);
            }
        } else if (authProviderId != null && !authProviderId.equals(user.getAuthProviderId())) {
            throw new RuntimeException(SecurityConstants.ERROR_INVALID_AUTH_PROVIDER);
        }
        String accessToken = jwtUtil.generateAccessToken(user.getId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        return Map.of(
            SecurityConstants.RESPONSE_KEY_ACCESS_TOKEN, accessToken,
            SecurityConstants.RESPONSE_KEY_REFRESH_TOKEN, refreshToken.getToken()
        );
    }

    /**
     * Refreshes an access token using a valid refresh token.
     *
     * @param request the refresh request containing refresh token
     * @return a map containing the new access token
     */
    @PostMapping("/refresh")
    public Map<String, String> refresh(@RequestBody final RefreshTokenRequestDto request) {
        String refreshTokenStr = request.refreshToken();
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenStr);
        refreshTokenService.verifyExpiration(refreshToken);
        String newAccessToken = jwtUtil.generateAccessToken(refreshToken.getUser().getId());
        return Map.of(SecurityConstants.RESPONSE_KEY_ACCESS_TOKEN, newAccessToken);
    }

    /**
     * Logs out a user by revoking their refresh tokens.
     *
     * @param request the logout request containing refresh token
     * @return a map containing success message
     */
    @PostMapping("/logout")
    public Map<String, String> logout(@RequestBody final LogoutRequestDto request) {
        String refreshTokenStr = request.refreshToken();
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenStr);
        refreshTokenService.revokeUserTokens(refreshToken.getUser().getId());
        return Map.of(SecurityConstants.RESPONSE_KEY_MESSAGE, SecurityConstants.MESSAGE_LOGOUT_SUCCESS);
    }
}
