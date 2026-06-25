package com.furniturebid.mockapi.property;

import com.furniturebid.mockapi.exception.*;
import net.jqwik.api.*;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

// Feature: mock-api-service, Property 11: Error Response Structure Consistency
// Validates: Requirements 15.1, 15.2
class ErrorResponsePropertyTest {

    // ========== Property 1: All exceptions produce valid error responses ==========

    @Property
    void invalidCredentialsExceptionHasValidStructure(
            @ForAll("validMessages") String message
    ) {
        InvalidCredentialsException ex = new InvalidCredentialsException(message);

        assertThat(ex.getStatusCode()).isEqualTo(401);
        assertThat(ex.getErrorCode()).isNotEmpty();
        assertThat(ex.getMessage()).hasSizeBetween(1, 256);
    }

    @Property
    void unauthorizedExceptionHasValidStructure(
            @ForAll("validMessages") String message
    ) {
        UnauthorizedException ex = new UnauthorizedException(message);

        assertThat(ex.getStatusCode()).isEqualTo(401);
        assertThat(ex.getErrorCode()).isNotEmpty();
        assertThat(ex.getMessage()).hasSizeBetween(1, 256);
    }

    @Property
    void tokenExpiredExceptionHasValidStructure(
            @ForAll("validMessages") String message
    ) {
        TokenExpiredException ex = new TokenExpiredException(message);

        assertThat(ex.getStatusCode()).isEqualTo(401);
        assertThat(ex.getErrorCode()).isNotEmpty();
        assertThat(ex.getMessage()).hasSizeBetween(1, 256);
    }

    @Property
    void forbiddenExceptionHasValidStructure(
            @ForAll("validMessages") String message
    ) {
        ForbiddenException ex = new ForbiddenException(message);

        assertThat(ex.getStatusCode()).isEqualTo(403);
        assertThat(ex.getErrorCode()).isNotEmpty();
        assertThat(ex.getMessage()).hasSizeBetween(1, 256);
    }

    @Property
    void notFoundExceptionHasValidStructure(
            @ForAll("validMessages") String message
    ) {
        NotFoundException ex = new NotFoundException(message);

        assertThat(ex.getStatusCode()).isEqualTo(404);
        assertThat(ex.getErrorCode()).isNotEmpty();
        assertThat(ex.getMessage()).hasSizeBetween(1, 256);
    }

    @Property
    void conflictExceptionHasValidStructure(
            @ForAll("validMessages") String message
    ) {
        ConflictException ex = new ConflictException(message);

        assertThat(ex.getStatusCode()).isEqualTo(409);
        assertThat(ex.getErrorCode()).isNotEmpty();
        assertThat(ex.getMessage()).hasSizeBetween(1, 256);
    }

    @Property
    void bidTooLowExceptionHasValidStructure(
            @ForAll("validMessages") String message
    ) {
        BidTooLowException ex = new BidTooLowException(message);

        assertThat(ex.getStatusCode()).isEqualTo(422);
        assertThat(ex.getErrorCode()).isNotEmpty();
        assertThat(ex.getMessage()).hasSizeBetween(1, 256);
    }

    @Property
    void auctionEndedExceptionHasValidStructure(
            @ForAll("validMessages") String message
    ) {
        AuctionEndedException ex = new AuctionEndedException(message);

        assertThat(ex.getStatusCode()).isEqualTo(422);
        assertThat(ex.getErrorCode()).isNotEmpty();
        assertThat(ex.getMessage()).hasSizeBetween(1, 256);
    }

    // ========== Property 2: ValidationException includes fieldErrors ==========

    @Property
    void validationExceptionIncludesFieldErrors(
            @ForAll("validMessages") String message,
            @ForAll("fieldErrorMaps") Map<String, String> fieldErrors
    ) {
        ValidationException ex = new ValidationException(message, fieldErrors);

        assertThat(ex.getStatusCode()).isEqualTo(400);
        assertThat(ex.getErrorCode()).isEqualTo("INVALID_REQUEST");
        assertThat(ex.getMessage()).hasSizeBetween(1, 256);
        assertThat(ex.getFieldErrors()).isNotNull();
        assertThat(ex.getFieldErrors()).isNotEmpty();
    }

    // ========== Providers ==========

    @Provide
    Arbitrary<String> validMessages() {
        // Generate random strings of length 1-256 using printable ASCII characters
        return Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(256)
                .alpha()
                .ofMinLength(1);
    }

    @Provide
    Arbitrary<Map<String, String>> fieldErrorMaps() {
        // Generate maps with 1-5 entries, using field name keys and error message values
        Arbitrary<String> fieldNames = Arbitraries.of(
                "email", "password", "displayName", "title", "description",
                "category", "condition", "amount", "location", "price"
        );
        Arbitrary<String> errorMessages = Arbitraries.of(
                "is required", "is too short", "is too long", "is invalid",
                "must be positive", "exceeds maximum", "format is incorrect"
        );

        return Arbitraries.maps(fieldNames, errorMessages)
                .ofMinSize(1)
                .ofMaxSize(5);
    }
}
