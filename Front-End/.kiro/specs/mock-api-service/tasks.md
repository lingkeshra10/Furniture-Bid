# Implementation Plan: Mock API Service

## Overview

This plan implements a Spring Boot (Java 17+) Mock API Service at `/Users/lingkeshrarajendram/Documents/Project/Furniture Bid/Source Code/Back-End/`. The service provides all REST endpoints and WebSocket events consumed by the Furniture Bid front-end, using in-memory data structures with pre-seeded mock data. Implementation follows a layered approach: project setup → data layer → security → controllers → WebSocket → integration.

## Tasks

- [x] 1. Set up Spring Boot project structure and configuration
  - [x] 1.1 Initialize Maven project with Spring Boot 3.x parent, Java 17, and dependencies (spring-boot-starter-web, spring-boot-starter-websocket, jjwt, netty-socketio, jqwik for testing, spring-boot-starter-test)
    - Create `pom.xml` at the Back-End root with all required dependencies
    - Create `src/main/java/com/furniturebid/mockapi/MockApiApplication.java` main class with `@SpringBootApplication`
    - Create `src/main/resources/application.properties` with `server.port=8080`
    - _Requirements: 1.1, 1.4_

  - [x] 1.2 Implement CORS configuration and base path setup
    - Create `com.furniturebid.mockapi.config.CorsConfig.java` with `@Configuration` allowing all origins, methods, headers, and credentials
    - Verify all REST endpoints are served under `/api` base path
    - _Requirements: 1.2, 1.3, 1.6_

  - [x] 1.3 Create entity classes for all data models
    - Create `entity/UserEntity.java`, `entity/FurnitureListingEntity.java`, `entity/BidEntity.java`, `entity/NotificationEntity.java`, `entity/PaymentEntity.java`, `entity/AutoBidConfig.java`, `entity/ListingReportEntity.java`, `entity/Dimensions.java`
    - Include all fields as defined in the design document
    - _Requirements: 1.5_

  - [x] 1.4 Create request and response DTO classes
    - Create all request DTOs: `LoginRequest`, `RegisterRequest`, `PlaceBidRequest`, `AutoBidRequest`, `CreatePaymentIntentRequest`, `ConfirmPaymentRequest`, `FlagRequest`, `SocialLoginRequest`
    - Create all response DTOs: `LoginResponse`, `UserDto`, `PaginatedResponse<T>`, `PlaceBidResponse`, `BidDto`, `FurnitureListingSummaryDto`, `FurnitureListingDto`, `SellerActiveListingDto`, `SellerCompletedAuctionDto`, `NotificationDto`, `PaymentIntentDto`, `PaymentRecordDto`, `AdminUserRowDto`, `AdminListingRowDto`, `ListingReportDto`, `AnalyticsSummaryDto`, `AuctionTrendDto`, `CategoryDistributionDto`, `TopSellerDto`, `ApiErrorResponse`
    - Add Bean Validation annotations (`@NotBlank`, `@Size`, `@Email`, `@Pattern`) on request DTOs
    - _Requirements: 2.3, 2.5, 15.1, 15.2_

  - [x] 1.5 Create custom exception classes and global exception handler
    - Create exception classes: `InvalidCredentialsException`, `UnauthorizedException`, `TokenExpiredException`, `ForbiddenException`, `NotFoundException`, `ConflictException`, `ValidationException`, `BidTooLowException`, `AuctionEndedException`
    - Create `GlobalExceptionHandler` as `@RestControllerAdvice` mapping each exception to the standardized `ApiErrorResponse` format
    - _Requirements: 15.1, 15.2, 15.3, 15.4, 15.5, 15.6, 15.7, 15.8, 15.9, 15.10_

