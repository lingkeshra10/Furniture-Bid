package com.furniturebid.mockapi.property;

import com.furniturebid.mockapi.dto.response.FurnitureListingSummaryDto;
import com.furniturebid.mockapi.dto.response.PaginatedResponse;
import com.furniturebid.mockapi.entity.FurnitureListingEntity;
import com.furniturebid.mockapi.service.FurnitureService;
import com.furniturebid.mockapi.store.MockDataStore;
import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.StringLength;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// Feature: mock-api-service, Property 5: Catalog Sort Order
// Validates: Requirements 7.2
class CatalogSortPropertyTest {

    private MockDataStore createSeededStore() {
        MockDataStore store = new MockDataStore();

        Instant now = Instant.now();

        // Create 6 active listings with distinct prices, end dates, and created dates
        store.getListings().put(UUID.randomUUID(), new FurnitureListingEntity(
                UUID.randomUUID(), "Vintage Sofa", "A nice vintage sofa", "sofa", "good",
                null, null, null, null, "New York",
                Collections.singletonList("img1.jpg"),
                new BigDecimal("100.00"), null, new BigDecimal("150.00"),
                3, now.plus(2, ChronoUnit.DAYS), "active",
                UUID.randomUUID(), "Seller One", 4.5, now.minus(10, ChronoUnit.DAYS)
        ));

        store.getListings().put(UUID.randomUUID(), new FurnitureListingEntity(
                UUID.randomUUID(), "Modern Chair", "A sleek office chair", "office-chair", "new",
                null, null, null, null, "Los Angeles",
                Collections.singletonList("img2.jpg"),
                new BigDecimal("200.00"), null, new BigDecimal("350.00"),
                5, now.plus(5, ChronoUnit.DAYS), "active",
                UUID.randomUUID(), "Seller One", 4.5, now.minus(5, ChronoUnit.DAYS)
        ));

        store.getListings().put("listing-3", new FurnitureListingEntity(
                UUID.randomUUID(), "Oak Dining Table", "A beautiful oak table", "dining-table", "like-new",
                null, null, null, null, "Chicago",
                Collections.singletonList("img3.jpg"),
                new BigDecimal("300.00"), null, new BigDecimal("50.00"),
                1, now.plus(1, ChronoUnit.DAYS), "active",
                UUID.randomUUID(), "Seller Two", 4.0, now.minus(20, ChronoUnit.DAYS)
        ));

        store.getListings().put("listing-4", new FurnitureListingEntity(
                UUID.randomUUID(), "Wardrobe Set", "Large wardrobe set", "wardrobe", "fair",
                null, null, null, null, "Miami",
                Collections.singletonList("img4.jpg"),
                new BigDecimal("400.00"), null, new BigDecimal("500.00"),
                8, now.plus(10, ChronoUnit.DAYS), "active",
                UUID.randomUUID(), "Seller Two", 4.0, now.minus(2, ChronoUnit.DAYS)
        ));

        store.getListings().put("listing-5", new FurnitureListingEntity(
                UUID.randomUUID(), "Coffee Table", "Small coffee table", "coffee-table", "good",
                null, null, null, null, "Boston",
                Collections.singletonList("img5.jpg"),
                new BigDecimal("50.00"), null, new BigDecimal("75.00"),
                2, now.plus(3, ChronoUnit.DAYS), "active",
                UUID.randomUUID(), "Seller Three", 3.5, now.minus(15, ChronoUnit.DAYS)
        ));

        store.getListings().put("listing-6", new FurnitureListingEntity(
                UUID.randomUUID(), "Bed Frame", "King size bed frame", "bed-frame", "new",
                null, null, null, null, "Seattle",
                Collections.singletonList("img6.jpg"),
                new BigDecimal("600.00"), null, new BigDecimal("800.00"),
                10, now.plus(7, ChronoUnit.DAYS), "active",
                UUID.randomUUID(), "Seller Three", 3.5, now.minus(1, ChronoUnit.DAYS)
        ));

        // Add one non-active listing to ensure it's excluded
        store.getListings().put("listing-7", new FurnitureListingEntity(
                UUID.randomUUID(), "Ended Bookshelf", "An ended listing", "bookshelf", "poor",
                null, null, null, null, "Denver",
                Collections.singletonList("img7.jpg"),
                new BigDecimal("25.00"), null, new BigDecimal("30.00"),
                1, now.minus(1, ChronoUnit.DAYS), "ended",
                UUID.randomUUID(), "Seller One", 4.5, now.minus(30, ChronoUnit.DAYS)
        ));

        return store;
    }

