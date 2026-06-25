package com.furniturebid.mockapi.store;

import com.furniturebid.mockapi.entity.*;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Seeds the MockDataStore with realistic sample data on application startup.
 */
@Component
public class MockDataSeeder implements ApplicationRunner {

    private final MockDataStore dataStore;
    
    // User IDs
    private static final UUID USER_ID_1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID USER_ID_2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID USER_ID_3 = UUID.fromString("33333333-3333-3333-3333-333333333333");
    
    // Listing IDs
    private static final UUID LISTING_ID_1 = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID LISTING_ID_2 = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID LISTING_ID_3 = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final UUID LISTING_ID_4 = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    private static final UUID LISTING_ID_5 = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");
    private static final UUID LISTING_ID_6 = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
    private static final UUID LISTING_ID_7 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID LISTING_ID_8 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID LISTING_ID_9 = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID LISTING_ID_10 = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final UUID LISTING_ID_11 = UUID.fromString("00000000-0000-0000-0000-000000000005");

    public MockDataSeeder(MockDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedUsers();
        seedListings();
        seedBids();
        seedNotifications();
        seedPayments();
    }

    private void seedUsers() {
        Instant now = Instant.now();

        dataStore.getUsers().put(USER_ID_1.toString(), new UserEntity(
                USER_ID_1, "buyer@example.com", "SecurePass1", "John Buyer",
                "buyer", "https://api.dicebear.com/7.x/avataaars/svg?seed=buyer",
                "active", now.minus(30, ChronoUnit.DAYS)
        ));

        dataStore.getUsers().put(USER_ID_2.toString(), new UserEntity(
                USER_ID_2, "seller@example.com", "SecurePass1", "Jane Seller",
                "seller", "https://api.dicebear.com/7.x/avataaars/svg?seed=seller",
                "active", now.minus(60, ChronoUnit.DAYS)
        ));

        dataStore.getUsers().put(USER_ID_3.toString(), new UserEntity(
                USER_ID_3, "admin@example.com", "SecurePass1", "Admin User",
                "admin", "https://api.dicebear.com/7.x/avataaars/svg?seed=admin",
                "active", now.minus(90, ChronoUnit.DAYS)
        ));
    }

