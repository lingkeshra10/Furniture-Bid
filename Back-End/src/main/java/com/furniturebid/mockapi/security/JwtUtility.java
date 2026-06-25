package com.furniturebid.mockapi.security;

import com.furniturebid.mockapi.exception.TokenExpiredException;
import com.furniturebid.mockapi.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

/**
 * JWT utility for token generation and validation.
 * Uses a hardcoded HMAC-SHA256 signing key (mock service for development only).
 */
@Component
public class JwtUtility {

    private static final String SECRET_KEY_BASE64 =
            "dGhpcyBpcyBhIG1vY2sgc2VydmljZSBzZWNyZXQga2V5IGZvciBkZXZlbG9wbWVudCBvbmx5IQ==";

    private static final long EXPIRATION_SECONDS = 3600L;

    private final SecretKey signingKey;

    public JwtUtility() {
        byte[] keyBytes = Base64.getDecoder().decode(SECRET_KEY_BASE64);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a JWT token for the given user.
     *
     * @param userId the user's unique identifier (set as 'sub' claim)
     * @param role   the user's role (set as custom 'role' claim)
     * @return signed JWT token string
     */
    public String generateToken(String userId, String role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION_SECONDS * 1000);

        return Jwts.builder()
                .subject(userId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Parses and validates a JWT token.
     * Throws TokenExpiredException if the token is expired.
     * Throws UnauthorizedException if the token is malformed or has an invalid signature.
     *
     * @param token the JWT token string
     * @return the parsed Claims
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("Token has expired");
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid token: " + e.getMessage());
        }
    }

    /**
     * Parses a JWT token allowing expired tokens (for the refresh-token flow).
     * Still throws UnauthorizedException on invalid signature or malformed tokens.
     *
     * @param token the JWT token string
     * @return the parsed Claims (may be expired)
     */
    public Claims parseTokenAllowExpired(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            // Token is expired but signature is valid — return the claims
            return e.getClaims();
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid token: " + e.getMessage());
        }
    }

    /**
     * Checks whether the given claims represent an expired token.
     *
     * @param claims the JWT claims to check
     * @return true if the token's expiration date is before the current time
     */
    public boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
}
