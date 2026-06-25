package com.furniturebid.mockapi.dto.response;

import java.math.BigDecimal;

public class AnalyticsSummaryDto {

    private int totalUsers;
    private int activeAuctions;
    private int completedAuctions;
    private BigDecimal totalRevenue;

    public AnalyticsSummaryDto() {
    }

    public AnalyticsSummaryDto(int totalUsers, int activeAuctions,
                               int completedAuctions, BigDecimal totalRevenue) {
        this.totalUsers = totalUsers;
        this.activeAuctions = activeAuctions;
        this.completedAuctions = completedAuctions;
        this.totalRevenue = totalRevenue;
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getActiveAuctions() {
        return activeAuctions;
    }

    public void setActiveAuctions(int activeAuctions) {
        this.activeAuctions = activeAuctions;
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
