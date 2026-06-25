package com.furniturebid.mockapi.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class PaymentRecordDto {

    private UUID id;
    private UUID auctionId;
    private String currency;
    private String status;
    private BigDecimal amount;
    private Instant createdAt;

    public PaymentRecordDto() {
    }

    public PaymentRecordDto(UUID id, UUID auctionId, String currency,
                            String status, BigDecimal amount, Instant createdAt) {
        this.id = id;
        this.auctionId = auctionId;
        this.currency = currency;
        this.status = status;
        this.amount = amount;
        this.createdAt = createdAt;
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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
