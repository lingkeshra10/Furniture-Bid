package com.furniturebid.mockapi.property;

import com.furniturebid.mockapi.dto.response.PlaceBidResponse;
import com.furniturebid.mockapi.entity.FurnitureListingEntity;
import com.furniturebid.mockapi.entity.UserEntity;
import com.furniturebid.mockapi.service.AuctionService;
import com.furniturebid.mockapi.store.MockDataStore;
import net.jqwik.api.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// Feature: mock-api-service, Property 3: Bid Amount Threshold
// Validates: Requirements 5.1, 5.2
class BidThresholdPropertyTest {

    private static final BigDecimal MIN_INCREMENT = new BigDecimal("5.00");
    private static final UUID TEST_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TEST_AUCTION_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private MockDataStore createDataStore(BigDecimal currentBid) {
        MockDataStore dataStore = new MockDataStore();

        // Add a test user
        UserEntity user = new UserEntity(
                TEST_USER_ID,
                "test@test.com",
                "TestPass1",
                "Test User",
                "buyer",
                null,
                "active",
                Instant.now()
        );
        dataStore.getUsers().put(TEST_USER_ID.toString(), user);

        // Add an active listing with the given currentBid
        FurnitureListingEntity listing = new FurnitureListingEntity(
                TEST_AUCTION_ID,
                "Test Furniture",
                "A test furniture listing for property testing",
                "sofa",
                "good",
                "TestBrand",
                "Wood",
                null,
                25.0,
                "New York",
                List.of("image1.jpg"),
                new BigDecimal("10.00"),
                new BigDecimal("1000.00"),
                currentBid,
                3,
                Instant.now().plus(7, ChronoUnit.DAYS),
                "active",
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                "Seller One",
                4.5,
                Instant.now().minus(5, ChronoUnit.DAYS)
        );
        dataStore.getListings().put(TEST_AUCTION_ID.toString(), listing);

        return dataStore;
    }

    @Property
    void bidsAtOrAboveThresholdSucceed(
            @ForAll("currentBidValues") BigDecimal currentBid,
            @ForAll("positiveOffset") BigDecimal offset
    ) {
        // amount = currentBid + 5.00 + offset (offset >= 0)
        BigDecimal amount = currentBid.add(MIN_INCREMENT).add(offset);

        MockDataStore dataStore = createDataStore(currentBid);
        AuctionService auctionService = new AuctionService(dataStore);

        PlaceBidResponse response = auctionService.placeBid(TEST_USER_ID.toString(), TEST_AUCTION_ID.toString(), amount);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getBid()).isNotNull();
        assertThat(response.getBid().getAmount()).isEqualByComparingTo(amount);
        assertThat(response.getBid().getAuctionId()).isEqualTo(TEST_AUCTION_ID);
        assertThat(response.getBid().getBidderId()).isEqualTo(TEST_USER_ID);
        assertThat(response.getError()).isNull();
    }

    @Property
    void bidsBelowThresholdFail(
            @ForAll("currentBidValues") BigDecimal currentBid,
            @ForAll("belowThresholdOffset") BigDecimal belowAmount
    ) {
        // belowAmount is in range (0.01, currentBid + 4.99) — always below currentBid + 5.00
        // We generate it as: currentBid + belowAmount where belowAmount is in [0.01, 4.99]
        BigDecimal amount = currentBid.add(belowAmount);

        MockDataStore dataStore = createDataStore(currentBid);
        AuctionService auctionService = new AuctionService(dataStore);

        PlaceBidResponse response = auctionService.placeBid(TEST_USER_ID.toString(), TEST_AUCTION_ID.toString(), amount);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getError()).isNotNull();
        assertThat(response.getError()).isNotBlank();
        assertThat(response.getBid()).isNull();
    }

    @Provide
    Arbitrary<BigDecimal> currentBidValues() {
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("10.00"), new BigDecimal("10000.00"))
                .ofScale(2);
    }

    @Provide
    Arbitrary<BigDecimal> positiveOffset() {
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("0.00"), new BigDecimal("5000.00"))
                .ofScale(2);
    }

    @Provide
    Arbitrary<BigDecimal> belowThresholdOffset() {
        // Generates values from 0.01 to 4.99 — adding this to currentBid gives
        // an amount that is still less than currentBid + 5.00
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("0.01"), new BigDecimal("4.99"))
                .ofScale(2);
    }
}
