package com.furniturebid.mockapi.property;

import com.furniturebid.mockapi.dto.response.TopSellerDto;
import com.furniturebid.mockapi.entity.FurnitureListingEntity;
import com.furniturebid.mockapi.entity.UserEntity;
import com.furniturebid.mockapi.service.AdminService;
import com.furniturebid.mockapi.store.MockDataStore;
import net.jqwik.api.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

// Feature: mock-api-service, Property 10: Top Sellers Bounded and Sorted
// Validates: Requirements 13.4
class TopSellersPropertyTest {

    private AdminService createAdminServiceWithSellers(List<SellerData> sellers) {
        MockDataStore dataStore = new MockDataStore();

        int listingCounter = 0;
        for (SellerData seller : sellers) {
            // Create user entity for the seller
            UserEntity user = new UserEntity();
            user.setId(seller.sellerId());
            user.setEmail(seller.sellerId() + "@example.com");
            user.setPassword("password");
            user.setDisplayName(seller.displayName());
            user.setRole("seller");
            user.setStatus("active");
            user.setCreatedAt(Instant.now());
            dataStore.getUsers().put(seller.sellerId(), user);

            // Create ended listings for this seller
            for (int i = 0; i < seller.endedListingCount(); i++) {
                FurnitureListingEntity listing = new FurnitureListingEntity();
                UUID listingId = UUID.randomUUID();
                listing.setId(listingId);
                listing.setTitle("Listing " + listingId);
                listing.setCategory("sofa");
                listing.setStatus("ended");
                listing.setSellerId(seller.sellerId());
                listing.setSellerDisplayName(seller.displayName());
                listing.setStartingPrice(BigDecimal.valueOf(50));
                listing.setCurrentBid(seller.bidAmounts().get(i));
                listing.setCreatedAt(Instant.now());
                dataStore.getListings().put(listingId, listing);
            }

            // Create active listings (should NOT count as completed auctions)
            for (int i = 0; i < seller.activeListingCount(); i++) {
                FurnitureListingEntity listing = new FurnitureListingEntity();
                UUID listingId = UUID.randomUUID();
                listing.setId(listingId);
                listing.setTitle("Active Listing " + listingId);
                listing.setCategory("sofa");
                listing.setStatus("active");
                listing.setSellerId(seller.sellerId());
                listing.setSellerDisplayName(seller.displayName());
                listing.setStartingPrice(BigDecimal.valueOf(50));
                listing.setCurrentBid(BigDecimal.valueOf(100));
                listing.setCreatedAt(Instant.now());
                dataStore.getListings().put(listingId, listing);
            }
        }

        return new AdminService(dataStore);
    }

    @Property
    void topSellersHasAtMostTenEntries(
            @ForAll("validDateRanges") DateRange dateRange,
            @ForAll("sellerDataSets") List<SellerData> sellers
    ) {
        AdminService adminService = createAdminServiceWithSellers(sellers);

        List<TopSellerDto> topSellers = adminService.getTopSellers(
                dateRange.startDate().toString(),
                dateRange.endDate().toString()
        );

        // Verify: array has at most 10 entries
        assertThat(topSellers).hasSizeLessThanOrEqualTo(10);
    }

    @Property
    void topSellersAreSortedByTotalRevenueDescending(
            @ForAll("validDateRanges") DateRange dateRange,
            @ForAll("sellerDataSets") List<SellerData> sellers
    ) {
        AdminService adminService = createAdminServiceWithSellers(sellers);

        List<TopSellerDto> topSellers = adminService.getTopSellers(
                dateRange.startDate().toString(),
                dateRange.endDate().toString()
        );

        // Verify: entries are sorted by totalRevenue descending (each totalRevenue >= next)
        for (int i = 0; i < topSellers.size() - 1; i++) {
            BigDecimal current = topSellers.get(i).getTotalRevenue();
            BigDecimal next = topSellers.get(i + 1).getTotalRevenue();
            assertThat(current.compareTo(next))
                    .as("Entry %d revenue (%s) should be >= entry %d revenue (%s)",
                            i, current, i + 1, next)
                    .isGreaterThanOrEqualTo(0);
        }
    }

    @Property
    void topSellersHavePositiveCompletedAuctionsAndRevenue(
            @ForAll("validDateRanges") DateRange dateRange,
            @ForAll("sellerDataSets") List<SellerData> sellers
    ) {
        AdminService adminService = createAdminServiceWithSellers(sellers);

        List<TopSellerDto> topSellers = adminService.getTopSellers(
                dateRange.startDate().toString(),
                dateRange.endDate().toString()
        );

        // Verify: each entry has positive completedAuctions (> 0) and positive totalRevenue (> 0)
        for (TopSellerDto seller : topSellers) {
            assertThat(seller.getCompletedAuctions())
                    .as("completedAuctions for %s", seller.getDisplayName())
                    .isGreaterThan(0);
            assertThat(seller.getTotalRevenue().compareTo(BigDecimal.ZERO))
                    .as("totalRevenue for %s", seller.getDisplayName())
                    .isGreaterThan(0);
        }
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
    Arbitrary<List<SellerData>> sellerDataSets() {
        // Generate between 0 and 15 sellers (more than 10 to test the limit)
        return Arbitraries.integers().between(0, 15).flatMap(sellerCount -> {
            List<Arbitrary<SellerData>> sellerArbitraries = new ArrayList<>();
            for (int i = 0; i < sellerCount; i++) {
                final int index = i;
                sellerArbitraries.add(sellerData(index));
            }
            if (sellerArbitraries.isEmpty()) {
                return Arbitraries.just(Collections.emptyList());
            }
            return Combinators.combine(sellerArbitraries).as(list -> list);
        });
    }

    private Arbitrary<SellerData> sellerData(int index) {
        // Each seller has 0 to 5 ended listings and 0 to 3 active listings
        // Sellers with 0 ended listings should be excluded from results
        return Arbitraries.integers().between(0, 5).flatMap(endedCount ->
                Arbitraries.integers().between(0, 3).flatMap(activeCount -> {
                    if (endedCount == 0) {
                        return Arbitraries.just(new SellerData(
                                UUID.randomUUID(),
                                "Seller " + index,
                                0,
                                activeCount,
                                Collections.emptyList()
                        ));
                    }
                    // Generate bid amounts for ended listings (positive values)
                    return Arbitraries.bigDecimals()
                            .between(BigDecimal.valueOf(10), BigDecimal.valueOf(5000))
                            .list().ofSize(endedCount)
                            .map(bidAmounts -> new SellerData(
                                    UUID.randomUUID(),
                                    "Seller " + index,
                                    endedCount,
                                    activeCount,
                                    bidAmounts
                            ));
                })
        );
    }

    // ========== Data Records ==========

    record DateRange(LocalDate startDate, LocalDate endDate) {}

    record SellerData(UUID sellerId, String displayName, int endedListingCount,
                      int activeListingCount, List<BigDecimal> bidAmounts) {}
}
