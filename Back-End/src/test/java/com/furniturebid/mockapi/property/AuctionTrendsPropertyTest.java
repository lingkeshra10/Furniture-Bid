package com.furniturebid.mockapi.property;

import com.furniturebid.mockapi.dto.response.AuctionTrendDto;
import com.furniturebid.mockapi.service.AdminService;
import com.furniturebid.mockapi.store.MockDataStore;
import net.jqwik.api.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Feature: mock-api-service, Property 8: Auction Trends Day Count
// Validates: Requirements 13.2
class AuctionTrendsPropertyTest {

    private AdminService createAdminService() {
        MockDataStore dataStore = new MockDataStore();
        return new AdminService(dataStore);
    }

    @Property
    void auctionTrendsReturnsExactlyOnEntryPerDay(
            @ForAll("validDateRanges") DateRange dateRange
    ) {
        AdminService adminService = createAdminService();

        List<AuctionTrendDto> trends = adminService.getAuctionTrends(
                dateRange.startDate().toString(),
                dateRange.endDate().toString()
        );

        long expectedDays = ChronoUnit.DAYS.between(dateRange.startDate(), dateRange.endDate()) + 1;

        // Verify: array length == endDate - startDate + 1 (inclusive days)
        assertThat(trends).hasSize((int) expectedDays);
    }

    @Property
    void auctionTrendsEntriesHaveValidIsoDates(
            @ForAll("validDateRanges") DateRange dateRange
    ) {
        AdminService adminService = createAdminService();

        List<AuctionTrendDto> trends = adminService.getAuctionTrends(
                dateRange.startDate().toString(),
                dateRange.endDate().toString()
        );

        // Verify: each entry has a valid ISO 8601 date string
        for (AuctionTrendDto trend : trends) {
            assertThat(trend.getDate()).isNotNull();
            assertThat(trend.getDate()).isNotBlank();

            // Attempt to parse as ISO 8601 date — should not throw
            LocalDate parsed = LocalDate.parse(trend.getDate(), DateTimeFormatter.ISO_LOCAL_DATE);
            assertThat(parsed).isNotNull();
        }
    }

    @Property
    void auctionTrendsEntriesHaveNonNegativeValues(
            @ForAll("validDateRanges") DateRange dateRange
    ) {
        AdminService adminService = createAdminService();

        List<AuctionTrendDto> trends = adminService.getAuctionTrends(
                dateRange.startDate().toString(),
                dateRange.endDate().toString()
        );

        // Verify: auctionsCreated and auctionsCompleted are non-negative integers
        for (AuctionTrendDto trend : trends) {
            assertThat(trend.getAuctionsCreated())
                    .as("auctionsCreated for date %s", trend.getDate())
                    .isGreaterThanOrEqualTo(0);
            assertThat(trend.getAuctionsCompleted())
                    .as("auctionsCompleted for date %s", trend.getDate())
                    .isGreaterThanOrEqualTo(0);
        }
    }

    @Provide
    Arbitrary<DateRange> validDateRanges() {
        // Generate a start date within a reasonable range, then an offset of 0-364 days
        return Arbitraries.integers().between(0, 3650).flatMap(startOffset ->
                Arbitraries.integers().between(0, 364).map(rangeLength -> {
                    LocalDate baseDate = LocalDate.of(2020, 1, 1).plusDays(startOffset);
                    LocalDate endDate = baseDate.plusDays(rangeLength);
                    return new DateRange(baseDate, endDate);
                })
        );
    }

    record DateRange(LocalDate startDate, LocalDate endDate) {}
}
