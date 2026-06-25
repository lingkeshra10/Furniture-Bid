package com.furniturebid.mockapi.dto.response;

import java.time.Instant;
import java.util.UUID;

public class ListingReportDto {

    private UUID id;
    private UUID listingId;
    private String reason;
    private String reporterDisplayName;
    private Instant reportDate;

    public ListingReportDto() {
    }

    public ListingReportDto(UUID id, UUID listingId, String reason,
                            String reporterDisplayName, Instant reportDate) {
        this.id = id;
        this.listingId = listingId;
        this.reason = reason;
        this.reporterDisplayName = reporterDisplayName;
        this.reportDate = reportDate;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getListingId() {
        return listingId;
    }

    public void setListingId(UUID listingId) {
        this.listingId = listingId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReporterDisplayName() {
        return reporterDisplayName;
    }

    public void setReporterDisplayName(String reporterDisplayName) {
        this.reporterDisplayName = reporterDisplayName;
    }

    public Instant getReportDate() {
        return reportDate;
    }

    public void setReportDate(Instant reportDate) {
        this.reportDate = reportDate;
    }
}
