package com.furniturebid.mockapi.exception;

public class BidTooLowException extends ApiException {

    public BidTooLowException(String message) {
        super(422, "BID_TOO_LOW", message);
    }
}
