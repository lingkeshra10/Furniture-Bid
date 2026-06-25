package com.furniturebid.mockapi.security;

/**
 * Holds the authenticated user's context extracted from a valid JWT token.
 * Stored as a request attribute for downstream controllers to access.
 */
public class AuthenticatedUser {

    private final String userId;
    private final String role;

    public AuthenticatedUser(String userId, String role) {
        this.userId = userId;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }
}
