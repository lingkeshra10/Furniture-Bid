package com.furniturebid.mockapi.service;

import com.furniturebid.mockapi.dto.response.*;
import com.furniturebid.mockapi.entity.FurnitureListingEntity;
import com.furniturebid.mockapi.entity.ListingReportEntity;
import com.furniturebid.mockapi.entity.UserEntity;
import com.furniturebid.mockapi.exception.NotFoundException;
import com.furniturebid.mockapi.exception.ValidationException;
import com.furniturebid.mockapi.store.MockDataStore;
import com.furniturebid.mockapi.util.PaginationHelper;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service handling admin operations including user management, listing management,
 * reports, and analytics.
 */
@Service
public class AdminService {

    private static final List<String> FURNITURE_CATEGORIES = Arrays.asList(
            "sofa", "dining-table", "office-chair", "wardrobe",
            "bed-frame", "coffee-table", "cabinet", "bookshelf"
    );

    private final MockDataStore dataStore;

    public AdminService(MockDataStore dataStore) {
        this.dataStore = dataStore;
    }

    // ========== User Management ==========

    /**
     * Retrieves a paginated list of all users mapped to AdminUserRowDto.
     *
     * @param page     the page number (1-based)
     * @param pageSize the number of items per page
     * @return paginated response of admin user rows
     */
    public PaginatedResponse<AdminUserRowDto> getUsers(Integer page, Integer pageSize) {
        List<AdminUserRowDto> userRows = dataStore.getUsers().values().stream()
                .map(this::toAdminUserRowDto)
                .collect(Collectors.toList());

        return PaginationHelper.paginate(userRows, page, pageSize);
    }

