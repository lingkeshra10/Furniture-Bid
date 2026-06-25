package com.furniturebid.mockapi.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class SellerCompletedAuctionDto {

    private UUID id;
    private String title;
    private BigDecimal winningBid;
    private String winnerDisplayName;
    private boolean reserveMet;
    private Instant endedAt;

    public SellerCompletedAuctionDto() {
    }

    public SellerCompletedAuctionDto(UUID id, String title, BigDecimal winningBid,
                                      String winnerDisplayName, boolean reserveMet, Instant endedAt) {
        this.id = id;
        this.title = title;
        this.winningBid = winningBid;
        this.winnerDisplayName = winnerDisplayName;
        this.reserveMet = reserveMet;
        this.endedAt = endedAt;
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

    public BigDecimal getWinningBid() {
        return winningBid;
    }

    public void setWinningBid(BigDecimal winningBid) {
        this.winningBid = winningBid;
    }

    public String getWinnerDisplayName() {
        return winnerDisplayName;
    }

    public void setWinnerDisplayName(String winnerDisplayName) {
        this.winnerDisplayName = winnerDisplayName;
    }

    public boolean isReserveMet() {
        return reserveMet;
    }

    public void setReserveMet(boolean reserveMet) {
        this.reserveMet = reserveMet;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }
}
