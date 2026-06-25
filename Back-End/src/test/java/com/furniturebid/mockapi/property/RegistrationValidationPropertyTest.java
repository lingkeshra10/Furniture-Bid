package com.furniturebid.mockapi.property;

import com.furniturebid.mockapi.dto.response.LoginResponse;
import com.furniturebid.mockapi.exception.ValidationException;
import com.furniturebid.mockapi.security.JwtUtility;
import com.furniturebid.mockapi.service.AuthService;
import com.furniturebid.mockapi.store.MockDataStore;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Feature: mock-api-service, Property 2: Registration Input Validation
// Validates: Requirements 2.3, 2.5
class RegistrationValidationPropertyTest {

    private AuthService createAuthService() {
        return new AuthService(new MockDataStore(), new JwtUtility());
    }

    // ========== Property 1: Valid registration succeeds ==========

    @Property
    void validRegistrationReturnsTokenAndBuyerUser(
            @ForAll("validEmails") String email,
            @ForAll("validPasswords") String password,
            @ForAll("validDisplayNames") String displayName
    ) {
        AuthService authService = createAuthService();

        LoginResponse response = authService.register(email, password, displayName);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotNull().isNotBlank();
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getRole()).isEqualTo("buyer");
        assertThat(response.getUser().getEmail()).isEqualTo(email);
        assertThat(response.getUser().getDisplayName()).isEqualTo(displayName);
    }

    // ========== Property 2: Invalid registration fails ==========

    @Property
    void invalidEmailCausesValidationError(
            @ForAll("invalidEmails") String email,
            @ForAll("validPasswords") String password,
            @ForAll("validDisplayNames") String displayName
    ) {
        AuthService authService = createAuthService();

        assertThatThrownBy(() -> authService.register(email, password, displayName))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> {
                    ValidationException ve = (ValidationException) ex;
                    assertThat(ve.getFieldErrors()).isNotEmpty();
                    assertThat(ve.getFieldErrors()).containsKey("email");
                });
    }

    @Property
    void invalidPasswordCausesValidationError(
            @ForAll("validEmails") String email,
            @ForAll("invalidPasswords") String password,
            @ForAll("validDisplayNames") String displayName
    ) {
        AuthService authService = createAuthService();

        assertThatThrownBy(() -> authService.register(email, password, displayName))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> {
                    ValidationException ve = (ValidationException) ex;
                    assertThat(ve.getFieldErrors()).isNotEmpty();
                    assertThat(ve.getFieldErrors()).containsKey("password");
                });
    }

    @Property
    void invalidDisplayNameCausesValidationError(
            @ForAll("validEmails") String email,
            @ForAll("validPasswords") String password,
            @ForAll("invalidDisplayNames") String displayName
    ) {
        AuthService authService = createAuthService();

        assertThatThrownBy(() -> authService.register(email, password, displayName))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> {
                    ValidationException ve = (ValidationException) ex;
                    assertThat(ve.getFieldErrors()).isNotEmpty();
                    assertThat(ve.getFieldErrors()).containsKey("displayName");
                });
    }

    // ========== Providers ==========

    @Provide
    Arbitrary<String> validEmails() {
        Arbitrary<String> localPart = Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('0', '9')
                .ofMinLength(1)
                .ofMaxLength(10);

        Arbitrary<String> domain = Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(2)
                .ofMaxLength(8);

        Arbitrary<String> tld = Arbitraries.of("com", "org", "net", "io", "dev");

        return Combinators.combine(localPart, domain, tld)
                .as((local, dom, t) -> local + "@" + dom + "." + t);
    }

    @Provide
    Arbitrary<String> validPasswords() {
        // Generate passwords 8-64 chars that include at least 1 uppercase, 1 lowercase, 1 digit
        return Arbitraries.integers().between(8, 64).flatMap(length -> {
            // Start with required characters: 1 uppercase, 1 lowercase, 1 digit
            Arbitrary<Character> upper = Arbitraries.chars().range('A', 'Z');
            Arbitrary<Character> lower = Arbitraries.chars().range('a', 'z');
            Arbitrary<Character> digit = Arbitraries.chars().range('0', '9');
            Arbitrary<String> rest = Arbitraries.strings()
                    .withCharRange('a', 'z')
                    .withCharRange('A', 'Z')
                    .withCharRange('0', '9')
                    .ofLength(length - 3);

            return Combinators.combine(upper, lower, digit, rest)
                    .as((u, l, d, r) -> "" + u + l + d + r);
        });
    }

    @Provide
    Arbitrary<String> validDisplayNames() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withCharRange(' ', ' ')
                .ofMinLength(3)
                .ofMaxLength(50)
                // Ensure at least 3 non-space chars by starting with alpha
                .filter(s -> s.trim().length() >= 3);
    }

    @Provide
    Arbitrary<String> invalidEmails() {
        return Arbitraries.oneOf(
                // Missing @ sign
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(3).ofMaxLength(10),
                // Missing domain after @
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(5)
                        .map(s -> s + "@"),
                // Missing TLD (no dot after @)
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(5)
                        .map(s -> s + "@nodot"),
                // Empty string
                Arbitraries.just(""),
                // Null-like (empty)
                Arbitraries.just("   ")
        );
    }

    @Provide
    Arbitrary<String> invalidPasswords() {
        return Arbitraries.oneOf(
                // Too short (less than 8 chars) but valid character classes
                Arbitraries.integers().between(1, 7).map(len -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append('A'); // uppercase
                    sb.append('a'); // lowercase
                    sb.append('1'); // digit
                    for (int i = 3; i < len; i++) {
                        sb.append('x');
                    }
                    return sb.toString();
                }),
                // Too long (more than 64 chars)
                Arbitraries.strings().withCharRange('a', 'z').ofLength(62)
                        .map(s -> "A1" + s + "extra"),
                // Missing uppercase (all lowercase + digits)
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(6).ofMaxLength(12)
                        .map(s -> s + "12"),
                // Missing lowercase (all uppercase + digits)
                Arbitraries.strings().withCharRange('A', 'Z').ofMinLength(6).ofMaxLength(12)
                        .map(s -> s + "12"),
                // Missing digit (all letters)
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(4).ofMaxLength(10)
                        .map(s -> "ABCD" + s)
        );
    }

    @Provide
    Arbitrary<String> invalidDisplayNames() {
        return Arbitraries.oneOf(
                // Too short (less than 3 chars)
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(2),
                // Too long (more than 50 chars)
                Arbitraries.strings().withCharRange('a', 'z').ofLength(51),
                // Empty string
                Arbitraries.just("")
        );
    }
}
