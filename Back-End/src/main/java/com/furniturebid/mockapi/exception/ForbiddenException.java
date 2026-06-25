package com.furniturebid.mockapi.exception;

public class ForbiddenException extends ApiException {

    public ForbiddenException(String message) {
        super(403, "FORBIDDEN", message);
    }

    public ForbiddenException() {
        this("You do not have permission to access this resource");
    }
}
