package com.portfolio.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Thread-local context for storing the current authenticated user's ID.
 * Provides thread-safe access to user information throughout the request lifecycle.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserContext {
    private static final ThreadLocal<Integer> currentUserId = new ThreadLocal<>();

    /**
     * Sets the current user ID for the current thread.
     *
     * @param userId the user ID to set
     */
    public static void setCurrentUserId(final Integer userId) {
        currentUserId.set(userId);
    }

    /**
     * Retrieves the current user ID for the current thread.
     *
     * @return the current user ID, or null if not set
     */
    public static Integer getCurrentUserId() {
        return currentUserId.get();
    }

    /**
     * Clears the current user ID from the current thread.
     * Should be called at the end of request processing to prevent memory leaks.
     */
    public static void clear() {
        currentUserId.remove();
    }
}
