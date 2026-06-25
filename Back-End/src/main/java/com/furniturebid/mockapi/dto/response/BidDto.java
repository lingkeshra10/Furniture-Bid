package com.furniturebid.mockapi.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class BidDto {

    private UUID id;
    private UUID auctionId;
    private UUID bidderId;
    private String bidderAlias;
    private BigDecimal amount;
    private Instant timestamp;

    public BidDto() {
    }

    public BidDto(UUID id, UUID auctionId, UUID bidderId, String bidderAlias,
                  BigDecimal amount, Instant timestamp) {
        this.id = id;
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.bidderAlias = bidderAlias;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(UUID auctionId) {
        this.auctionId = auctionId;
    }

    public UUID getBidderId() {
        return bidderId;
    }

    public void setBidderId(UUID bidderId) {
        this.bidderId = bidderId;
    }

    public String getBidderAlias() {
        return bidderAlias;
    }

    public void setBidderAlias(String bidderAlias) {
        this.bidderAlias = bidderAlias;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
