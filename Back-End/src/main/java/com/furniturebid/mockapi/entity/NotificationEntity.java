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
public class NotificationEntity {
    private UUID id;
    private UUID userId;
    private String type;
    private String title;
    private String message;
    private UUID auctionId;
    private boolean isRead;
    private Instant createdAt;
}