    private void seedListings() {
        Instant now = Instant.now();

        // Active listings (future end dates) - 5 active
        dataStore.getListings().put(LISTING_ID_1.toString(), new FurnitureListingEntity(
                LISTING_ID_1,
                "Mid-Century Modern Sofa",
                "Beautiful mid-century modern sofa in excellent condition. Tufted cushions with walnut legs. Seats 3 comfortably.",
                "sofa", "like-new", "West Elm", "Velvet",
                new Dimensions(210.0, 85.0, 90.0), 45.0,
                "New York, NY",
                List.of("https://picsum.photos/seed/sofa1/800/600", "https://picsum.photos/seed/sofa1b/800/600"),
                new BigDecimal("450.00"), new BigDecimal("800.00"), new BigDecimal("550.00"),
                3, now.plus(2, ChronoUnit.DAYS), "active",
                USER_ID_2, "Jane Seller", 4.8, now.minus(5, ChronoUnit.DAYS)
        ));

        dataStore.getListings().put(LISTING_ID_2.toString(), new FurnitureListingEntity(
                LISTING_ID_2,
                "Solid Oak Dining Table with 6 Chairs",
                "Handcrafted solid oak dining table set. Table extends to seat 8. Chairs have cushioned seats.",
                "dining-table", "good", "Pottery Barn", "Oak",
                new Dimensions(180.0, 76.0, 100.0), 85.0,
                "Los Angeles, CA",
                List.of("https://picsum.photos/seed/dining1/800/600", "https://picsum.photos/seed/dining1b/800/600"),
                new BigDecimal("600.00"), new BigDecimal("1200.00"), new BigDecimal("750.00"),
                5, now.plus(3, ChronoUnit.DAYS), "active",
                USER_ID_2, "Jane Seller", 4.8, now.minus(7, ChronoUnit.DAYS)
        ));

        dataStore.getListings().put(LISTING_ID_3.toString(), new FurnitureListingEntity(
                LISTING_ID_3,
                "Herman Miller Aeron Office Chair",
                "Size B Herman Miller Aeron chair. Fully loaded with all adjustments. PostureFit SL support.",
                "office-chair", "like-new", "Herman Miller", "Mesh/Aluminum",
                new Dimensions(68.0, 110.0, 65.0), 20.0,
                "San Francisco, CA",
                List.of("https://picsum.photos/seed/chair1/800/600"),
                new BigDecimal("400.00"), new BigDecimal("900.00"), new BigDecimal("520.00"),
                4, now.plus(5, ChronoUnit.DAYS), "active",
                USER_ID_2, "Jane Seller", 4.8, now.minus(3, ChronoUnit.DAYS)
        ));

        dataStore.getListings().put(LISTING_ID_4.toString(), new FurnitureListingEntity(
                LISTING_ID_4,
                "IKEA PAX Wardrobe System",
                "Complete PAX wardrobe system with sliding mirror doors. Includes internal organizers and drawers.",
                "wardrobe", "good", "IKEA", "Particleboard",
                new Dimensions(200.0, 236.0, 60.0), 120.0,
                "Chicago, IL",
                List.of("https://picsum.photos/seed/wardrobe1/800/600", "https://picsum.photos/seed/wardrobe1b/800/600"),
                new BigDecimal("200.00"), new BigDecimal("500.00"), new BigDecimal("280.00"),
                2, now.plus(4, ChronoUnit.DAYS), "active",
                USER_ID_2, "Jane Seller", 4.8, now.minus(2, ChronoUnit.DAYS)
        ));

        dataStore.getListings().put(LISTING_ID_5.toString(), new FurnitureListingEntity(
                LISTING_ID_5,
                "Rustic Reclaimed Wood Coffee Table",
                "Stunning coffee table made from reclaimed barn wood. Iron hairpin legs. Each piece is unique.",
                "coffee-table", "new", "Handcrafted", "Reclaimed Wood/Iron",
                new Dimensions(120.0, 45.0, 60.0), 25.0,
                "Portland, OR",
                List.of("https://picsum.photos/seed/coffee1/800/600"),
                new BigDecimal("150.00"), new BigDecimal("400.00"), new BigDecimal("200.00"),
                2, now.plus(6, ChronoUnit.DAYS), "active",
                USER_ID_2, "Jane Seller", 4.8, now.minus(1, ChronoUnit.DAYS)
        ));

        // Ended listings (past end dates) - 3 ended
        dataStore.getListings().put(LISTING_ID_6.toString(), new FurnitureListingEntity(
                LISTING_ID_6,
                "Queen Platform Bed Frame with Storage",
                "Modern queen platform bed frame with 4 under-bed storage drawers. Upholstered headboard.",
                "bed-frame", "new", "Article", "Engineered Wood/Fabric",
                new Dimensions(160.0, 120.0, 210.0), 65.0,
                "Seattle, WA",
                List.of("https://picsum.photos/seed/bed1/800/600", "https://picsum.photos/seed/bed1b/800/600"),
                new BigDecimal("300.00"), new BigDecimal("700.00"), new BigDecimal("680.00"),
                8, now.minus(1, ChronoUnit.DAYS), "ended",
                USER_ID_2, "Jane Seller", 4.8, now.minus(14, ChronoUnit.DAYS)
        ));

        dataStore.getListings().put(LISTING_ID_7.toString(), new FurnitureListingEntity(
                LISTING_ID_7,
                "Antique Mahogany Bookshelf",
                "Stunning antique mahogany bookshelf from the 1920s. Five shelves with ornate carved details.",
                "bookshelf", "fair", "Antique", "Mahogany",
                new Dimensions(100.0, 200.0, 35.0), 55.0,
                "Boston, MA",
                List.of("https://picsum.photos/seed/bookshelf1/800/600"),
                new BigDecimal("250.00"), new BigDecimal("600.00"), new BigDecimal("450.00"),
                6, now.minus(3, ChronoUnit.DAYS), "ended",
                USER_ID_2, "Jane Seller", 4.8, now.minus(17, ChronoUnit.DAYS)
        ));

        dataStore.getListings().put(LISTING_ID_8.toString(), new FurnitureListingEntity(
                LISTING_ID_8,
                "Modern Glass Cabinet Display Case",
                "Tempered glass display cabinet with LED lighting. Four shelves, lockable doors.",
                "cabinet", "like-new", "CB2", "Glass/Metal",
                new Dimensions(80.0, 180.0, 40.0), 40.0,
                "Austin, TX",
                List.of("https://picsum.photos/seed/cabinet1/800/600"),
                new BigDecimal("180.00"), new BigDecimal("450.00"), new BigDecimal("350.00"),
                4, now.minus(5, ChronoUnit.DAYS), "ended",
                USER_ID_2, "Jane Seller", 4.8, now.minus(19, ChronoUnit.DAYS)
        ));

        // Flagged listings - 2 flagged
        dataStore.getListings().put(LISTING_ID_9.toString(), new FurnitureListingEntity(
                LISTING_ID_9,
                "Vintage Leather Sofa",
                "Vintage leather sofa with minor wear. Rich patina, very comfortable.",
                "sofa", "fair", "Unknown", "Leather",
                new Dimensions(220.0, 80.0, 95.0), 60.0,
                "Miami, FL",
                List.of("https://picsum.photos/seed/sofa2/800/600"),
                new BigDecimal("100.00"), new BigDecimal("300.00"), new BigDecimal("150.00"),
                1, now.plus(1, ChronoUnit.DAYS), "flagged",
                USER_ID_2, "Jane Seller", 4.8, now.minus(4, ChronoUnit.DAYS)
        ));

        dataStore.getListings().put(LISTING_ID_10.toString(), new FurnitureListingEntity(
                LISTING_ID_10,
                "Bamboo Standing Desk",
                "Adjustable height bamboo standing desk. Electric motor for smooth transitions.",
                "office-chair", "new", "FlexiSpot", "Bamboo/Steel",
                new Dimensions(140.0, 120.0, 70.0), 35.0,
                "Denver, CO",
                List.of("https://picsum.photos/seed/desk1/800/600"),
                new BigDecimal("250.00"), new BigDecimal("550.00"), new BigDecimal("300.00"),
                2, now.plus(2, ChronoUnit.DAYS), "flagged",
                USER_ID_2, "Jane Seller", 4.8, now.minus(6, ChronoUnit.DAYS)
        ));

        // Additional active listing from a different seller to add variety
        dataStore.getListings().put(LISTING_ID_11.toString(), new FurnitureListingEntity(
                LISTING_ID_11,
                "Scandinavian Teak Bookshelf",
                "Minimalist Scandinavian design teak bookshelf. Six adjustable shelves. Wall-mountable.",
                "bookshelf", "new", "Muuto", "Teak",
                new Dimensions(90.0, 180.0, 30.0), 28.0,
                "Minneapolis, MN",
                List.of("https://picsum.photos/seed/bookshelf2/800/600"),
                new BigDecimal("200.00"), new BigDecimal("500.00"), new BigDecimal("250.00"),
                1, now.plus(7, ChronoUnit.DAYS), "active",
                USER_ID_1, "John Buyer", 4.2, now.minus(2, ChronoUnit.DAYS)
        ));
    }

