package com.furniturebid.mockapi.exception;

public class AuctionEndedException extends ApiException {

    public AuctionEndedException(String message) {
        super(422, "AUCTION_ENDED", message);
    }

    public AuctionEndedException() {
        this("This auction has already ended");
    }
}