- [x] 2. Implement data layer and mock data seeding
  - [x] 2.1 Implement MockDataStore component
    - Create `store/MockDataStore.java` as a `@Component` with `ConcurrentHashMap` collections for users, listings, bids, watchlists, notifications, payments, autoBids, and reports
    - Provide helper methods for common queries (get by ID, filter, paginate)
    - _Requirements: 1.5_

  - [x] 2.2 Implement MockDataSeeder with realistic sample data
    - Create `store/MockDataSeeder.java` as an `ApplicationRunner`
    - Seed 3+ users (buyer: `buyer@example.com`/`SecurePass1`, seller: `seller@example.com`/`SecurePass1`, admin: `admin@example.com`/`SecurePass1`)
    - Seed 10+ furniture listings across 4+ categories and 3+ conditions (3+ active, 2+ ended, 1+ flagged), assigning 3+ to the seller user
    - Seed 5+ bids across 3+ listings with 2+ from the buyer
    - Seed 5+ notifications for the buyer covering 4+ notification types with 2+ unread
    - Seed 2+ payment records (1 succeeded, 1 requires_payment_method)
    - _Requirements: 16.1, 16.2, 16.3, 16.4, 16.5, 16.6, 16.7_

- [x] 3. Implement JWT security layer
  - [x] 3.1 Implement JwtUtility class
    - Create `security/JwtUtility.java` using jjwt library with hardcoded HMAC-SHA256 signing key
    - Implement `generateToken(userId, role)` with 1hr (3600s) expiry
    - Implement `parseToken(token)` that throws on invalid/expired tokens
    - Implement `parseTokenAllowExpired(token)` for refresh-token flow
    - Implement `isTokenExpired(claims)` helper
    - _Requirements: 3.1, 3.2, 3.5_

  - [x] 3.2 Write property test for JWT Token Round-Trip
    - **Property 1: JWT Token Round-Trip**
    - For any valid userId and role, generating and parsing a token produces matching claims with exp = iat + 3600
    - **Validates: Requirements 3.1, 3.2**

  - [x] 3.3 Implement JwtAuthFilter
    - Create `security/JwtAuthFilter.java` extending `OncePerRequestFilter`
    - Skip unauthenticated endpoints (login, register, reset-password, social-login, GET /api/furniture, GET /api/furniture/{id}, GET /api/auctions/{auctionId}/bids)
    - For `/api/auth/refresh-token`: use `parseTokenAllowExpired`
    - For all other protected endpoints: reject expired tokens with `TOKEN_EXPIRED`
    - Set authenticated user context (userId, role) on valid tokens
    - Create `security/AuthenticatedUser.java` to hold user context
    - _Requirements: 3.2, 3.3, 3.4, 3.5_

  - [x] 3.4 Implement SecurityConfig
    - Create `config/SecurityConfig.java` to register the `JwtAuthFilter` and configure endpoint security
    - _Requirements: 3.2, 3.3_

- [x] 4. Checkpoint - Verify project builds and security layer works
  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. Implement Identity Controller and Auth Service
  - [x] 5.1 Implement AuthService
    - Create `service/AuthService.java` with login (credential matching), register (validation + user creation), resetPassword (no-op 204), refreshToken (new token for userId), socialLogin (find/create user)
    - Validate registration fields: email format, password 8-64 chars with uppercase+lowercase+digit, displayName 3-50 chars
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9, 2.10_

  - [x] 5.2 Implement IdentityController
    - Create `controller/IdentityController.java` with `@RequestMapping("/api/auth")`
    - POST `/login` - authenticate and return token + user
    - POST `/register` - validate, create user, return token + user
    - POST `/reset-password` - return 204 regardless of email existence
    - POST `/refresh-token` - generate new token from existing user context
    - POST `/social-login` - validate provider (google/facebook), return token + user
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9, 2.10_

  - [x] 5.3 Write property test for Registration Input Validation
    - **Property 2: Registration Input Validation**
    - For any valid input combination → 200 with token and user (role=buyer); for any invalid input → 400 with fieldErrors
    - **Validates: Requirements 2.3, 2.5**

- [x] 6. Implement User Profile Controller and User Service
  - [x] 6.1 Implement UserService
    - Create `service/UserService.java` with getProfile, updateProfile (with validation), getWatchlist (paginated), addToWatchlist, removeFromWatchlist
    - Validate displayName (3-50 chars), avatarUrl (max 2048 chars)
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8_

  - [x] 6.2 Implement UserProfileController
    - Create `controller/UserProfileController.java` with `@RequestMapping("/api/users")`
    - GET `/profile` - return authenticated user
    - PUT `/profile` - update displayName/avatarUrl
    - GET `/watchlist` - paginated watchlist
    - POST `/watchlist/{listingId}` - add to watchlist
    - DELETE `/watchlist/{listingId}` - remove from watchlist
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8_

