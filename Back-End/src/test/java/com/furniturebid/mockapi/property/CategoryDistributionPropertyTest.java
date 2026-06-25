package com.furniturebid.mockapi.property;

import com.furniturebid.mockapi.dto.response.CategoryDistributionDto;
import com.furniturebid.mockapi.entity.FurnitureListingEntity;
import com.furniturebid.mockapi.service.AdminService;
import com.furniturebid.mockapi.store.MockDataStore;
import net.jqwik.api.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

// Feature: mock-api-service, Property 9: Category Distribution Completeness
// Validates: Requirements 13.3
class CategoryDistributionPropertyTest {

    private static final List<String> VALID_CATEGORIES = Arrays.asList(
            "sofa", "dining-table", "office-chair", "wardrobe",
            "bed-frame", "coffee-table", "cabinet", "bookshelf"
    );

    private AdminService createAdminServiceWithListings(List<FurnitureListingEntity> listings) {
        MockDataStore dataStore = new MockDataStore();
        for (FurnitureListingEntity listing : listings) {
            dataStore.getListings().put(listing.getId(), listing);
        }
        return new AdminService(dataStore);
    }

    @Property
    void categoryDistributionReturnsExactlyEightEntries(
            @ForAll("validDateRanges") DateRange dateRange,
            @ForAll("listingCounts") List<String> listingCategories
    ) {
        // Seed listings with random categories
        List<FurnitureListingEntity> listings = new ArrayList<>();
        for (int i = 0; i < listingCategories.size(); i++) {
            listings.add(createListing("listing-" + i, listingCategories.get(i)));
        }

        AdminService adminService = createAdminServiceWithListings(listings);

        List<CategoryDistributionDto> distribution = adminService.getCategoryDistribution(
                dateRange.startDate().toString(),
                dateRange.endDate().toString()
        );

        // Verify: array has exactly 8 entries
        assertThat(distribution).hasSize(8);
    }

    @Property
    void categoryDistributionContainsAllValidCategories(
            @ForAll("validDateRanges") DateRange dateRange,
            @ForAll("listingCounts") List<String> listingCategories
    ) {
        List<FurnitureListingEntity> listings = new ArrayList<>();
        for (int i = 0; i < listingCategories.size(); i++) {
            listings.add(createListing("listing-" + i, listingCategories.get(i)));
        }

        AdminService adminService = createAdminServiceWithListings(listings);

        List<CategoryDistributionDto> distribution = adminService.getCategoryDistribution(
                dateRange.startDate().toString(),
                dateRange.endDate().toString()
        );

        // Verify: each entry's category is one of the valid FurnitureCategory values
        List<String> returnedCategories = distribution.stream()
                .map(CategoryDistributionDto::getCategory)
                .collect(Collectors.toList());

        assertThat(returnedCategories).containsExactlyInAnyOrderElementsOf(VALID_CATEGORIES);
    }

    @Property
    void categoryDistributionHasNonNegativeCounts(
            @ForAll("validDateRanges") DateRange dateRange,
            @ForAll("listingCounts") List<String> listingCategories
    ) {
        List<FurnitureListingEntity> listings = new ArrayList<>();
        for (int i = 0; i < listingCategories.size(); i++) {
            listings.add(createListing("listing-" + i, listingCategories.get(i)));
        }

        AdminService adminService = createAdminServiceWithListings(listings);

        List<CategoryDistributionDto> distribution = adminService.getCategoryDistribution(
                dateRange.startDate().toString(),
                dateRange.endDate().toString()
        );

        // Verify: each entry's count is non-negative
        for (CategoryDistributionDto entry : distribution) {
            assertThat(entry.getCount())
                    .as("count for category %s", entry.getCategory())
                    .isGreaterThanOrEqualTo(0);
        }
    }

    @Property
    void categoryDistributionHasNoDuplicateCategories(
            @ForAll("validDateRanges") DateRange dateRange,
            @ForAll("listingCounts") List<String> listingCategories
    ) {
        List<FurnitureListingEntity> listings = new ArrayList<>();
        for (int i = 0; i < listingCategories.size(); i++) {
            listings.add(createListing("listing-" + i, listingCategories.get(i)));
        }

        AdminService adminService = createAdminServiceWithListings(listings);

        List<CategoryDistributionDto> distribution = adminService.getCategoryDistribution(
                dateRange.startDate().toString(),
                dateRange.endDate().toString()
        );

        // Verify: no duplicates (all 8 categories are represented, no missing)
        List<String> returnedCategories = distribution.stream()
                .map(CategoryDistributionDto::getCategory)
                .collect(Collectors.toList());

        Set<String> uniqueCategories = new HashSet<>(returnedCategories);
        assertThat(uniqueCategories).hasSameSizeAs(returnedCategories);
    }

    // ========== Helpers ==========

    private FurnitureListingEntity createListing(String id, String category) {
        FurnitureListingEntity listing = new FurnitureListingEntity();
        listing.setId(id);
        listing.setTitle("Test Listing " + id);
        listing.setCategory(category);
        listing.setStatus("active");
        listing.setStartingPrice(BigDecimal.valueOf(100));
        listing.setCurrentBid(BigDecimal.valueOf(150));
        listing.setSellerId(UUID.randomUUID());
        listing.setSellerDisplayName("Test Seller");
        listing.setCreatedAt(Instant.now());
        return listing;
    }

    // ========== Providers ==========

    @Provide
    Arbitrary<DateRange> validDateRanges() {
        return Arbitraries.integers().between(0, 3650).flatMap(startOffset ->
                Arbitraries.integers().between(0, 364).map(rangeLength -> {
                    LocalDate baseDate = LocalDate.of(2020, 1, 1).plusDays(startOffset);
                    LocalDate endDate = baseDate.plusDays(rangeLength);
                    return new DateRange(baseDate, endDate);
                })
        );
    }

    @Provide
    Arbitrary<List<String>> listingCounts() {
        // Generate 0 to 20 listings with random valid categories
        Arbitrary<String> categoryArbitrary = Arbitraries.of(VALID_CATEGORIES);
        return categoryArbitrary.list().ofMinSize(0).ofMaxSize(20);
    }

    record DateRange(LocalDate startDate, LocalDate endDate) {}
}
