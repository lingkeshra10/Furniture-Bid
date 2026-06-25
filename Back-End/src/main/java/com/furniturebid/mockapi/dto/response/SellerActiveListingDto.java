package com.furniturebid.mockapi.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public class SellerActiveListingDto {

    private UUID id;
    private String title;
    private BigDecimal currentBid;
    private int bidCount;
    private long timeRemaining;

    public SellerActiveListingDto() {
    }

    public SellerActiveListingDto(UUID id, String title, BigDecimal currentBid,
                                   int bidCount, long timeRemaining) {
        this.id = id;
        this.title = title;
        this.currentBid = currentBid;
        this.bidCount = bidCount;
        this.timeRemaining = timeRemaining;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getCurrentBid() {
        return currentBid;
    }

    public void setCurrentBid(BigDecimal currentBid) {
        this.currentBid = currentBid;
    }

    public int getBidCount() {
        return bidCount;
    }

    public void setBidCount(int bidCount) {
        this.bidCount = bidCount;
    }

    public long getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(long timeRemaining) {
        this.timeRemaining = timeRemaining;
    }
}
