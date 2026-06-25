package com.furniturebid.mockapi.dto.response;

public class PlaceBidResponse {

    private boolean success;
    private BidDto bid;
    private String error;

    public PlaceBidResponse() {
    }

    public PlaceBidResponse(boolean success, BidDto bid, String error) {
        this.success = success;
        this.bid = bid;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public BidDto getBid() {
        return bid;
    }

    public void setBid(BidDto bid) {
        this.bid = bid;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
