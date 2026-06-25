package com.furniturebid.mockapi.property;

import com.furniturebid.mockapi.dto.response.*;
import com.furniturebid.mockapi.entity.*;
import com.furniturebid.mockapi.service.*;
import com.furniturebid.mockapi.store.MockDataStore;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// Feature: mock-api-service, Property 7: Chronological Descending Sort Order
// Validates: Requirements 5.3, 9.1, 10.4, 12.4
class ChronologicalSortPropertyTest {

    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final UUID TEST_AUCTION_ID = UUID.randomUUID();

    @Provide
    Arbitrary<List<Instant>> randomTimestamps() {
        Arbitrary<Integer> countArb = Arbitraries.integers().between(5, 20);
        Arbitrary<Long> epochArb = Arbitraries.longs()
                .between(Instant.parse("2020-01-01T00:00:00Z").getEpochSecond(),
                        Instant.parse("2025-01-01T00:00:00Z").getEpochSecond());

        return countArb.flatMap(count ->
                epochArb.list().ofSize(count).map(epochs ->
                        epochs.stream()
                                .map(Instant::ofEpochSecond)
                                .toList()
                )
        );
    }

    @Property
    void bidHistorySortedDescending(@ForAll("randomTimestamps") List<Instant> timestamps) {
        // Given - a fresh store with bids at random timestamps
        MockDataStore store = new MockDataStore();
        List<BidEntity> bids = new ArrayList<>();

        for (int i = 0; i < timestamps.size(); i++) {
            BidEntity bid = new BidEntity(
                    UUID.randomUUID(),
                    TEST_AUCTION_ID,
                    UUID.randomUUID(),
                    "Bidder " + i,
                    new BigDecimal("100.00").add(new BigDecimal(i)),
                    timestamps.get(i)
            );
            bids.add(bid);
        }
        store.getBidsByAuction().put(TEST_AUCTION_ID, bids);

        AuctionService service = new AuctionService(store);

        // When - requesting full page of bid history
        PaginatedResponse<BidDto> result = service.getBidHistory(TEST_AUCTION_ID, 1, 100);

        // Then - items are sorted by timestamp descending
        List<BidDto> items = result.getData();
        assertThat(items).hasSizeGreaterThanOrEqualTo(5);

        for (int i = 0; i < items.size() - 1; i++) {
            assertThat(items.get(i).getTimestamp())
                    .as("Bid at index %d (timestamp=%s) should be >= bid at index %d (timestamp=%s)",
                            i, items.get(i).getTimestamp(), i + 1, items.get(i + 1).getTimestamp())
                    .isAfterOrEqualTo(items.get(i + 1).getTimestamp());
        }
    }

    @Property
    void notificationsSortedDescending(@ForAll("randomTimestamps") List<Instant> timestamps) {
        // Given - a fresh store with notifications at random timestamps
        MockDataStore store = new MockDataStore();
        List<NotificationEntity> notifications = new ArrayList<>();

        for (int i = 0; i < timestamps.size(); i++) {
            NotificationEntity notification = new NotificationEntity(
                    UUID.randomUUID(),
                    TEST_USER_ID,
                    "outbid",
                    "Notification " + i,
                    "You have been outbid on item " + i,
                    UUID.randomUUID(),
                    false,
                    timestamps.get(i)
            );
            notifications.add(notification);
        }
        store.getNotificationsByUser().put(TEST_USER_ID, notifications);

        NotificationService service = new NotificationService(store);

        // When - requesting full page of notifications
        PaginatedResponse<NotificationDto> result = service.getNotifications(TEST_USER_ID, 1, 100);

        // Then - items are sorted by createdAt descending
        List<NotificationDto> items = result.getData();
        assertThat(items).hasSizeGreaterThanOrEqualTo(5);

        for (int i = 0; i < items.size() - 1; i++) {
            assertThat(items.get(i).getCreatedAt())
                    .as("Notification at index %d (createdAt=%s) should be >= notification at index %d (createdAt=%s)",
                            i, items.get(i).getCreatedAt(), i + 1, items.get(i + 1).getCreatedAt())
                    .isAfterOrEqualTo(items.get(i + 1).getCreatedAt());
        }
    }

    @Property
    void paymentHistorySortedDescending(@ForAll("randomTimestamps") List<Instant> timestamps) {
        // Given - a fresh store with payments at random timestamps
        MockDataStore store = new MockDataStore();

        for (int i = 0; i < timestamps.size(); i++) {
            PaymentEntity payment = new PaymentEntity(
                    UUID.randomUUID(),
                    TEST_USER_ID,
                    UUID.randomUUID(),
                    "pi_mock_secret_" + i,
                    new BigDecimal("50.00").add(new BigDecimal(i * 10)),
                    "USD",
                    "succeeded",
                    timestamps.get(i)
            );
            store.getPayments().put(payment.getId(), payment);
        }

        PaymentService service = new PaymentService(store);

        // When - requesting full page of payment history
        PaginatedResponse<PaymentRecordDto> result = service.getPaymentHistory(TEST_USER_ID, 1, 100);

        // Then - items are sorted by createdAt descending
        List<PaymentRecordDto> items = result.getData();
        assertThat(items).hasSizeGreaterThanOrEqualTo(5);

        for (int i = 0; i < items.size() - 1; i++) {
            assertThat(items.get(i).getCreatedAt())
                    .as("Payment at index %d (createdAt=%s) should be >= payment at index %d (createdAt=%s)",
                            i, items.get(i).getCreatedAt(), i + 1, items.get(i + 1).getCreatedAt())
                    .isAfterOrEqualTo(items.get(i + 1).getCreatedAt());
        }
    }

    @Property
    void reportsSortedDescending(@ForAll("randomTimestamps") List<Instant> timestamps) {
        // Given - a fresh store with reports at random timestamps
        MockDataStore store = new MockDataStore();

        List<ListingReportEntity> reports = new ArrayList<>();
        for (int i = 0; i < timestamps.size(); i++) {
            ListingReportEntity report = new ListingReportEntity(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "Inappropriate content",
                    UUID.randomUUID(),
                    "Reporter " + i,
                    timestamps.get(i)
            );
            reports.add(report);
        }
        store.getReports().put("all-reports", reports);

        AdminService service = new AdminService(store);

        // When - requesting full page of reports
        PaginatedResponse<ListingReportDto> result = service.getReports(1, 100);

        // Then - items are sorted by reportDate descending
        List<ListingReportDto> items = result.getData();
        assertThat(items).hasSizeGreaterThanOrEqualTo(5);

        for (int i = 0; i < items.size() - 1; i++) {
            assertThat(items.get(i).getReportDate())
                    .as("Report at index %d (reportDate=%s) should be >= report at index %d (reportDate=%s)",
                            i, items.get(i).getReportDate(), i + 1, items.get(i + 1).getReportDate())
                    .isAfterOrEqualTo(items.get(i + 1).getReportDate());
        }
    }
}