- [x] 7. Implement Auction Controller and Auction Service
  - [x] 7.1 Implement AuctionService
    - Create `service/AuctionService.java` with placeBid (amount validation with min increment of 5.00), getBidHistory (paginated, descending), setAutoBid, removeAutoBid, getActiveListings (seller-scoped), getCompletedAuctions (seller-scoped)
    - Validate bid amount >= currentBid + 5.00, return success:false with error message if below
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 6.1, 6.2, 6.3_

  - [x] 7.2 Implement AuctionController
    - Create `controller/AuctionController.java` with `@RequestMapping("/api/auctions")` and `@RequestMapping("/api/seller")`
    - POST `/auctions/bids` - place bid
    - GET `/auctions/{auctionId}/bids` - bid history (public)
    - POST `/auctions/{auctionId}/auto-bid` - activate auto-bid
    - DELETE `/auctions/{auctionId}/auto-bid` - deactivate auto-bid
    - GET `/seller/active-listings` - seller's active listings (seller/admin only)
    - GET `/seller/completed-auctions` - seller's completed auctions (seller/admin only)
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 6.1, 6.2, 6.3_

  - [x] 7.3 Write property test for Bid Amount Threshold
    - **Property 3: Bid Amount Threshold**
    - For any amount >= currentBid + 5.00 → success:true; for any amount < currentBid + 5.00 → success:false with error
    - **Validates: Requirements 5.1, 5.2**

- [x] 8. Implement Furniture Controller and Furniture Service
  - [x] 8.1 Implement FurnitureService
    - Create `service/FurnitureService.java` with getCatalog (filtering by category, condition, price range, location + sorting + pagination), getListingById, createListing (multipart), flagListing, deleteListing (ownership check)
    - Support sort values: ending-soonest (default), price-low-high, price-high-low, newest
    - Filter: multiple comma-separated categories/conditions, priceMin/priceMax range, location substring (case-insensitive)
    - Return empty array when priceMin > priceMax
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8, 8.1, 8.2, 8.3, 8.4, 8.5, 8.6_

  - [x] 8.2 Implement FurnitureController
    - Create `controller/FurnitureController.java` with `@RequestMapping("/api/furniture")`
    - GET `/` - catalog with filtering/sorting/pagination (public)
    - GET `/{id}` - listing detail (public)
    - POST `/` - create listing (multipart, seller/admin only)
    - PUT `/{id}/flag` - flag listing (authenticated)
    - DELETE `/{id}` - remove listing (owner/admin only)
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8, 8.1, 8.2, 8.3, 8.4, 8.5, 8.6_

  - [x] 8.3 Write property test for Catalog Filtering Correctness
    - **Property 4: Catalog Filtering Correctness**
    - For any filter combination, all returned items have status=active and satisfy all filter constraints
    - **Validates: Requirements 7.1, 7.3, 7.4, 7.5, 7.6**

  - [x] 8.4 Write property test for Catalog Sort Order
    - **Property 5: Catalog Sort Order**
    - For any valid sort value, returned items are ordered according to the sort criteria
    - **Validates: Requirements 7.2**

- [x] 9. Checkpoint - Verify core REST endpoints work
  - Ensure all tests pass, ask the user if questions arise.

- [x] 10. Implement Notification Controller and Notification Service
  - [x] 10.1 Implement NotificationService
    - Create `service/NotificationService.java` with getNotifications (paginated, descending by createdAt, user-scoped), markAsRead, markAllAsRead
    - _Requirements: 9.1, 9.2, 9.3, 9.4_

  - [x] 10.2 Implement NotificationController
    - Create `controller/NotificationController.java` with `@RequestMapping("/api/notifications")`
    - GET `/` - paginated notifications for authenticated user
    - PUT `/{id}/read` - mark single notification as read
    - PUT `/read-all` - mark all notifications as read
    - _Requirements: 9.1, 9.2, 9.3, 9.4_

