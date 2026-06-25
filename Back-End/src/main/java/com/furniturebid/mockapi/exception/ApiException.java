package com.furniturebid.mockapi.exception;

/**
 * Base exception class for all custom API exceptions.
 * Carries statusCode, errorCode, and message for standardized error responses.
 */
public abstract class ApiException extends RuntimeException {

    private final int statusCode;
    private final String errorCode;

    protected ApiException(int statusCode, String errorCode, String message) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
