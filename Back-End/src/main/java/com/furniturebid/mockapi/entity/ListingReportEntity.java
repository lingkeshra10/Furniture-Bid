package com.furniturebid.mockapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListingReportEntity {
    private UUID id;
    private UUID listingId;
    private String reason;
    private UUID reporterId;
    private String reporterDisplayName;
    private Instant reportDate;
}