    @Property
    void endingSoonestSortOrdersItemsByTimeRemainingAscending() {
        // Given
        MockDataStore store = createSeededStore();
        FurnitureService service = new FurnitureService(store);

        // When
        PaginatedResponse<FurnitureListingSummaryDto> result = service.getCatalog(
                null, null, null, null, null, "ending-soonest", 1, 100);

        // Then
        List<FurnitureListingSummaryDto> items = result.getData();
        assertThat(items).hasSizeGreaterThanOrEqualTo(2);

        for (int i = 0; i < items.size() - 1; i++) {
            assertThat(items.get(i).getTimeRemaining())
                    .as("Item at index %d (timeRemaining=%d) should be <= item at index %d (timeRemaining=%d)",
                            i, items.get(i).getTimeRemaining(), i + 1, items.get(i + 1).getTimeRemaining())
                    .isLessThanOrEqualTo(items.get(i + 1).getTimeRemaining());
        }
    }

    @Property
    void priceLowHighSortOrdersItemsByCurrentBidAscending() {
        // Given
        MockDataStore store = createSeededStore();
        FurnitureService service = new FurnitureService(store);

        // When
        PaginatedResponse<FurnitureListingSummaryDto> result = service.getCatalog(
                null, null, null, null, null, "price-low-high", 1, 100);

        // Then
        List<FurnitureListingSummaryDto> items = result.getData();
        assertThat(items).hasSizeGreaterThanOrEqualTo(2);

        for (int i = 0; i < items.size() - 1; i++) {
            assertThat(items.get(i).getCurrentBid())
                    .as("Item at index %d (currentBid=%s) should be <= item at index %d (currentBid=%s)",
                            i, items.get(i).getCurrentBid(), i + 1, items.get(i + 1).getCurrentBid())
                    .isLessThanOrEqualTo(items.get(i + 1).getCurrentBid());
        }
    }

    @Property
    void priceHighLowSortOrdersItemsByCurrentBidDescending() {
        // Given
        MockDataStore store = createSeededStore();
        FurnitureService service = new FurnitureService(store);

        // When
        PaginatedResponse<FurnitureListingSummaryDto> result = service.getCatalog(
                null, null, null, null, null, "price-high-low", 1, 100);

        // Then
        List<FurnitureListingSummaryDto> items = result.getData();
        assertThat(items).hasSizeGreaterThanOrEqualTo(2);

        for (int i = 0; i < items.size() - 1; i++) {
            assertThat(items.get(i).getCurrentBid())
                    .as("Item at index %d (currentBid=%s) should be >= item at index %d (currentBid=%s)",
                            i, items.get(i).getCurrentBid(), i + 1, items.get(i + 1).getCurrentBid())
                    .isGreaterThanOrEqualTo(items.get(i + 1).getCurrentBid());
        }
    }

    @Property
    void newestSortOrdersItemsByCreatedAtDescending() {
        // Given
        MockDataStore store = createSeededStore();
        FurnitureService service = new FurnitureService(store);

        // When
        PaginatedResponse<FurnitureListingSummaryDto> result = service.getCatalog(
                null, null, null, null, null, "newest", 1, 100);

        // Then - verify by checking listing IDs map back to entities in correct createdAt order
        List<FurnitureListingSummaryDto> items = result.getData();
        assertThat(items).hasSizeGreaterThanOrEqualTo(2);

        for (int i = 0; i < items.size() - 1; i++) {
            Instant createdAtCurrent = store.getListingById(items.get(i).getId())
                    .orElseThrow().getCreatedAt();
            Instant createdAtNext = store.getListingById(items.get(i + 1).getId())
                    .orElseThrow().getCreatedAt();
            assertThat(createdAtCurrent)
                    .as("Item at index %d should have been created at or after item at index %d", i, i + 1)
                    .isAfterOrEqualTo(createdAtNext);
        }
    }

    @Property
    void invalidSortDefaultsToEndingSoonest(
            @ForAll @AlphaChars @StringLength(min = 1, max = 20) String invalidSort
    ) {
        // Skip if the random string happens to be a valid sort value
        if (invalidSort.equals("ending-soonest") || invalidSort.equals("price-low-high")
                || invalidSort.equals("price-high-low") || invalidSort.equals("newest")) {
            return;
        }

        // Given
        MockDataStore store = createSeededStore();
        FurnitureService service = new FurnitureService(store);

        // When - with invalid sort
        PaginatedResponse<FurnitureListingSummaryDto> invalidResult = service.getCatalog(
                null, null, null, null, null, invalidSort, 1, 100);

        // When - with explicit ending-soonest
        PaginatedResponse<FurnitureListingSummaryDto> defaultResult = service.getCatalog(
                null, null, null, null, null, "ending-soonest", 1, 100);

        // Then - results should have the same order (same IDs in same positions)
        List<FurnitureListingSummaryDto> invalidItems = invalidResult.getData();
        List<FurnitureListingSummaryDto> defaultItems = defaultResult.getData();

        assertThat(invalidItems).hasSameSizeAs(defaultItems);

        for (int i = 0; i < invalidItems.size(); i++) {
            assertThat(invalidItems.get(i).getId())
                    .as("Item at index %d should match between invalid sort '%s' and 'ending-soonest'", i, invalidSort)
                    .isEqualTo(defaultItems.get(i).getId());
        }
    }
}
