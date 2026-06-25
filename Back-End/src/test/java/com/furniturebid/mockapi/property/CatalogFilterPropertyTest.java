package com.furniturebid.mockapi.property;

import com.furniturebid.mockapi.dto.response.FurnitureListingSummaryDto;
import com.furniturebid.mockapi.dto.response.PaginatedResponse;
import com.furniturebid.mockapi.entity.FurnitureListingEntity;
import com.furniturebid.mockapi.service.FurnitureService;
import com.furniturebid.mockapi.store.MockDataStore;
import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

// Feature: mock-api-service, Property 4: Catalog Filtering Correctness
// Validates: Requirements 7.1, 7.3, 7.4, 7.5, 7.6
class CatalogFilterPropertyTest {

    private static final List<String> ALL_CATEGORIES = List.of(
            "sofa", "dining-table", "office-chair", "wardrobe",
            "bed-frame", "coffee-table", "cabinet", "bookshelf"
    );

    private static final List<String> ALL_CONDITIONS = List.of(
            "new", "like-new", "good", "fair", "poor"
    );

    private static final List<String> ALL_STATUSES = List.of("active", "ended", "flagged");

    private static final List<String> KNOWN_LOCATIONS = List.of(
            "New York, NY", "Los Angeles, CA", "Chicago, IL",
            "Houston, TX", "San Francisco, CA", "Seattle, WA",
            "Boston, MA", "Denver, CO"
    );

    private MockDataStore dataStore;
    private FurnitureService furnitureService;

    @BeforeProperty
    void setUp() {
        dataStore = new MockDataStore();
        furnitureService = new FurnitureService(dataStore);
        seedDataStore();
    }

    private void seedDataStore() {
        // Seed diverse listings with mix of statuses, categories, conditions, prices, locations
        Random random = new Random(42);
        for (int i = 0; i < 30; i++) {
            UUID id = UUID.randomUUID();
            String category = ALL_CATEGORIES.get(i % ALL_CATEGORIES.size());
            String condition = ALL_CONDITIONS.get(i % ALL_CONDITIONS.size());
            String status = ALL_STATUSES.get(i % ALL_STATUSES.size());
            String location = KNOWN_LOCATIONS.get(i % KNOWN_LOCATIONS.size());
            BigDecimal currentBid = BigDecimal.valueOf(50 + (i * 37) % 950).setScale(2);

            FurnitureListingEntity entity = new FurnitureListingEntity(
                    id,
                    "Listing " + i,
                    "Description for listing " + i,
                    category,
                    condition,
                    null,
                    null,
                    null,
                    null,
                    location,
                    List.of("https://img.example.com/" + i + ".jpg"),
                    BigDecimal.valueOf(10.00),
                    null,
                    currentBid,
                    random.nextInt(10),
                    Instant.now().plus(1 + (i % 14), ChronoUnit.DAYS),
                    status,
                    UUID.randomUUID(),
                    "Test Seller",
                    4.5,
                    Instant.now().minus(i, ChronoUnit.DAYS)
            );
            dataStore.getListings().put(id, entity);
        }
    }

    // --- Property 1: All returned items are active ---

    @Property
    void allReturnedItemsAreActive(
            @ForAll("categoryFilter") String category,
            @ForAll("conditionFilter") String condition,
            @ForAll("optionalPriceMin") BigDecimal priceMin,
            @ForAll("optionalPriceMax") BigDecimal priceMax,
            @ForAll("optionalLocation") String location
    ) {
        PaginatedResponse<FurnitureListingSummaryDto> response = furnitureService.getCatalog(
                category, condition, priceMin, priceMax, location, null, 1, 100
        );

        // Verify every returned item corresponds to an active listing in the data store
        for (FurnitureListingSummaryDto dto : response.getData()) {
            FurnitureListingEntity entity = dataStore.getListings().get(dto.getId());
            assertThat(entity).isNotNull();
            assertThat(entity.getStatus()).isEqualTo("active");
        }
    }

    // --- Property 2: Category filter works ---

