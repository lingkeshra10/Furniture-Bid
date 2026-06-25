package com.furniturebid.mockapi.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class AutoBidRequest {

    @NotNull
    private BigDecimal maxAmount;

    public AutoBidRequest() {
    }

    public AutoBidRequest(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }
}
