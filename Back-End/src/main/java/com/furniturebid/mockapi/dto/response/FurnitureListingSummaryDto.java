package com.furniturebid.mockapi.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public class FurnitureListingSummaryDto {

    private UUID id;
    private String title;
    private String thumbnailUrl;
    private String condition;
    private String category;
    private BigDecimal currentBid;
    private long timeRemaining;

    public FurnitureListingSummaryDto() {
    }

    public FurnitureListingSummaryDto(UUID id, String title, String thumbnailUrl,
                                      String condition, String category,
                                      BigDecimal currentBid, long timeRemaining) {
        this.id = id;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.condition = condition;
        this.category = category;
        this.currentBid = currentBid;
        this.timeRemaining = timeRemaining;
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

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getCurrentBid() {
        return currentBid;
    }

    public void setCurrentBid(BigDecimal currentBid) {
        this.currentBid = currentBid;
    }

    public long getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(long timeRemaining) {
        this.timeRemaining = timeRemaining;
    }
}
