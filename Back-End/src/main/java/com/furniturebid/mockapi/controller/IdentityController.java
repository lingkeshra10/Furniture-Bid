package com.furniturebid.mockapi.controller;

import com.furniturebid.mockapi.dto.request.LoginRequest;
import com.furniturebid.mockapi.dto.request.RegisterRequest;
import com.furniturebid.mockapi.dto.request.SocialLoginRequest;
import com.furniturebid.mockapi.dto.response.LoginResponse;
import com.furniturebid.mockapi.security.AuthenticatedUser;
import com.furniturebid.mockapi.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller handling identity/authentication endpoints.
 */
@RestController
@RequestMapping("/api/auth")
public class IdentityController {

    private final AuthService authService;

    public IdentityController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/login - Authenticate user and return token + user.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/register - Validate, create user, return token + user.
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(
                request.getEmail(),
                request.getPassword(),
                request.getDisplayName()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/reset-password - Return 204 regardless of email existence.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody Map<String, String> body) {
        String email = body.getOrDefault("email", "");
        authService.resetPassword(email);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/auth/refresh-token - Generate new token from existing user context.
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(HttpServletRequest request) {
        AuthenticatedUser authUser = (AuthenticatedUser) request.getAttribute("authenticatedUser");
        LoginResponse response = authService.refreshToken(authUser.getUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/social-login - Validate provider (google/facebook), return token + user.
     */
    @PostMapping("/social-login")
    public ResponseEntity<LoginResponse> socialLogin(@Valid @RequestBody SocialLoginRequest request) {
        LoginResponse response = authService.socialLogin(request.getProvider(), request.getToken());
        return ResponseEntity.ok(response);
    }
}
