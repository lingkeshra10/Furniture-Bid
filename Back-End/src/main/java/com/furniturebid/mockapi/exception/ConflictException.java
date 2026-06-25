package com.furniturebid.mockapi.exception;

public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(409, "CONFLICT", message);
    }
}
