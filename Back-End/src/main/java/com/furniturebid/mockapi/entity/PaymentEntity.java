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
public class PaymentEntity {
    private UUID id;
    private UUID userId;
    private UUID auctionId;
    private String clientSecret;
    private BigDecimal amount;
    private String currency;
    private String status;
    private Instant createdAt;
}
