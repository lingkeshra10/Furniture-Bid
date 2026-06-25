package com.furniturebid.mockapi.exception;

public class TokenExpiredException extends ApiException {

    public TokenExpiredException(String message) {
        super(401, "TOKEN_EXPIRED", message);
    }

    public TokenExpiredException() {
        this("Token has expired");
    }
}
