package com.furniturebid.mockapi.dto.response;

import java.math.BigDecimal;

public class TopSellerDto {

    private String displayName;
    private int completedAuctions;
    private BigDecimal totalRevenue;

    public TopSellerDto() {
    }

    public TopSellerDto(String displayName, int completedAuctions, BigDecimal totalRevenue) {
        this.displayName = displayName;
        this.completedAuctions = completedAuctions;
        this.totalRevenue = totalRevenue;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getCompletedAuctions() {
        return completedAuctions;
    }

    public void setCompletedAuctions(int completedAuctions) {
        this.completedAuctions = completedAuctions;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