    private void seedBids() {
        Instant now = Instant.now();

        // Bids for listing-1 (3 bids, 2 from buyer, 1 from admin)
        List<BidEntity> listing1Bids = new ArrayList<>();
        listing1Bids.add(new BidEntity(
                UUID.fromString("44444444-4444-4444-4444-444444444444"), LISTING_ID_1, USER_ID_1, "John B.",
                new BigDecimal("480.00"), now.minus(4, ChronoUnit.DAYS)
        ));
        listing1Bids.add(new BidEntity(
                UUID.fromString("44444444-4444-4444-4444-444444444445"), LISTING_ID_1, USER_ID_3, "Admin U.",
                new BigDecimal("520.00"), now.minus(3, ChronoUnit.DAYS)
        ));
        listing1Bids.add(new BidEntity(
                UUID.fromString("44444444-4444-4444-4444-444444444446"), LISTING_ID_1, USER_ID_1, "John B.",
                new BigDecimal("550.00"), now.minus(2, ChronoUnit.DAYS)
        ));
        dataStore.getBidsByAuction().put(LISTING_ID_1.toString(), listing1Bids);

        // Bids for listing-2 (3 bids, 2 from buyer, 1 from admin)
        List<BidEntity> listing2Bids = new ArrayList<>();
        listing2Bids.add(new BidEntity(
                UUID.fromString("55555555-5555-5555-5555-555555555555"), LISTING_ID_2, USER_ID_1, "John B.",
                new BigDecimal("650.00"), now.minus(6, ChronoUnit.DAYS)
        ));
        listing2Bids.add(new BidEntity(
                UUID.fromString("55555555-5555-5555-5555-555555555556"), LISTING_ID_2, USER_ID_3, "Admin U.",
                new BigDecimal("700.00"), now.minus(5, ChronoUnit.DAYS)
        ));
        listing2Bids.add(new BidEntity(
                UUID.fromString("55555555-5555-5555-5555-555555555557"), LISTING_ID_2, USER_ID_1, "John B.",
                new BigDecimal("750.00"), now.minus(4, ChronoUnit.DAYS)
        ));
        dataStore.getBidsByAuction().put(LISTING_ID_2.toString(), listing2Bids);

        // Bids for listing-6 (ended auction, mixed bidders)
        List<BidEntity> listing6Bids = new ArrayList<>();
        listing6Bids.add(new BidEntity(
                UUID.fromString("66666666-6666-6666-6666-666666666666"), LISTING_ID_6, USER_ID_1, "John B.",
                new BigDecimal("350.00"), now.minus(10, ChronoUnit.DAYS)
        ));
        listing6Bids.add(new BidEntity(
                UUID.fromString("66666666-6666-6666-6666-666666666667"), LISTING_ID_6, USER_ID_3, "Admin U.",
                new BigDecimal("680.00"), now.minus(2, ChronoUnit.DAYS)
        ));
        dataStore.getBidsByAuction().put(LISTING_ID_6.toString(), listing6Bids);
    }

