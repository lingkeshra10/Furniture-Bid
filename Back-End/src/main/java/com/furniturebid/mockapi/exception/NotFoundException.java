package com.furniturebid.mockapi.exception;

public class NotFoundException extends ApiException {

    public NotFoundException(String message) {
        super(404, "NOT_FOUND", message);
    }

    public NotFoundException(String resourceType, String id) {
        this(resourceType + " not found with id: " + id);
    }
}
