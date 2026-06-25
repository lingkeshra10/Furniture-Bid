package com.furniturebid.mockapi.property;

import com.furniturebid.mockapi.security.JwtUtility;
import io.jsonwebtoken.Claims;
import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.StringLength;

import static org.assertj.core.api.Assertions.assertThat;

// Feature: mock-api-service, Property 1: JWT Token Round-Trip
// Validates: Requirements 3.1, 3.2
class JwtRoundTripPropertyTest {

    private final JwtUtility jwtUtility = new JwtUtility();

    @Property
    void jwtTokenRoundTrip(
            @ForAll @AlphaChars @StringLength(min = 1, max = 50) String userId,
            @ForAll("validRoles") String role
    ) {
        // Generate token
        String token = jwtUtility.generateToken(userId, role);

        // Parse token
        Claims claims = jwtUtility.parseToken(token);

        // Verify userId (sub claim) matches
        assertThat(claims.getSubject()).isEqualTo(userId);

        // Verify role claim matches
        assertThat(claims.get("role", String.class)).isEqualTo(role);

        // Verify exp = iat + 3600 seconds
        long iat = claims.getIssuedAt().getTime() / 1000;
        long exp = claims.getExpiration().getTime() / 1000;
        assertThat(exp - iat).isEqualTo(3600L);
    }

    @Provide
    Arbitrary<String> validRoles() {
        return Arbitraries.of("buyer", "seller", "admin");
    }
}
