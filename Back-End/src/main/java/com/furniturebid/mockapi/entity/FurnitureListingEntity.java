package com.furniturebid.mockapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FurnitureListingEntity {
    private UUID id;
    private String title;
    private String description;
    private String category;
    private String condition;
    private String brand;
    private String material;
    private Dimensions dimensions;
    private Double weight;
    private String location;
    private List<String> images;
    private BigDecimal startingPrice;
    private BigDecimal reservePrice;
    private BigDecimal currentBid;
    private int bidCount;
    private Instant auctionEndDate;
    private String status;
    private UUID sellerId;
    private String sellerDisplayName;
    private double sellerRating;
    private Instant createdAt;
}