    private void seedNotifications() {
        Instant now = Instant.now();

        List<NotificationEntity> buyerNotifications = new ArrayList<>();

        // bid_placed - read
        buyerNotifications.add(new NotificationEntity(
                UUID.fromString("77777777-7777-7777-7777-777777777777"), USER_ID_1, "bid_placed",
                "Bid Placed Successfully",
                "Your bid of $550.00 was placed on Mid-Century Modern Sofa.",
                LISTING_ID_1, true, now.minus(2, ChronoUnit.DAYS)
        ));

        // outbid - unread
        buyerNotifications.add(new NotificationEntity(
                UUID.fromString("77777777-7777-7777-7777-777777777778"), USER_ID_1, "outbid",
                "You've Been Outbid",
                "Someone placed a higher bid on Solid Oak Dining Table with 6 Chairs. Current bid: $750.00.",
                LISTING_ID_2, false, now.minus(1, ChronoUnit.DAYS)
        ));

        // auction_won - read
        buyerNotifications.add(new NotificationEntity(
                UUID.fromString("77777777-7777-7777-7777-777777777779"), USER_ID_1, "auction_won",
                "Congratulations! You Won",
                "You won the auction for Queen Platform Bed Frame with Storage at $680.00.",
                LISTING_ID_6, true, now.minus(1, ChronoUnit.DAYS)
        ));

        // auction_lost - unread
        buyerNotifications.add(new NotificationEntity(
                UUID.fromString("77777777-7777-7777-7777-777777777780"), USER_ID_1, "auction_lost",
                "Auction Ended",
                "The auction for Antique Mahogany Bookshelf has ended. You were outbid.",
                LISTING_ID_7, false, now.minus(3, ChronoUnit.DAYS)
        ));

        // auction_ending - unread
        buyerNotifications.add(new NotificationEntity(
                UUID.fromString("77777777-7777-7777-7777-777777777781"), USER_ID_1, "auction_ending",
                "Auction Ending Soon",
                "The auction for Mid-Century Modern Sofa ends in 2 hours. Current bid: $550.00.",
                LISTING_ID_1, false, now.minus(6, ChronoUnit.HOURS)
        ));

        // payment_received - read
        buyerNotifications.add(new NotificationEntity(
                UUID.fromString("77777777-7777-7777-7777-777777777782"), USER_ID_1, "payment_received",
                "Payment Confirmed",
                "Your payment of $680.00 for Queen Platform Bed Frame with Storage has been confirmed.",
                LISTING_ID_6, true, now.minus(12, ChronoUnit.HOURS)
        ));

        dataStore.getNotificationsByUser().put(USER_ID_1.toString(), buyerNotifications);
    }

    private void seedPayments() {
        // Payment 1 - succeeded (for ended auction listing-6)
        dataStore.getPayments().put(UUID.fromString("88888888-8888-8888-8888-888888888888").toString(), new PaymentEntity(
                UUID.fromString("88888888-8888-8888-8888-888888888888"), USER_ID_1, LISTING_ID_6,
                "pi_mock_secret_succeeded_001",
                new BigDecimal("680.00"), "USD", "succeeded",
                Instant.now().minus(1, ChronoUnit.DAYS)
        ));

        // Payment 2 - requires_payment_method (for a pending auction win)
        dataStore.getPayments().put(UUID.fromString("88888888-8888-8888-8888-888888888889").toString(), new PaymentEntity(
                UUID.fromString("88888888-8888-8888-8888-888888888889"), USER_ID_1, LISTING_ID_8,
                "pi_mock_secret_pending_002",
                new BigDecimal("350.00"), "USD", "requires_payment_method",
                Instant.now().minus(4, ChronoUnit.DAYS)
        ));
    }
}
