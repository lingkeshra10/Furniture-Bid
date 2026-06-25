package com.furniturebid.mockapi.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public class AdminListingRowDto {

    private UUID id;
    private String title;
    private String sellerDisplayName;
    private String status;
    private BigDecimal currentBid;
    private int reportCount;

    public AdminListingRowDto() {
    }

    public AdminListingRowDto(UUID id, String title, String sellerDisplayName,
                              String status, BigDecimal currentBid, int reportCount) {
        this.id = id;
        this.title = title;
        this.sellerDisplayName = sellerDisplayName;
        this.status = status;
        this.currentBid = currentBid;
        this.reportCount = reportCount;
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

    public String getSellerDisplayName() {
        return sellerDisplayName;
    }

    public void setSellerDisplayName(String sellerDisplayName) {
        this.sellerDisplayName = sellerDisplayName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getCurrentBid() {
        return currentBid;
    }

    public void setCurrentBid(BigDecimal currentBid) {
        this.currentBid = currentBid;
    }

    public int getReportCount() {
        return reportCount;
    }

    public void setReportCount(int reportCount) {
        this.reportCount = reportCount;
    }
}
