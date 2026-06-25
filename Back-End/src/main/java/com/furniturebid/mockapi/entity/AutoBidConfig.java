package com.furniturebid.mockapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AutoBidConfig {
    private UUID userId;
    private UUID auctionId;
    private BigDecimal maxAmount;
}