- [x] 11. Implement Payment Controller and Payment Service
  - [x] 11.1 Implement PaymentService
    - Create `service/PaymentService.java` with createPaymentIntent (generates ID, clientSecret, status=requires_payment_method, currency=USD), confirmPayment (update status to succeeded), getPaymentHistory (paginated, descending by createdAt)
    - _Requirements: 10.1, 10.2, 10.3, 10.4_

  - [x] 11.2 Implement PaymentController
    - Create `controller/PaymentController.java` with `@RequestMapping("/api/payments")`
    - POST `/create-payment-intent` - create payment intent
    - POST `/confirm-payment` - confirm payment
    - GET `/history` - payment history
    - _Requirements: 10.1, 10.2, 10.3, 10.4_

- [x] 12. Implement Admin Controller and Admin Service
  - [x] 12.1 Implement AdminService
    - Create `service/AdminService.java` with getUsers (paginated), suspendUser, activateUser, deleteUser, getListings (paginated), removeListing, flagListing, getReports (paginated, descending by reportDate), getAnalyticsSummary, getAuctionTrends (one entry per calendar day in range), getCategoryDistribution (exactly 8 entries), getTopSellers (at most 10, sorted by totalRevenue desc)
    - Validate date range parameters: valid ISO 8601 dates, startDate <= endDate
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 12.1, 12.2, 12.3, 12.4, 12.5, 13.1, 13.2, 13.3, 13.4, 13.5_

  - [x] 12.2 Implement AdminController
    - Create `controller/AdminController.java` with `@RequestMapping("/api/admin")`
    - GET `/users` - paginated user list (admin only)
    - PUT `/users/{userId}/suspend` - suspend user (admin only)
    - PUT `/users/{userId}/activate` - activate user (admin only)
    - DELETE `/users/{userId}` - delete user (admin only)
    - GET `/listings` - paginated listings (admin only)
    - DELETE `/listings/{listingId}` - remove listing (admin only)
    - PUT `/listings/{listingId}/flag` - flag listing (admin only)
    - GET `/listings/reports` - paginated reports (admin only)
    - GET `/analytics/summary` - analytics summary (admin only)
    - GET `/analytics/auction-trends` - auction trends (admin only)
    - GET `/analytics/category-distribution` - category distribution (admin only)
    - GET `/analytics/top-sellers` - top sellers (admin only)
    - Enforce admin role check, return 403 for non-admin tokens
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 12.1, 12.2, 12.3, 12.4, 12.5, 13.1, 13.2, 13.3, 13.4, 13.5_

  - [x] 12.3 Write property test for Auction Trends Day Count
    - **Property 8: Auction Trends Day Count**
    - For any valid date range, the array has exactly (endDate - startDate + 1) entries with valid dates and non-negative integers
    - **Validates: Requirements 13.2**

  - [x] 12.4 Write property test for Category Distribution Completeness
    - **Property 9: Category Distribution Completeness**
    - For any valid date range, the array contains exactly 8 entries (one per FurnitureCategory) with non-negative counts
    - **Validates: Requirements 13.3**

  - [x] 12.5 Write property test for Top Sellers Bounded and Sorted
    - **Property 10: Top Sellers Bounded and Sorted**
    - For any valid date range, the array has at most 10 entries sorted by totalRevenue descending
    - **Validates: Requirements 13.4**

