package com.furniturebid.mockapi.exception;

public class InvalidCredentialsException extends ApiException {

    public InvalidCredentialsException(String message) {
        super(401, "INVALID_CREDENTIALS", message);
    }

    public InvalidCredentialsException() {
        this("Invalid email or password");
    }
}
