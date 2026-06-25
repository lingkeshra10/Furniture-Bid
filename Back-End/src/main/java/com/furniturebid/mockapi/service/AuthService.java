package com.furniturebid.mockapi.service;

import com.furniturebid.mockapi.dto.response.LoginResponse;
import com.furniturebid.mockapi.dto.response.UserDto;
import com.furniturebid.mockapi.entity.UserEntity;
import com.furniturebid.mockapi.exception.ConflictException;
import com.furniturebid.mockapi.exception.InvalidCredentialsException;
import com.furniturebid.mockapi.exception.NotFoundException;
import com.furniturebid.mockapi.exception.ValidationException;
import com.furniturebid.mockapi.security.JwtUtility;
import com.furniturebid.mockapi.store.MockDataStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Service handling authentication operations: login, register, password reset,
 * token refresh, and social login.
 */
@Service
public class AuthService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");

    private final MockDataStore dataStore;
    private final JwtUtility jwtUtility;

    public AuthService(MockDataStore dataStore, JwtUtility jwtUtility) {
        this.dataStore = dataStore;
        this.jwtUtility = jwtUtility;
    }

    /**
     * Authenticates a user by email and password.
     *
     * @param email    the user's email
     * @param password the user's password (plain text comparison for mock)
     * @return LoginResponse with JWT token and user details
     * @throws InvalidCredentialsException if email not found or password doesn't match
     */
    public LoginResponse login(String email, String password) {
        Optional<UserEntity> userOpt = dataStore.getUserByEmail(email);

        if (userOpt.isEmpty() || !userOpt.get().getPassword().equals(password)) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        UserEntity user = userOpt.get();
        String token = jwtUtility.generateToken(user.getId().toString(), user.getRole());
        return new LoginResponse(token, toUserDto(user));
    }

    /**
     * Registers a new user after validating input fields.
     *
     * @param email       the user's email
     * @param password    the user's password
     * @param displayName the user's display name
     * @return LoginResponse with JWT token and newly created user details
     * @throws ValidationException if any field fails validation
     * @throws ConflictException   if email is already registered
     */
    public LoginResponse register(String email, String password, String displayName) {
        // Validate fields
        Map<String, String> fieldErrors = new HashMap<>();

        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            fieldErrors.put("email", "Invalid email format");
        }

        if (password == null || password.length() < 8 || password.length() > 64
                || !UPPERCASE_PATTERN.matcher(password).find()
                || !LOWERCASE_PATTERN.matcher(password).find()
                || !DIGIT_PATTERN.matcher(password).find()) {
            fieldErrors.put("password",
                    "Password must be 8-64 characters with at least one uppercase letter, one lowercase letter, and one digit");
        }

        if (displayName == null || displayName.length() < 3 || displayName.length() > 50) {
            fieldErrors.put("displayName", "Display name must be between 3 and 50 characters");
        }

        if (!fieldErrors.isEmpty()) {
            throw new ValidationException(fieldErrors);
        }

        // Check for existing email
        if (dataStore.getUserByEmail(email).isPresent()) {
            throw new ConflictException("Email already registered");
        }

        // Create new user
        UUID userId = UUID.randomUUID();
        UserEntity newUser = new UserEntity(
                userId,
                email,
                password,
                displayName,
                "buyer",
                null,
                "active",
                Instant.now()
        );

        dataStore.getUsers().put(userId.toString(), newUser);

        String token = jwtUtility.generateToken(userId.toString(), "buyer");
        return new LoginResponse(token, toUserDto(newUser));
    }

    /**
     * Initiates a password reset. This is a no-op for the mock service.
     *
     * @param email the user's email (unused in mock)
     */
    public void resetPassword(String email) {
        // No-op for mock service — controller returns 204
    }

    /**
     * Refreshes a JWT token for the given user.
     *
     * @param userId the user's ID
     * @return LoginResponse with a new JWT token and user details
     * @throws NotFoundException if user ID not found
     */
    public LoginResponse refreshToken(String userId) {
        UserEntity user = dataStore.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String token = jwtUtility.generateToken(user.getId().toString(), user.getRole());
        return new LoginResponse(token, toUserDto(user));
    }

    /**
     * Handles social login (Google/Facebook). Finds or creates a mock user
     * for the given provider.
     *
     * @param provider the social provider ("google" or "facebook")
     * @param token    the provider token (not validated in mock)
     * @return LoginResponse with JWT token and user details
     * @throws ValidationException if provider is not supported
     */
    public LoginResponse socialLogin(String provider, String token) {
        // Validate provider
        if (provider == null || (!provider.equalsIgnoreCase("google") && !provider.equalsIgnoreCase("facebook"))) {
            Map<String, String> fieldErrors = new HashMap<>();
            fieldErrors.put("provider", "Provider must be 'google' or 'facebook'");
            throw new ValidationException(fieldErrors);
        }

        String normalizedProvider = provider.toLowerCase();
        String mockEmail = normalizedProvider + "_user@example.com";

        // Find existing user by mock email or create one
        Optional<UserEntity> existingUser = dataStore.getUserByEmail(mockEmail);

        if (existingUser.isPresent()) {
            UserEntity user = existingUser.get();
            String jwtToken = jwtUtility.generateToken(user.getId().toString(), user.getRole());
            return new LoginResponse(jwtToken, toUserDto(user));
        }

         // Create new social user
        UUID userId = UUID.randomUUID();
        String displayName = capitalizeFirst(normalizedProvider) + " User";
        UserEntity newUser = new UserEntity(
                userId,
                mockEmail,
                "",
                displayName,
                "buyer",
                null,
                "active",
                Instant.now()
        );

        dataStore.getUsers().put(userId.toString(), newUser);

        String jwtToken = jwtUtility.generateToken(userId.toString(), "buyer");
        return new LoginResponse(jwtToken, toUserDto(newUser));
    }

    /**
     * Converts a UserEntity to a UserDto (excludes password).
     */
    private UserDto toUserDto(UserEntity entity) {
        return new UserDto(
                entity.getId(),
                entity.getEmail(),
                entity.getDisplayName(),
                entity.getRole(),
                entity.getAvatarUrl(),
                entity.getCreatedAt()
        );
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
