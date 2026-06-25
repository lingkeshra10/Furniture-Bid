package com.furniturebid.mockapi.service;

import com.furniturebid.mockapi.dto.response.FurnitureListingDto;
import com.furniturebid.mockapi.dto.response.FurnitureListingSummaryDto;
import com.furniturebid.mockapi.dto.response.PaginatedResponse;
import com.furniturebid.mockapi.entity.Dimensions;
import com.furniturebid.mockapi.entity.FurnitureListingEntity;
import com.furniturebid.mockapi.entity.ListingReportEntity;
import com.furniturebid.mockapi.entity.UserEntity;
import com.furniturebid.mockapi.exception.ForbiddenException;
import com.furniturebid.mockapi.exception.NotFoundException;
import com.furniturebid.mockapi.store.MockDataStore;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service handling furniture listing operations: catalog browsing with filtering/sorting/pagination,
 * listing detail retrieval, listing creation, flagging, and deletion.
 */
@Service
public class FurnitureService {

    private static final Set<String> VALID_CATEGORIES = Set.of(
            "sofa", "dining-table", "office-chair", "wardrobe",
            "bed-frame", "coffee-table", "cabinet", "bookshelf"
    );

    private static final Set<String> VALID_CONDITIONS = Set.of(
            "new", "like-new", "good", "fair", "poor"
    );

    private static final Set<String> VALID_SORT_VALUES = Set.of(
            "ending-soonest", "price-low-high", "price-high-low", "newest"
    );

    private static final String DEFAULT_SORT = "ending-soonest";

    private final MockDataStore dataStore;

