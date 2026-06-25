package com.furniturebid.mockapi.store;

import com.furniturebid.mockapi.dto.response.PaginatedResponse;
import com.furniturebid.mockapi.entity.*;
import com.furniturebid.mockapi.util.PaginationHelper;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory data store for the mock API service.
 * Uses ConcurrentHashMap collections for thread-safe access.
 */
@Component
public class MockDataStore {

    private final ConcurrentHashMap<String, UserEntity> users;
    private final ConcurrentHashMap<String, FurnitureListingEntity> listings;
    private final ConcurrentHashMap<String, List<BidEntity>> bidsByAuction;
    private final ConcurrentHashMap<String, List<String>> watchlistByUser;
    private final ConcurrentHashMap<String, List<NotificationEntity>> notificationsByUser;
    private final ConcurrentHashMap<String, PaymentEntity> payments;
    private final ConcurrentHashMap<String, AutoBidConfig> autoBids;
    private final ConcurrentHashMap<String, List<ListingReportEntity>> reports;

    public MockDataStore() {
        this.users = new ConcurrentHashMap<>();
        this.listings = new ConcurrentHashMap<>();
        this.bidsByAuction = new ConcurrentHashMap<>();
        this.watchlistByUser = new ConcurrentHashMap<>();
        this.notificationsByUser = new ConcurrentHashMap<>();
        this.payments = new ConcurrentHashMap<>();
        this.autoBids = new ConcurrentHashMap<>();
        this.reports = new ConcurrentHashMap<>();
    }

    // ========== User Helpers ==========

    public Optional<UserEntity> getUserById(String id) {
        return Optional.ofNullable(users.get(id));
    }

    public Optional<UserEntity> getUserByEmail(String email) {
        return users.values().stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    // ========== Listing Helpers ==========

    public Optional<FurnitureListingEntity> getListingById(String id) {
        return Optional.ofNullable(listings.get(id));
    }

    // ========== Bid Helpers ==========

    public List<BidEntity> getBidsForAuction(String auctionId) {
        return bidsByAuction.getOrDefault(auctionId, Collections.emptyList());
    }

    // ========== Notification Helpers ==========

    public List<NotificationEntity> getNotificationsForUser(String userId) {
        return notificationsByUser.getOrDefault(userId, Collections.emptyList());
    }

    // ========== Payment Helpers ==========

    public List<PaymentEntity> getPaymentsForUser(String userId) {
        return payments.values().stream()
                .filter(payment -> payment.getUserId().toString().equals(userId))
                .collect(Collectors.toList());
    }

    // ========== Watchlist Helpers ==========

    public List<String> getWatchlistForUser(String userId) {
        return watchlistByUser.getOrDefault(userId, Collections.emptyList());
    }

    // ========== Pagination Helper ==========

    /**
     * Creates a paginated response from a list of items.
     * Delegates to {@link PaginationHelper#paginate(List, Integer, Integer)} for validation,
     * defaults, and offset calculation.
     *
     * @param items    the full list of items to paginate
     * @param page     the page number (1-based), or null to use default (1)
     * @param pageSize the number of items per page, or null to use default (20)
     * @param <T>      the type of items
     * @return a PaginatedResponse containing the sublist with pagination metadata
     */
    public <T> PaginatedResponse<T> paginate(List<T> items, Integer page, Integer pageSize) {
        return PaginationHelper.paginate(items, page, pageSize);
    }

    // ========== Direct Collection Accessors ==========

    public ConcurrentHashMap<String, UserEntity> getUsers() {
        return users;
    }

    public ConcurrentHashMap<String, FurnitureListingEntity> getListings() {
        return listings;
    }

    public ConcurrentHashMap<String, List<BidEntity>> getBidsByAuction() {
        return bidsByAuction;
    }

    public ConcurrentHashMap<String, List<String>> getWatchlistByUser() {
        return watchlistByUser;
    }

    public ConcurrentHashMap<String, List<NotificationEntity>> getNotificationsByUser() {
        return notificationsByUser;
    }

    public ConcurrentHashMap<String, PaymentEntity> getPayments() {
        return payments;
    }

    public ConcurrentHashMap<String, AutoBidConfig> getAutoBids() {
        return autoBids;
    }

    public ConcurrentHashMap<String, List<ListingReportEntity>> getReports() {
        return reports;
    }
}