- [x] 13. Implement Pagination utility and property tests
  - [x] 13.1 Implement shared pagination helper
    - Create a utility method in `MockDataStore` or a `PaginationHelper` class that handles offset calculation, hasMore logic, total count, and page/pageSize validation (page >= 1, pageSize 1-100)
    - Default page=1, pageSize=20 when not provided
    - Return 400 for invalid page/pageSize values
    - _Requirements: 17.1, 17.2, 17.3, 17.4, 17.5, 17.6, 17.7_

  - [x]* 13.2 Write property test for Pagination Correctness
    - **Property 6: Pagination Correctness**
    - For any total N, page >= 1, pageSize in [1,100]: data has correct items, total = N, hasMore is correct, empty data when offset > N
    - **Validates: Requirements 17.1, 17.4, 17.5, 17.6, 17.7**

  - [x] 13.3 Write property test for Chronological Descending Sort Order
    - **Property 7: Chronological Descending Sort Order**
    - For bid history, notifications, payments, and reports endpoints, items are sorted by timestamp descending
    - **Validates: Requirements 5.3, 9.1, 10.4, 12.4**

  - [x] 13.4 Write property test for Error Response Structure Consistency
    - **Property 11: Error Response Structure Consistency**
    - For any error condition, response has statusCode, errorCode, message (1-256 chars), and fieldErrors when errorCode is INVALID_REQUEST
    - **Validates: Requirements 15.1, 15.2**

- [x] 14. Checkpoint - Verify all REST endpoints work
  - Ensure all tests pass, ask the user if questions arise.

- [x] 15. Implement WebSocket layer
  - [x] 15.1 Implement SocketIO configuration
    - Create `config/SocketIOConfig.java` to configure netty-socketio server (port, CORS, etc.)
    - _Requirements: 14.1_

  - [x] 15.2 Implement SocketIOHandler
    - Create `websocket/SocketIOHandler.java`
    - Authenticate connections via JWT in `auth.token` handshake field, reject invalid/expired tokens
    - Handle `join:auction` and `leave:auction` events for auction subscriptions
    - Handle `subscribe:notifications` event for notification subscriptions
    - Clean up all subscriptions on disconnect
    - Implement broadcast methods: `broadcastBidUpdate`, `sendOutbidNotification`, `sendNotification`
    - Support `auction:ending`, `auction:won`, `auction:lost` event emission
    - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5, 14.6, 14.7, 14.8, 14.9, 14.10, 14.11, 14.12_

  - [x] 15.3 Wire WebSocket events to AuctionService
    - After successful bid placement in AuctionService, call `SocketIOHandler.broadcastBidUpdate()`
    - When a bid outbids another user, call `SocketIOHandler.sendOutbidNotification()`
    - _Requirements: 14.6, 14.7_

- [x] 16. Final integration and README
  - [x] 16.1 Create project README with setup instructions
    - Document how to build and run the service (`mvn spring-boot:run`)
    - Document mock user credentials (buyer@example.com, seller@example.com, admin@example.com with password SecurePass1)
    - Document available endpoints and WebSocket events
    - _Requirements: 16.7_

  - [x]* 16.2 Write integration tests for critical flows
    - Test full auth flow (register → login → refresh-token)
    - Test bid placement flow (login → place bid → verify bid history)
    - Test admin access control (buyer token → 403 on admin endpoints)
    - Test CORS headers present in responses
    - _Requirements: 1.2, 2.1, 5.1, 11.5_

- [x] 17. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design document
- Unit tests validate specific examples and edge cases
- The service uses netty-socketio for Socket.IO protocol compatibility with the Vue.js front-end
- All mock data is in-memory; restarting the service resets to seeded state
- JWT uses a hardcoded signing key since this is a mock service for development only

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["1.2", "1.3", "1.4", "1.5"] },
    { "id": 2, "tasks": ["2.1"] },
    { "id": 3, "tasks": ["2.2", "3.1"] },
    { "id": 4, "tasks": ["3.2", "3.3"] },
    { "id": 5, "tasks": ["3.4", "13.1"] },
    { "id": 6, "tasks": ["5.1", "6.1", "7.1", "8.1"] },
    { "id": 7, "tasks": ["5.2", "5.3", "6.2", "7.2", "7.3", "8.2", "8.3", "8.4"] },
    { "id": 8, "tasks": ["10.1", "11.1", "12.1"] },
    { "id": 9, "tasks": ["10.2", "11.2", "12.2", "12.3", "12.4", "12.5"] },
    { "id": 10, "tasks": ["13.2", "13.3", "13.4"] },
    { "id": 11, "tasks": ["15.1"] },
    { "id": 12, "tasks": ["15.2"] },
    { "id": 13, "tasks": ["15.3", "16.1"] },
    { "id": 14, "tasks": ["16.2"] }
  ]
}
```