    @Property
    void categoryFilterReturnsMatchingItems(
            @ForAll("nonEmptyCategoryFilter") String category
    ) {
        PaginatedResponse<FurnitureListingSummaryDto> response = furnitureService.getCatalog(
                category, null, null, null, null, null, 1, 100
        );

        Set<String> requestedCategories = Arrays.stream(category.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(ALL_CATEGORIES::contains)
                .collect(Collectors.toSet());

        if (!requestedCategories.isEmpty()) {
            for (FurnitureListingSummaryDto dto : response.getData()) {
                assertThat(dto.getCategory()).isIn(requestedCategories);
            }
        }
    }

    // --- Property 3: Condition filter works ---

    @Property
    void conditionFilterReturnsMatchingItems(
            @ForAll("nonEmptyConditionFilter") String condition
    ) {
        PaginatedResponse<FurnitureListingSummaryDto> response = furnitureService.getCatalog(
                null, condition, null, null, null, null, 1, 100
        );

        Set<String> requestedConditions = Arrays.stream(condition.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(ALL_CONDITIONS::contains)
                .collect(Collectors.toSet());

        if (!requestedConditions.isEmpty()) {
            for (FurnitureListingSummaryDto dto : response.getData()) {
                assertThat(dto.getCondition()).isIn(requestedConditions);
            }
        }
    }

    // --- Property 4: Price range filter works ---

    @Property
    void priceRangeFilterReturnsItemsInRange(
            @ForAll("validPriceMin") BigDecimal priceMin,
            @ForAll("validPriceMax") BigDecimal priceMax
    ) {
        // Only test when priceMin <= priceMax (non-empty case)
        if (priceMin.compareTo(priceMax) > 0) {
            return; // Covered by priceMinGreaterThanPriceMaxReturnsEmpty
        }

        PaginatedResponse<FurnitureListingSummaryDto> response = furnitureService.getCatalog(
                null, null, priceMin, priceMax, null, null, 1, 100
        );

        for (FurnitureListingSummaryDto dto : response.getData()) {
            assertThat(dto.getCurrentBid()).isGreaterThanOrEqualTo(priceMin);
            assertThat(dto.getCurrentBid()).isLessThanOrEqualTo(priceMax);
        }
    }

    // --- Property 5: Location filter works ---

    @Property
    void locationFilterReturnsMatchingItems(
            @ForAll("locationSubstring") String location
    ) {
        PaginatedResponse<FurnitureListingSummaryDto> response = furnitureService.getCatalog(
                null, null, null, null, location, null, 1, 100
        );

        String locationLower = location.toLowerCase();
        for (FurnitureListingSummaryDto dto : response.getData()) {
            FurnitureListingEntity entity = dataStore.getListings().get(dto.getId());
            assertThat(entity.getLocation().toLowerCase()).contains(locationLower);
        }
    }

    // --- Property 6: priceMin > priceMax returns empty ---

    @Property
    void priceMinGreaterThanPriceMaxReturnsEmpty(
            @ForAll("priceMinGreaterThanMax") BigDecimal[] pricePair
    ) {
        BigDecimal priceMin = pricePair[0];
        BigDecimal priceMax = pricePair[1];

        PaginatedResponse<FurnitureListingSummaryDto> response = furnitureService.getCatalog(
                null, null, priceMin, priceMax, null, null, 1, 100
        );

        assertThat(response.getData()).isEmpty();
    }

    // ========== Generators ==========

    @Provide
    Arbitrary<String> categoryFilter() {
        return Arbitraries.frequencyOf(
                Tuple.of(1, Arbitraries.just(null)),
                Tuple.of(3, nonEmptyCategoryFilter())
        );
    }

    @Provide
    Arbitrary<String> nonEmptyCategoryFilter() {
        return Arbitraries.of(ALL_CATEGORIES)
                .list().ofMinSize(1).ofMaxSize(4)
                .map(list -> String.join(",", list));
    }

    @Provide
    Arbitrary<String> conditionFilter() {
        return Arbitraries.frequencyOf(
                Tuple.of(1, Arbitraries.just(null)),
                Tuple.of(3, nonEmptyConditionFilter())
        );
    }

    @Provide
    Arbitrary<String> nonEmptyConditionFilter() {
        return Arbitraries.of(ALL_CONDITIONS)
                .list().ofMinSize(1).ofMaxSize(3)
                .map(list -> String.join(",", list));
    }

    @Provide
    Arbitrary<BigDecimal> optionalPriceMin() {
        return Arbitraries.frequencyOf(
                Tuple.of(1, Arbitraries.just((BigDecimal) null)),
                Tuple.of(3, Arbitraries.bigDecimals()
                        .between(BigDecimal.ZERO, BigDecimal.valueOf(999))
                        .ofScale(2))
        );
    }

    @Provide
    Arbitrary<BigDecimal> optionalPriceMax() {
        return Arbitraries.frequencyOf(
                Tuple.of(1, Arbitraries.just((BigDecimal) null)),
                Tuple.of(3, Arbitraries.bigDecimals()
                        .between(BigDecimal.ONE, BigDecimal.valueOf(1000))
                        .ofScale(2))
        );
    }

    @Provide
    Arbitrary<String> optionalLocation() {
        return Arbitraries.frequencyOf(
                Tuple.of(1, Arbitraries.just((String) null)),
                Tuple.of(3, locationSubstring())
        );
    }

    @Provide
    Arbitrary<String> locationSubstring() {
        // Pick a known location and take a substring of it
        return Arbitraries.of(KNOWN_LOCATIONS)
                .flatMap(loc -> {
                    if (loc.length() <= 1) {
                        return Arbitraries.just(loc);
                    }
                    return Arbitraries.integers().between(0, loc.length() - 1)
                            .flatMap(start -> Arbitraries.integers().between(start + 1, loc.length())
                                    .map(end -> loc.substring(start, end)));
                });
    }

    @Provide
    Arbitrary<BigDecimal> validPriceMin() {
        return Arbitraries.bigDecimals()
                .between(BigDecimal.ZERO, BigDecimal.valueOf(500))
                .ofScale(2);
    }

    @Provide
    Arbitrary<BigDecimal> validPriceMax() {
        return Arbitraries.bigDecimals()
                .between(BigDecimal.valueOf(100), BigDecimal.valueOf(1000))
                .ofScale(2);
    }

    @Provide
    Arbitrary<BigDecimal[]> priceMinGreaterThanMax() {
        return Arbitraries.bigDecimals()
                .between(BigDecimal.valueOf(100), BigDecimal.valueOf(1000))
                .ofScale(2)
                .flatMap(max -> Arbitraries.bigDecimals()
                        .between(max.add(BigDecimal.ONE), max.add(BigDecimal.valueOf(500)))
                        .ofScale(2)
                        .map(min -> new BigDecimal[]{min, max}));
    }
}
