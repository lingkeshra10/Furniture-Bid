package com.furniturebid.mockapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BidEntity {
    private UUID id;
    private UUID auctionId;
    private UUID bidderId;
    private String bidderAlias;
    private BigDecimal amount;
    private Instant timestamp;
}
