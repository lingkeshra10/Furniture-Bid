package com.furniturebid.mockapi.exception;

public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String message) {
        super(401, "UNAUTHORIZED", message);
    }

    public UnauthorizedException() {
        this("Authentication is required to access this resource");
    }
}