    /**
     * Suspends a user by setting their status to "suspended".
     *
     * @param userId the user ID
     * @throws NotFoundException if the user does not exist
     */
    public void suspendUser(String userId) {
        UserEntity user = dataStore.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));
        user.setStatus("suspended");
    }

    /**
     * Activates a user by setting their status to "active".
     *
     * @param userId the user ID
     * @throws NotFoundException if the user does not exist
     */
    public void activateUser(String userId) {
        UserEntity user = dataStore.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));
        user.setStatus("active");
    }

    /**
     * Deletes a user by setting their status to "deleted".
     *
     * @param userId the user ID
     * @throws NotFoundException if the user does not exist
     */
    public void deleteUser(String userId) {
        UserEntity user = dataStore.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));
        user.setStatus("deleted");
    }

    // ========== Listing Management ==========

    /**
     * Retrieves a paginated list of all listings mapped to AdminListingRowDto.
     * Report count is calculated by counting reports for each listing.
     *
     * @param page     the page number (1-based)
     * @param pageSize the number of items per page
     * @return paginated response of admin listing rows
     */
    public PaginatedResponse<AdminListingRowDto> getListings(Integer page, Integer pageSize) {
        List<AdminListingRowDto> listingRows = dataStore.getListings().values().stream()
                .map(this::toAdminListingRowDto)
                .collect(Collectors.toList());

        return PaginationHelper.paginate(listingRows, page, pageSize);
    }

    /**
     * Removes a listing by setting its status to "removed".
     *
     * @param listingId the listing ID
     * @throws NotFoundException if the listing does not exist
     */
    public void removeListing(String listingId) {
        FurnitureListingEntity listing = dataStore.getListingById(listingId)
                .orElseThrow(() -> new NotFoundException("Listing", listingId));
        listing.setStatus("removed");
    }

    /**
     * Flags a listing by setting its status to "flagged".
     *
     * @param listingId the listing ID
     * @throws NotFoundException if the listing does not exist
     */
    public void flagListing(String listingId) {
        FurnitureListingEntity listing = dataStore.getListingById(listingId)
                .orElseThrow(() -> new NotFoundException("Listing", listingId));
        listing.setStatus("flagged");
    }

    // ========== Reports ==========

    /**
     * Retrieves a paginated list of all reports across all listings,
     * sorted by reportDate descending.
     *
     * @param page     the page number (1-based)
     * @param pageSize the number of items per page
     * @return paginated response of listing report DTOs
     */
    public PaginatedResponse<ListingReportDto> getReports(Integer page, Integer pageSize) {
        List<ListingReportDto> allReports = dataStore.getReports().values().stream()
                .flatMap(List::stream)
                .sorted(Comparator.comparing(ListingReportEntity::getReportDate).reversed())
                .map(this::toListingReportDto)
                .collect(Collectors.toList());

        return PaginationHelper.paginate(allReports, page, pageSize);
    }

    // ========== Analytics ==========

    /**
     * Returns an analytics summary with mock values computed from the data store.
     *
     * @param startDate the start date (ISO 8601)
     * @param endDate   the end date (ISO 8601)
     * @return analytics summary DTO
     * @throws ValidationException if date parameters are invalid
     */
    public AnalyticsSummaryDto getAnalyticsSummary(String startDate, String endDate) {
        validateDateRange(startDate, endDate);

        int totalUsers = dataStore.getUsers().size();

        long activeAuctions = dataStore.getListings().values().stream()
                .filter(l -> "active".equals(l.getStatus()))
                .count();

        long completedAuctions = dataStore.getListings().values().stream()
                .filter(l -> "ended".equals(l.getStatus()))
                .count();

        BigDecimal totalRevenue = dataStore.getListings().values().stream()
                .filter(l -> "ended".equals(l.getStatus()))
                .map(FurnitureListingEntity::getCurrentBid)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new AnalyticsSummaryDto(
                totalUsers,
                (int) activeAuctions,
                (int) completedAuctions,
                totalRevenue
        );
    }

    /**
     * Generates one AuctionTrendDto per calendar day in the [startDate, endDate] range.
     * Values are deterministic based on date hashCode for consistency.
     *
     * @param startDate the start date (ISO 8601)
     * @param endDate   the end date (ISO 8601)
     * @return list of auction trend entries, one per day
     * @throws ValidationException if date parameters are invalid
     */
    public List<AuctionTrendDto> getAuctionTrends(String startDate, String endDate) {
        validateDateRange(startDate, endDate);

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        List<AuctionTrendDto> trends = new ArrayList<>();
        LocalDate current = start;

        while (!current.isAfter(end)) {
            int hash = current.toString().hashCode();
            int auctionsCreated = Math.abs(hash % 6);           // 0-5
            int auctionsCompleted = Math.abs((hash / 6) % 4);   // 0-3

            trends.add(new AuctionTrendDto(
                    current.toString(),
                    auctionsCreated,
                    auctionsCompleted
            ));
            current = current.plusDays(1);
        }

        return trends;
    }

    /**
     * Returns exactly 8 CategoryDistributionDto entries, one for each FurnitureCategory.
     * Counts the number of listings in each category.
     *
     * @param startDate the start date (ISO 8601)
     * @param endDate   the end date (ISO 8601)
     * @return list of exactly 8 category distribution entries
     * @throws ValidationException if date parameters are invalid
     */
    public List<CategoryDistributionDto> getCategoryDistribution(String startDate, String endDate) {
        validateDateRange(startDate, endDate);

        Map<String, Integer> categoryCounts = new LinkedHashMap<>();
        for (String category : FURNITURE_CATEGORIES) {
            categoryCounts.put(category, 0);
        }

        for (FurnitureListingEntity listing : dataStore.getListings().values()) {
            String category = listing.getCategory();
            if (categoryCounts.containsKey(category)) {
                categoryCounts.merge(category, 1, Integer::sum);
            }
        }

        return categoryCounts.entrySet().stream()
                .map(entry -> new CategoryDistributionDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

     /**
     * Returns at most 10 top sellers sorted by totalRevenue descending.
     * Counts completed auctions and sums revenue from ended listings per seller.
     * Filters out sellers with 0 completed auctions.
     *
     * @param startDate the start date (ISO 8601)
     * @param endDate   the end date (ISO 8601)
     * @return list of at most 10 top seller entries
     * @throws ValidationException if date parameters are invalid
     */
    public List<TopSellerDto> getTopSellers(String startDate, String endDate) {
        validateDateRange(startDate, endDate);

        // Group ended listings by sellerId
        Map<java.util.UUID, List<FurnitureListingEntity>> endedBySeller = dataStore.getListings().values().stream()
                .filter(l -> "ended".equals(l.getStatus()))
                .collect(Collectors.groupingBy(FurnitureListingEntity::getSellerId));

        List<TopSellerDto> topSellers = new ArrayList<>();

        for (Map.Entry<java.util.UUID, List<FurnitureListingEntity>> entry : endedBySeller.entrySet()) {
            java.util.UUID sellerId = entry.getKey();
            List<FurnitureListingEntity> sellerListings = entry.getValue();

            int completedAuctions = sellerListings.size();
            if (completedAuctions == 0) {
                continue;
            }

            BigDecimal totalRevenue = sellerListings.stream()
                    .map(FurnitureListingEntity::getCurrentBid)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Get seller display name
            String displayName = dataStore.getUserById(sellerId.toString())
                    .map(UserEntity::getDisplayName)
                    .orElse("Unknown Seller");

            topSellers.add(new TopSellerDto(displayName, completedAuctions, totalRevenue));
        }

        // Sort by totalRevenue descending and limit to 10
        return topSellers.stream()
                .sorted(Comparator.comparing(TopSellerDto::getTotalRevenue).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    // ========== Validation Helpers ==========

    /**
     * Validates date range parameters: must be valid ISO 8601 dates and startDate <= endDate.
     *
     * @param startDate the start date string
     * @param endDate   the end date string
     * @throws ValidationException if dates are invalid or startDate > endDate
     */
    private void validateDateRange(String startDate, String endDate) {
        Map<String, String> fieldErrors = new HashMap<>();

        LocalDate parsedStart = null;
        LocalDate parsedEnd = null;

        if (startDate == null || startDate.isBlank()) {
            fieldErrors.put("startDate", "Start date is required");
        } else {
            try {
                parsedStart = LocalDate.parse(startDate);
            } catch (DateTimeParseException e) {
                fieldErrors.put("startDate", "Start date must be a valid ISO 8601 date (yyyy-MM-dd)");
            }
        }

        if (endDate == null || endDate.isBlank()) {
            fieldErrors.put("endDate", "End date is required");
        } else {
            try {
                parsedEnd = LocalDate.parse(endDate);
            } catch (DateTimeParseException e) {
                fieldErrors.put("endDate", "End date must be a valid ISO 8601 date (yyyy-MM-dd)");
            }
        }

        if (!fieldErrors.isEmpty()) {
            throw new ValidationException("Invalid date range parameters", fieldErrors);
        }

        if (parsedStart.isAfter(parsedEnd)) {
            fieldErrors.put("startDate", "Start date must be on or before end date");
            throw new ValidationException("Invalid date range parameters", fieldErrors);
        }
    }

    // ========== Mapping Helpers ==========

    private AdminUserRowDto toAdminUserRowDto(UserEntity user) {
        return new AdminUserRowDto(
                user.getId(),
                user.getDisplayName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt()  // registeredAt = createdAt
        );
    }

    private AdminListingRowDto toAdminListingRowDto(FurnitureListingEntity listing) {
        // Count reports for this listing
        List<ListingReportEntity> listingReports = dataStore.getReports()
                .getOrDefault(listing.getId().toString(), Collections.emptyList());
        int reportCount = listingReports.size();

        return new AdminListingRowDto(
                listing.getId(),
                listing.getTitle(),
                listing.getSellerDisplayName(),
                listing.getStatus(),
                listing.getCurrentBid(),
                reportCount
        );
    }

    private ListingReportDto toListingReportDto(ListingReportEntity report) {
        return new ListingReportDto(
                report.getId(),
                report.getListingId(),
                report.getReason(),
                report.getReporterDisplayName(),
                report.getReportDate()
        );
    }
}