    public FurnitureService(MockDataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Retrieves a paginated, filtered, and sorted catalog of active furniture listings.
     *
     * @param category  comma-separated category filter (nullable)
     * @param condition comma-separated condition filter (nullable)
     * @param priceMin  minimum price filter on currentBid (nullable)
     * @param priceMax  maximum price filter on currentBid (nullable)
     * @param location  location substring filter, case-insensitive (nullable)
     * @param sort      sort parameter (nullable, defaults to "ending-soonest")
     * @param page      page number (nullable, defaults handled by paginate)
     * @param pageSize  page size (nullable, defaults handled by paginate)
     * @return paginated response of FurnitureListingSummaryDto
     */
    public PaginatedResponse<FurnitureListingSummaryDto> getCatalog(
            String category, String condition,
            BigDecimal priceMin, BigDecimal priceMax,
            String location, String sort,
            Integer page, Integer pageSize) {

        // If priceMin > priceMax, return empty result
        if (priceMin != null && priceMax != null && priceMin.compareTo(priceMax) > 0) {
            return new PaginatedResponse<>(
                    Collections.emptyList(), 0,
                    page != null ? page : 1,
                    pageSize != null ? pageSize : 20,
                    false
            );
        }

        // Start with all active listings
        List<FurnitureListingEntity> filtered = dataStore.getListings().values().stream()
                .filter(listing -> "active".equals(listing.getStatus()))
                .collect(Collectors.toList());

        // Filter by categories (comma-separated, ignore invalid values)
        if (category != null && !category.isBlank()) {
            Set<String> requestedCategories = Arrays.stream(category.split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(VALID_CATEGORIES::contains)
                    .collect(Collectors.toSet());

            if (!requestedCategories.isEmpty()) {
                filtered = filtered.stream()
                        .filter(listing -> requestedCategories.contains(listing.getCategory()))
                        .collect(Collectors.toList());
            }
        }

        // Filter by conditions (comma-separated, ignore invalid values)
        if (condition != null && !condition.isBlank()) {
            Set<String> requestedConditions = Arrays.stream(condition.split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(VALID_CONDITIONS::contains)
                    .collect(Collectors.toSet());

            if (!requestedConditions.isEmpty()) {
                filtered = filtered.stream()
                        .filter(listing -> requestedConditions.contains(listing.getCondition()))
                        .collect(Collectors.toList());
            }
        }

        // Filter by priceMin on currentBid
        if (priceMin != null) {
            filtered = filtered.stream()
                    .filter(listing -> listing.getCurrentBid() != null
                            && listing.getCurrentBid().compareTo(priceMin) >= 0)
                    .collect(Collectors.toList());
        }

        // Filter by priceMax on currentBid
        if (priceMax != null) {
            filtered = filtered.stream()
                    .filter(listing -> listing.getCurrentBid() != null
                            && listing.getCurrentBid().compareTo(priceMax) <= 0)
                    .collect(Collectors.toList());
        }

        // Filter by location (case-insensitive contains)
        if (location != null && !location.isBlank()) {
            String locationLower = location.toLowerCase();
            filtered = filtered.stream()
                    .filter(listing -> listing.getLocation() != null
                            && listing.getLocation().toLowerCase().contains(locationLower))
                    .collect(Collectors.toList());
        }

        // Determine sort order (default to "ending-soonest" for invalid values)
        String resolvedSort = (sort != null && VALID_SORT_VALUES.contains(sort)) ? sort : DEFAULT_SORT;

        // Sort
        Comparator<FurnitureListingEntity> comparator = getComparator(resolvedSort);
        filtered.sort(comparator);

        // Map to summary DTOs
        Instant now = Instant.now();
        List<FurnitureListingSummaryDto> summaries = filtered.stream()
                .map(entity -> toSummaryDto(entity, now))
                .collect(Collectors.toList());

        // Paginate
        return dataStore.paginate(summaries, page, pageSize);
    }

    /**
     * Retrieves a single listing by its ID.
     *
     * @param id the listing ID
     * @return FurnitureListingDto with all fields
     * @throws NotFoundException if listing not found
     */
    public FurnitureListingDto getListingById(String id) {
        FurnitureListingEntity entity = dataStore.getListingById(id)
                .orElseThrow(() -> new NotFoundException("Listing", id));

        return toListingDto(entity);
    }

    /**
     * Creates a new furniture listing.
     *
     * @param title          the listing title
     * @param description    the listing description
     * @param category       the furniture category
     * @param condition      the furniture condition
     * @param dimensions     the furniture dimensions (nullable)
     * @param startingPrice  the starting price
     * @param reservePrice   the reserve price (nullable)
     * @param auctionEndDate the auction end date
     * @param images         list of image URLs
     * @param sellerId       the seller's user ID
     * @return FurnitureListingDto of the created listing
     */
     public FurnitureListingDto createListing(String title, String description, String category,
                                              String condition, Dimensions dimensions,
                                              BigDecimal startingPrice, BigDecimal reservePrice,
                                              Instant auctionEndDate, List<String> images,
                                              String sellerId) {
        UUID listingId = UUID.randomUUID();

        // Get seller info from data store
        String sellerDisplayName = "Unknown Seller";
        double sellerRating = 0.0;
        Optional<UserEntity> sellerOpt = dataStore.getUserById(sellerId);
        if (sellerOpt.isPresent()) {
            sellerDisplayName = sellerOpt.get().getDisplayName();
            // For mock, use a default rating since UserEntity doesn't have a rating field
            sellerRating = 4.5;
        }

        FurnitureListingEntity entity = new FurnitureListingEntity(
                listingId,
                title,
                description,
                category,
                condition,
                null,   // brand
                null,   // material
                dimensions,
                null,   // weight
                null,   // location
                images != null ? images : Collections.emptyList(),
                startingPrice,
                reservePrice,
                startingPrice,  // currentBid = startingPrice initially
                0,              // bidCount = 0
                auctionEndDate,
                "active",
                UUID.fromString(sellerId),
                sellerDisplayName,
                sellerRating,
                Instant.now()
        );

        dataStore.getListings().put(listingId.toString(), entity);

        return toListingDto(entity);
    }

     /**
     * Flags a listing for review.
     *
     * @param listingId  the listing ID to flag
     * @param reason     the reason for flagging
     * @param reporterId the reporter's user ID
     * @throws NotFoundException if listing not found
     */
    public void flagListing(String listingId, String reason, String reporterId) {
        FurnitureListingEntity listing = dataStore.getListingById(listingId)
                .orElseThrow(() -> new NotFoundException("Listing", listingId));

        // Update status to flagged
        listing.setStatus("flagged");

        // Get reporter display name
        String reporterDisplayName = "Unknown User";
        Optional<UserEntity> reporterOpt = dataStore.getUserById(reporterId);
        if (reporterOpt.isPresent()) {
            reporterDisplayName = reporterOpt.get().getDisplayName();
        }

        // Create report entity
        ListingReportEntity report = new ListingReportEntity(
                UUID.randomUUID(),
                UUID.fromString(listingId),
                reason,
                UUID.fromString(reporterId),
                reporterDisplayName,
                Instant.now()
        );

        // Add to reports collection
        dataStore.getReports()
                .computeIfAbsent(listingId, k -> new ArrayList<>())
                .add(report);
    }

    /**
     * Deletes (removes) a listing. Only the owner or an admin can delete.
     *
     * @param listingId the listing ID to delete
     * @param userId    the requesting user's ID
     * @param userRole  the requesting user's role
     * @throws NotFoundException  if listing not found
     * @throws ForbiddenException if user is not the owner and not an admin
     */
    public void deleteListing(String listingId, String userId, String userRole) {
        FurnitureListingEntity listing = dataStore.getListingById(listingId)
                .orElseThrow(() -> new NotFoundException("Listing", listingId));

        // Check ownership: userId must match sellerId OR userRole must be "admin"
        boolean isOwner = userId != null && userId.equals(listing.getSellerId().toString());
        boolean isAdmin = "admin".equalsIgnoreCase(userRole);

        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("You do not have permission to delete this listing");
        }

        // Update status to removed
        listing.setStatus("removed");
    }

    // ========== Private Helper Methods ==========

    private Comparator<FurnitureListingEntity> getComparator(String sort) {
        switch (sort) {
            case "price-low-high":
                return Comparator.comparing(
                        FurnitureListingEntity::getCurrentBid,
                        Comparator.nullsLast(Comparator.naturalOrder())
                );
            case "price-high-low":
                return Comparator.comparing(
                        FurnitureListingEntity::getCurrentBid,
                        Comparator.nullsLast(Comparator.reverseOrder())
                );
            case "newest":
                return Comparator.comparing(
                        FurnitureListingEntity::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                );
            case "ending-soonest":
            default:
                return Comparator.comparing(
                        FurnitureListingEntity::getAuctionEndDate,
                        Comparator.nullsLast(Comparator.naturalOrder())
                );
        }
    }

    private FurnitureListingSummaryDto toSummaryDto(FurnitureListingEntity entity, Instant now) {
        String thumbnailUrl = (entity.getImages() != null && !entity.getImages().isEmpty())
                ? entity.getImages().get(0)
                : null;

        long timeRemaining = (entity.getAuctionEndDate() != null)
                ? entity.getAuctionEndDate().toEpochMilli() - now.toEpochMilli()
                : 0;

        return new FurnitureListingSummaryDto(
                entity.getId(),
                entity.getTitle(),
                thumbnailUrl,
                entity.getCondition(),
                entity.getCategory(),
                entity.getCurrentBid(),
                timeRemaining
        );
    }

    private FurnitureListingDto toListingDto(FurnitureListingEntity entity) {
        FurnitureListingDto dto = new FurnitureListingDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setCategory(entity.getCategory());
        dto.setCondition(entity.getCondition());
        dto.setBrand(entity.getBrand());
        dto.setMaterial(entity.getMaterial());
        dto.setDimensions(entity.getDimensions());
        dto.setWeight(entity.getWeight());
        dto.setLocation(entity.getLocation());
        dto.setImages(entity.getImages());
        dto.setStartingPrice(entity.getStartingPrice());
        dto.setReservePrice(entity.getReservePrice());
        dto.setCurrentBid(entity.getCurrentBid());
        dto.setBidCount(entity.getBidCount());
        dto.setAuctionEndDate(entity.getAuctionEndDate());
        dto.setStatus(entity.getStatus());
        dto.setSellerId(entity.getSellerId());
        dto.setSellerDisplayName(entity.getSellerDisplayName());
        dto.setSellerRating(entity.getSellerRating());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
