package com.furniturebid.mockapi.dto.response;

public class AuctionTrendDto {

    private String date;
    private int auctionsCreated;
    private int auctionsCompleted;

    public AuctionTrendDto() {
    }

    public AuctionTrendDto(String date, int auctionsCreated, int auctionsCompleted) {
        this.date = date;
        this.auctionsCreated = auctionsCreated;
        this.auctionsCompleted = auctionsCompleted;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getAuctionsCreated() {
        return auctionsCreated;
    }

    public void setAuctionsCreated(int auctionsCreated) {
        this.auctionsCreated = auctionsCreated;
    }

    public int getAuctionsCompleted() {
        return auctionsCompleted;
    }

    public void setAuctionsCompleted(int auctionsCompleted) {
        this.auctionsCompleted = auctionsCompleted;
    }
}
