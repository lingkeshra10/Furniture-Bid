# Requirements Document

## Introduction

This document specifies the requirements for a Mock API Service that serves as the backend for the Furniture Bid front-end application during development. The service is built with Spring Boot (Java) and provides all REST API endpoints and WebSocket events that the front-end consumes, returning hardcoded in-memory mock data. The service resides at `/Users/lingkeshrarajendram/Documents/Project/Furniture Bid/Source Code/Back-End/` and implements the contracts defined in the front-end's API reference document.

## Glossary

- **Mock_API_Service**: The Spring Boot application that serves mock REST and WebSocket endpoints for the Furniture Bid front-end
- **Identity_Controller**: The REST controller handling authentication endpoints (login, register, reset-password, refresh-token, social-login)
- **User_Profile_Controller**: The REST controller handling user profile and watchlist endpoints
- **Auction_Controller**: The REST controller handling bid placement, bid history, auto-bid, and seller listing endpoints
- **Furniture_Controller**: The REST controller handling furniture catalog browsing, listing creation, flagging, and deletion
- **Notification_Controller**: The REST controller handling notification retrieval and read-status updates
- **Payment_Controller**: The REST controller handling payment intent creation, confirmation, and history
- **Admin_Controller**: The REST controller handling user management, listing management, and analytics endpoints
- **WebSocket_Handler**: The component handling real-time Socket.IO/STOMP connections and event broadcasting
- **Mock_Data_Store**: The in-memory data structure holding all mock entities (users, listings, bids, notifications, payments)
- **JWT_Utility**: The component responsible for generating and validating simplified JWT tokens
- **Paginated_Response**: A standardized response wrapper containing `data`, `total`, `page`, `pageSize`, and `hasMore` fields
- **FurnitureCategory**: One of: sofa, dining-table, office-chair, wardrobe, bed-frame, coffee-table, cabinet, bookshelf
- **FurnitureCondition**: One of: new, like-new, good, fair, poor
- **ListingStatus**: One of: active, ended, flagged, removed
- **UserRole**: One of: buyer, seller, admin
- **AccountStatus**: One of: active, suspended, deleted

## Requirements

### Requirement 1: Project Setup and Configuration

**User Story:** As a front-end developer, I want the mock API service to be a properly configured Spring Boot project with CORS enabled, so that I can make API calls from my local development environment without cross-origin issues.

#### Acceptance Criteria

1. THE Mock_API_Service SHALL be a Spring Boot application using Java 17 or later with Spring Web and Spring WebSocket dependencies
2. THE Mock_API_Service SHALL enable CORS for all origins, all HTTP methods, and all headers, including credentials support, to support local front-end development
3. THE Mock_API_Service SHALL serve all REST endpoints under the `/api` base path
4. THE Mock_API_Service SHALL listen on a configurable port (default 8080) specified via the `server.port` application property
5. THE Mock_API_Service SHALL use in-memory data structures to store mock data without requiring any external database
6. THE Mock_API_Service SHALL return all JSON responses with Content-Type `application/json`

### Requirement 2: Identity Service - Authentication Endpoints

**User Story:** As a front-end developer, I want the mock service to handle login, registration, password reset, token refresh, and social login requests, so that I can develop and test authentication flows in the front-end.

#### Acceptance Criteria

1. WHEN a POST request is received at `/api/auth/login` with an email and password matching a user in the Mock_Data_Store, THE Identity_Controller SHALL return a 200 response containing a JWT token string and a User object with id, email, displayName, role, optional avatarUrl, and createdAt fields
2. WHEN a POST request is received at `/api/auth/login` with credentials that do not match any user in the Mock_Data_Store, THE Identity_Controller SHALL return a 401 response with errorCode `INVALID_CREDENTIALS` and message "Invalid email or password"
3. WHEN a POST request is received at `/api/auth/register` with a valid email (well-formed email format), password (8 to 64 characters containing at least 1 uppercase letter, 1 lowercase letter, and 1 digit), and displayName (3 to 50 characters), THE Identity_Controller SHALL return a 200 response containing a JWT token string and a new User object with role set to "buyer"
4. WHEN a POST request is received at `/api/auth/register` with an email that already exists in the Mock_Data_Store, THE Identity_Controller SHALL return a 409 response with errorCode `CONFLICT` and message "Email already registered"
5. IF a POST request to `/api/auth/register` contains a missing or invalid field (malformed email, password outside 8-64 characters or missing required character types, or displayName outside 3-50 characters), THEN THE Identity_Controller SHALL return a 400 response with errorCode `INVALID_REQUEST` and a fieldErrors object identifying each invalid field
6. WHEN a POST request is received at `/api/auth/reset-password` with an email field, THE Identity_Controller SHALL return a 204 No Content response regardless of whether the email exists in the Mock_Data_Store
7. WHEN a POST request is received at `/api/auth/refresh-token` with an Authorization header containing a Bearer token (including expired tokens), THE Identity_Controller SHALL return a 200 response containing a new JWT token string and the associated User object
8. IF a POST request to `/api/auth/refresh-token` lacks an Authorization header or contains a malformed token that cannot be parsed, THEN THE Identity_Controller SHALL return a 401 response with errorCode `UNAUTHORIZED`
9. WHEN a POST request is received at `/api/auth/social-login` with a provider field set to "google" or "facebook" and a non-empty token string, THE Identity_Controller SHALL return a 200 response containing a JWT token string and a User object
10. IF a POST request to `/api/auth/social-login` contains a provider value other than "google" or "facebook", THEN THE Identity_Controller SHALL return a 400 response with errorCode `INVALID_REQUEST` and a fieldErrors object identifying the provider field

### Requirement 3: JWT Token Generation and Validation

**User Story:** As a front-end developer, I want the mock service to generate and validate JWT tokens with userId, role, and expiration claims, so that I can test authenticated request flows and token refresh logic.

#### Acceptance Criteria

1. THE JWT_Utility SHALL generate JWT tokens containing userId (string), role (UserRole), and exp (expiration timestamp in Unix seconds) claims, with the exp claim set to 3600 seconds (1 hour) after token creation time
2. WHEN a request includes a valid non-expired JWT token in the Authorization header using the format `Bearer <token>`, THE Mock_API_Service SHALL extract the userId and role from the token and make them available to the controller handling the request
3. WHEN a request to an endpoint marked as authentication-required lacks an Authorization header, contains a token not in `Bearer <token>` format, or contains a token with an unverifiable signature or malformed structure, THE Mock_API_Service SHALL return a 401 response with errorCode `UNAUTHORIZED`
4. WHEN a request contains an expired JWT token on any endpoint other than `/api/auth/refresh-token`, THE Mock_API_Service SHALL return a 401 response with errorCode `TOKEN_EXPIRED`
5. WHEN a POST request is received at `/api/auth/refresh-token` with an expired but structurally valid JWT token in the Authorization header, THE Mock_API_Service SHALL accept the token for processing without returning a `TOKEN_EXPIRED` error

### Requirement 4: User Profile Service

**User Story:** As a front-end developer, I want the mock service to support profile retrieval and updates, and watchlist management, so that I can develop the user profile and watchlist features.

#### Acceptance Criteria

1. WHEN a GET request is received at `/api/users/profile` with a valid token, THE User_Profile_Controller SHALL return a 200 response containing the authenticated user's User object (id, email, displayName, role, avatarUrl, createdAt)
2. WHEN a PUT request is received at `/api/users/profile` with optional displayName (3 to 50 characters) and avatarUrl (maximum 2048 characters) fields, THE User_Profile_Controller SHALL update the corresponding user in the Mock_Data_Store and return a 200 response containing the updated User object
3. IF a PUT request is received at `/api/users/profile` with a displayName shorter than 3 characters or longer than 50 characters, THEN THE User_Profile_Controller SHALL return a 400 response with errorCode `INVALID_REQUEST` and a fieldErrors object identifying the invalid field
4. WHEN a GET request is received at `/api/users/watchlist` with page and pageSize query parameters, THE User_Profile_Controller SHALL return a Paginated_Response containing FurnitureListingSummary objects (id, title, thumbnailUrl, currentBid, timeRemaining in milliseconds, condition, category)
5. WHEN a POST request is received at `/api/users/watchlist/{listingId}` where the listingId exists in the Mock_Data_Store, THE User_Profile_Controller SHALL add the listing to the user's watchlist and return a 204 No Content response
6. IF a POST request is received at `/api/users/watchlist/{listingId}` where the listingId does not exist in the Mock_Data_Store, THEN THE User_Profile_Controller SHALL return a 404 response with errorCode `NOT_FOUND`
7. WHEN a DELETE request is received at `/api/users/watchlist/{listingId}`, THE User_Profile_Controller SHALL remove the listing from the user's watchlist in the Mock_Data_Store and return a 204 No Content response
8. IF a DELETE request is received at `/api/users/watchlist/{listingId}` where the listingId is not present in the user's watchlist, THEN THE User_Profile_Controller SHALL return a 404 response with errorCode `NOT_FOUND`

### Requirement 5: Auction Service - Bidding Endpoints

**User Story:** As a front-end developer, I want the mock service to handle bid placement, bid history retrieval, and auto-bid activation/deactivation, so that I can develop the auction bidding UI.

#### Acceptance Criteria

1. WHEN a POST request is received at `/api/auctions/bids` with a valid token and a request body containing auctionId (string) and amount (number with at most 2 decimal places, range 0.01 to 999,999,999.99), THE Auction_Controller SHALL return a 200 response containing success:true and a Bid object (id, auctionId, bidderId, bidderAlias, amount, timestamp in ISO 8601 format)
2. IF a POST request is received at `/api/auctions/bids` with an amount lower than the current highest bid plus a minimum increment of 5.00, THEN THE Auction_Controller SHALL return a 200 response containing success:false and an error string indicating the minimum acceptable bid amount
3. WHEN a GET request is received at `/api/auctions/{auctionId}/bids` with page and pageSize query parameters, THE Auction_Controller SHALL return a Paginated_Response containing Bid objects sorted by timestamp in descending order (most recent first)
4. WHEN a POST request is received at `/api/auctions/{auctionId}/auto-bid` with a valid token and a request body containing maxAmount (number with at most 2 decimal places, range 0.01 to 999,999,999.99, must be greater than or equal to the current highest bid plus the minimum increment of 5.00), THE Auction_Controller SHALL store the auto-bid configuration in the Mock_Data_Store and return a 204 No Content response
5. WHEN a DELETE request is received at `/api/auctions/{auctionId}/auto-bid` with a valid token, THE Auction_Controller SHALL remove the auto-bid configuration from the Mock_Data_Store and return a 204 No Content response
6. IF a POST request to `/api/auctions/bids` or `/api/auctions/{auctionId}/auto-bid` references an auctionId that does not exist in the Mock_Data_Store, THEN THE Auction_Controller SHALL return a 404 response with errorCode `NOT_FOUND`
7. IF a POST request to `/api/auctions/bids`, POST to `/api/auctions/{auctionId}/auto-bid`, or DELETE to `/api/auctions/{auctionId}/auto-bid` is received without a valid token, THEN THE Auction_Controller SHALL return a 401 response with errorCode `UNAUTHORIZED`

### Requirement 6: Auction Service - Seller Endpoints

**User Story:** As a front-end developer, I want the mock service to return seller-specific listing data, so that I can develop the seller dashboard views.

#### Acceptance Criteria

1. WHEN a GET request is received at `/api/seller/active-listings` with a valid seller or admin token and page/pageSize query parameters, THE Auction_Controller SHALL return a Paginated_Response containing SellerActiveListing objects (id, title, currentBid, bidCount, timeRemaining in milliseconds) scoped to listings owned by the authenticated seller
2. WHEN a GET request is received at `/api/seller/completed-auctions` with a valid seller or admin token and page/pageSize query parameters, THE Auction_Controller SHALL return a Paginated_Response containing SellerCompletedAuction objects (id, title, winningBid, winnerDisplayName, reserveMet, endedAt in ISO 8601 format) scoped to listings owned by the authenticated seller
3. IF a GET request to `/api/seller/active-listings` or `/api/seller/completed-auctions` is received with a token whose role is "buyer", THEN THE Auction_Controller SHALL return a 403 response with errorCode `FORBIDDEN`

### Requirement 7: Furniture Service - Catalog Endpoints

**User Story:** As a front-end developer, I want the mock service to support furniture listing retrieval with filtering, sorting, and pagination, so that I can develop the catalog browsing experience.

#### Acceptance Criteria

1. WHEN a GET request is received at `/api/furniture` with optional query parameters (page, pageSize, sort, category, condition, priceMin, priceMax, location), THE Furniture_Controller SHALL return a Paginated_Response containing only FurnitureListingSummary objects with status "active", filtered and sorted according to the provided parameters, returning an empty data array with total set to 0 when no listings match
2. THE Furniture_Controller SHALL support sort values: "ending-soonest" (default), "price-low-high", "price-high-low", and "newest", and SHALL default to "ending-soonest" when the provided sort parameter does not match any supported value
3. THE Furniture_Controller SHALL support filtering by multiple comma-separated category values from the FurnitureCategory enumeration, ignoring any values that do not match a valid FurnitureCategory
4. THE Furniture_Controller SHALL support filtering by multiple comma-separated condition values from the FurnitureCondition enumeration, ignoring any values that do not match a valid FurnitureCondition
5. THE Furniture_Controller SHALL support filtering by priceMin (minimum 0) and priceMax (maximum 999,999) range applied to the currentBid field, and SHALL return an empty data array when priceMin exceeds priceMax
6. WHEN a GET request is received at `/api/furniture` with a location query parameter, THE Furniture_Controller SHALL return only listings whose location field contains the provided value as a case-insensitive substring
7. WHEN a GET request is received at `/api/furniture/{id}` with a valid listing ID, THE Furniture_Controller SHALL return a 200 response containing the full FurnitureListing object (id, title, description, category, condition, brand, material, dimensions, weight, location, images, startingPrice, reservePrice, currentBid, bidCount, auctionEndDate, status, sellerId, sellerDisplayName, sellerRating, createdAt)
8. WHEN a GET request is received at `/api/furniture/{id}` with a non-existent listing ID, THE Furniture_Controller SHALL return a 404 response with errorCode `NOT_FOUND`

### Requirement 8: Furniture Service - Listing Management

**User Story:** As a front-end developer, I want the mock service to handle listing creation, flagging, and deletion, so that I can develop the seller listing management and reporting features.

#### Acceptance Criteria

1. WHEN a POST request is received at `/api/furniture` as multipart/form-data with title (5-100 characters), description (20-2000 characters), category (valid FurnitureCategory), condition (valid FurnitureCondition), dimensions (JSON-stringified Dimensions object), startingPrice (numeric string, 0.01 to 999,999.99), reservePrice (numeric string, greater than or equal to startingPrice), auctionEndDate (ISO 8601 datetime, 24 hours to 30 days from current time), and images (1-10 files) fields from an authenticated user with seller or admin role, THE Furniture_Controller SHALL create a new listing with status "active" in the Mock_Data_Store and return a 200 response containing the full FurnitureListing object
2. WHEN a PUT request is received at `/api/furniture/{id}/flag` with a reason field (1-500 characters) from an authenticated user and the listing exists in the Mock_Data_Store, THE Furniture_Controller SHALL update the listing status to "flagged" in the Mock_Data_Store and return a 204 No Content response
3. WHEN a DELETE request is received at `/api/furniture/{id}` with a valid owner or admin token and the listing exists in the Mock_Data_Store, THE Furniture_Controller SHALL update the listing status to "removed" in the Mock_Data_Store and return a 204 No Content response
4. IF a PUT request to `/api/furniture/{id}/flag` or a DELETE request to `/api/furniture/{id}` references a listing ID that does not exist in the Mock_Data_Store, THEN THE Furniture_Controller SHALL return a 404 response with errorCode `NOT_FOUND`
5. IF a DELETE request is received at `/api/furniture/{id}` with a token whose userId does not match the listing's sellerId and whose role is not admin, THEN THE Furniture_Controller SHALL return a 403 response with errorCode `FORBIDDEN`
6. IF a POST request to `/api/furniture` fails field validation, THEN THE Furniture_Controller SHALL return a 400 response with errorCode `INVALID_REQUEST` and fieldErrors identifying each invalid field

### Requirement 9: Notification Service

**User Story:** As a front-end developer, I want the mock service to return notifications and support marking them as read, so that I can develop the notification center UI.

#### Acceptance Criteria

1. WHEN a GET request is received at `/api/notifications` with a valid token and page and pageSize query parameters, THE Notification_Controller SHALL return a Paginated_Response containing Notification objects (id, type, title, message, auctionId, isRead, createdAt) scoped to the authenticated user and sorted by createdAt in descending order
2. WHEN a PUT request is received at `/api/notifications/{id}/read` with a valid token and the notification ID exists in the Mock_Data_Store, THE Notification_Controller SHALL mark the specified notification as read in the Mock_Data_Store and return a 204 No Content response
3. IF a PUT request is received at `/api/notifications/{id}/read` with a notification ID that does not exist in the Mock_Data_Store, THEN THE Notification_Controller SHALL return a 404 response with errorCode `NOT_FOUND`
4. WHEN a PUT request is received at `/api/notifications/read-all` with a valid token, THE Notification_Controller SHALL mark all notifications for the authenticated user as read in the Mock_Data_Store and return a 204 No Content response

### Requirement 10: Payment Service

**User Story:** As a front-end developer, I want the mock service to simulate payment intent creation, confirmation, and history retrieval, so that I can develop the payment flow UI.

#### Acceptance Criteria

1. WHEN a POST request is received at `/api/payments/create-payment-intent` with a valid token and a request body containing auctionId (string) and amount (number, 0.01 to 999,999,999.99), THE Payment_Controller SHALL return a 200 response containing a PaymentIntent object (id, clientSecret, amount, currency set to "USD", status set to "requires_payment_method")
2. WHEN a POST request is received at `/api/payments/confirm-payment` with a valid token and a request body containing a paymentIntentId that exists in the Mock_Data_Store, THE Payment_Controller SHALL update the payment status to "succeeded" and return a 204 No Content response
3. IF a POST request to `/api/payments/confirm-payment` contains a paymentIntentId that does not exist in the Mock_Data_Store, THEN THE Payment_Controller SHALL return a 404 response with errorCode `NOT_FOUND`
4. WHEN a GET request is received at `/api/payments/history` with a valid token and page and pageSize query parameters, THE Payment_Controller SHALL return a response containing a data array of PaymentRecord objects (id, auctionId, amount, currency, status, createdAt) sorted by createdAt in descending order, and a total count

### Requirement 11: Admin User Management Endpoints

**User Story:** As a front-end developer, I want the mock service to provide admin-only user management endpoints, so that I can develop the admin user management dashboard.

#### Acceptance Criteria

1. WHEN a GET request is received at `/api/admin/users` with a valid admin token and page/pageSize query parameters, THE Admin_Controller SHALL return a Paginated_Response containing AdminUserRow objects (id, displayName, email, role, registeredAt in ISO 8601 format, status as one of the AccountStatus values)
2. WHEN a PUT request is received at `/api/admin/users/{userId}/suspend` with a valid admin token and the userId exists in the Mock_Data_Store, THE Admin_Controller SHALL update the user's status to "suspended" in the Mock_Data_Store and return a 204 No Content response
3. WHEN a PUT request is received at `/api/admin/users/{userId}/activate` with a valid admin token and the userId exists in the Mock_Data_Store, THE Admin_Controller SHALL update the user's status to "active" in the Mock_Data_Store and return a 204 No Content response
4. WHEN a DELETE request is received at `/api/admin/users/{userId}` with a valid admin token and the userId exists in the Mock_Data_Store, THE Admin_Controller SHALL update the user's status to "deleted" in the Mock_Data_Store and return a 204 No Content response
5. WHEN any admin endpoint receives a request with a non-admin role token, THE Admin_Controller SHALL return a 403 response with errorCode `FORBIDDEN`
6. IF a PUT or DELETE request to `/api/admin/users/{userId}/suspend`, `/api/admin/users/{userId}/activate`, or `/api/admin/users/{userId}` references a userId that does not exist in the Mock_Data_Store, THEN THE Admin_Controller SHALL return a 404 response with errorCode `NOT_FOUND`

### Requirement 12: Admin Listing Management Endpoints

**User Story:** As a front-end developer, I want the mock service to provide admin-only listing management endpoints, so that I can develop the admin listing management and reporting dashboard.

#### Acceptance Criteria

1. WHEN a GET request is received at `/api/admin/listings` with a valid admin token and page/pageSize query parameters, THE Admin_Controller SHALL return a Paginated_Response containing AdminListingRow objects (id, title, sellerDisplayName, status, currentBid, reportCount)
2. WHEN a DELETE request is received at `/api/admin/listings/{listingId}` with a valid admin token and the listingId exists in the Mock_Data_Store, THE Admin_Controller SHALL update the listing status to "removed" in the Mock_Data_Store and return a 204 No Content response
3. WHEN a PUT request is received at `/api/admin/listings/{listingId}/flag` with a valid admin token and the listingId exists in the Mock_Data_Store, THE Admin_Controller SHALL update the listing status to "flagged" in the Mock_Data_Store and return a 204 No Content response
4. WHEN a GET request is received at `/api/admin/listings/reports` with a valid admin token and page/pageSize query parameters, THE Admin_Controller SHALL return a Paginated_Response containing ListingReport objects (id, listingId, reason, reporterDisplayName, reportDate) sorted by reportDate in descending order
5. IF a DELETE or PUT request to `/api/admin/listings/{listingId}` or `/api/admin/listings/{listingId}/flag` references a listingId that does not exist in the Mock_Data_Store, THEN THE Admin_Controller SHALL return a 404 response with errorCode `NOT_FOUND`

### Requirement 13: Admin Analytics Endpoints

**User Story:** As a front-end developer, I want the mock service to return analytics data, so that I can develop the admin analytics dashboard with charts and summaries.

#### Acceptance Criteria

1. WHEN a GET request is received at `/api/admin/analytics/summary` with startDate and endDate query parameters in ISO 8601 date format, THE Admin_Controller SHALL return a 200 response containing an AnalyticsSummary object where totalUsers is a non-negative integer, activeAuctions is a non-negative integer, completedAuctions is a non-negative integer, and totalRevenue is a non-negative number with up to 2 decimal places
2. WHEN a GET request is received at `/api/admin/analytics/auction-trends` with startDate and endDate query parameters in ISO 8601 date format, THE Admin_Controller SHALL return a 200 response containing an array of AuctionTrend objects (date, auctionsCreated, auctionsCompleted) with exactly one entry per calendar day in the inclusive date range, where each date is in ISO 8601 date format and auctionsCreated and auctionsCompleted are non-negative integers
3. WHEN a GET request is received at `/api/admin/analytics/category-distribution` with startDate and endDate query parameters in ISO 8601 date format, THE Admin_Controller SHALL return a 200 response containing an array of exactly 8 CategoryDistribution objects (category, count) with one entry per FurnitureCategory value, where count is a non-negative integer
4. WHEN a GET request is received at `/api/admin/analytics/top-sellers` with startDate and endDate query parameters in ISO 8601 date format, THE Admin_Controller SHALL return a 200 response containing an array of at most 10 TopSeller objects (displayName, completedAuctions, totalRevenue) sorted by totalRevenue in descending order, where completedAuctions is a positive integer and totalRevenue is a positive number with up to 2 decimal places
5. IF a GET request to any `/api/admin/analytics/*` endpoint is missing the startDate or endDate query parameter, or provides a value that is not a valid ISO 8601 date, or startDate is after endDate, THEN THE Admin_Controller SHALL return a 400 response with errorCode `INVALID_REQUEST` and a message indicating the invalid parameter

### Requirement 14: WebSocket Real-Time Events

**User Story:** As a front-end developer, I want the mock service to support WebSocket connections with event subscription and broadcasting, so that I can develop real-time bidding updates and notification features.

#### Acceptance Criteria

1. THE WebSocket_Handler SHALL accept Socket.IO connections at the configured WebSocket URL, authenticated via a JWT token provided in the `auth.token` field of the connection handshake
2. IF a client attempts a WebSocket connection with a missing, invalid, or expired JWT token, THEN THE WebSocket_Handler SHALL reject the connection and emit a connection error indicating authentication failure
3. WHEN a client sends a `join:auction` event with an auctionId payload, THE WebSocket_Handler SHALL register the client to receive bid update events for that auction
4. WHEN a client sends a `leave:auction` event with an auctionId payload, THE WebSocket_Handler SHALL unregister the client from receiving bid update events for that auction
5. WHEN a client sends a `subscribe:notifications` event with a userId payload, THE WebSocket_Handler SHALL register the client to receive notification events for that user
6. WHEN a bid is placed via the POST `/api/auctions/bids` endpoint that succeeds, THE WebSocket_Handler SHALL emit a `bid:update` event containing auctionId, currentBid, bidCount, bidderAlias, and timestamp (ISO 8601) fields to all clients subscribed to that auction
7. WHEN a bid is placed that outbids another user, THE WebSocket_Handler SHALL emit an `outbid` event containing auctionId and currentBid fields to the outbid user's subscribed connection
8. THE WebSocket_Handler SHALL support emitting `auction:ending` events containing auctionId and minutesRemaining (set to 15) fields to clients subscribed to the relevant auction
9. THE WebSocket_Handler SHALL support emitting `auction:won` events containing auctionId, result ("won"), winningBid, and winnerId fields to the winning user's subscribed connection
10. THE WebSocket_Handler SHALL support emitting `auction:lost` events containing auctionId, result ("lost" or "reserve-not-met"), winningBid, and winnerId fields to losing bidders' subscribed connections
11. THE WebSocket_Handler SHALL support emitting `notification` events containing the full Notification object (id, type, title, message, auctionId, isRead, createdAt) to the user subscribed via `subscribe:notifications`
12. WHEN a WebSocket client disconnects, THE WebSocket_Handler SHALL remove all auction and notification subscriptions associated with that client

### Requirement 15: Error Response Format

**User Story:** As a front-end developer, I want the mock service to return standardized error responses matching the front-end's expected error format, so that I can develop error handling and display logic.

#### Acceptance Criteria

1. WHEN any endpoint encounters an error condition, THE Mock_API_Service SHALL return a JSON response body with Content-Type `application/json` containing statusCode (number matching the HTTP response status code), errorCode (string), message (string, 1 to 256 characters), and optional fieldErrors (object mapping field names to error message strings)
2. IF a request fails input validation, THEN THE Mock_API_Service SHALL return HTTP 400 with errorCode `INVALID_REQUEST` and a fieldErrors object containing one entry per invalid field, where each key is the field name and each value is a string describing the validation constraint that failed
3. IF a request to a protected endpoint lacks an Authorization header or contains a malformed token, THEN THE Mock_API_Service SHALL return HTTP 401 with errorCode `UNAUTHORIZED`
4. IF a request contains an expired JWT token on any endpoint other than `/api/auth/refresh-token`, THEN THE Mock_API_Service SHALL return HTTP 401 with errorCode `TOKEN_EXPIRED`
5. IF an authenticated user's role does not have permission for the requested endpoint, THEN THE Mock_API_Service SHALL return HTTP 403 with errorCode `FORBIDDEN`
6. IF a request references a resource identifier that does not exist in the Mock_Data_Store, THEN THE Mock_API_Service SHALL return HTTP 404 with errorCode `NOT_FOUND`
7. IF a request would create a resource that conflicts with an existing resource in the Mock_Data_Store (such as registering a duplicate email), THEN THE Mock_API_Service SHALL return HTTP 409 with errorCode `CONFLICT`
8. IF a bid placement request specifies an amount below the current highest bid plus the minimum increment, THEN THE Mock_API_Service SHALL return HTTP 422 with errorCode `BID_TOO_LOW`
9. IF a bid placement or auto-bid request targets an auction with status "ended", THEN THE Mock_API_Service SHALL return HTTP 422 with errorCode `AUCTION_ENDED`
10. IF an unhandled error occurs during request processing, THEN THE Mock_API_Service SHALL return HTTP 500 with errorCode `INTERNAL_ERROR`

### Requirement 16: Mock Data Seeding

**User Story:** As a front-end developer, I want the mock service to be pre-loaded with realistic sample data on startup, so that the front-end has meaningful data to display without manual setup.

#### Acceptance Criteria

1. WHEN the Mock_API_Service starts, THE Mock_Data_Store SHALL be populated with at least 3 mock users: one with role "buyer", one with role "seller", and one with role "admin", each with AccountStatus "active"
2. WHEN the Mock_API_Service starts, THE Mock_Data_Store SHALL be populated with at least 10 furniture listings spanning at least 4 distinct FurnitureCategory values and at least 3 distinct FurnitureCondition values, where at least 3 listings have auctionEndDate in the future (status "active"), at least 2 listings have auctionEndDate in the past (status "ended"), and at least 1 listing has status "flagged"
3. WHEN the Mock_API_Service starts, THE Mock_Data_Store SHALL assign at least 3 of the seeded furniture listings to the mock seller user as the sellerId owner, so that the seller dashboard endpoints return data
4. WHEN the Mock_API_Service starts, THE Mock_Data_Store SHALL be populated with at least 5 mock bids distributed across at least 3 different listings, with at least 2 bids placed by the mock buyer user
5. WHEN the Mock_API_Service starts, THE Mock_Data_Store SHALL be populated with at least 5 mock notifications for the mock buyer user covering at least 4 distinct NotificationType values, with at least 2 notifications having isRead set to false
6. WHEN the Mock_API_Service starts, THE Mock_Data_Store SHALL be populated with at least 2 mock payment records for the mock buyer user, with at least one having status "succeeded" and at least one having status "requires_payment_method"
7. THE Mock_Data_Store SHALL include predefined login credentials (email and password) for each mock user that are documented in the project README

### Requirement 17: Pagination Support

**User Story:** As a front-end developer, I want all list endpoints to properly handle pagination parameters, so that I can develop infinite scrolling and pagination controls.

#### Acceptance Criteria

1. WHEN a paginated endpoint receives page and pageSize query parameters, THE Mock_API_Service SHALL return only the subset of items corresponding to the requested page (offset = (page - 1) * pageSize), where page is an integer greater than or equal to 1 and pageSize is an integer between 1 and 100 inclusive
2. WHEN a paginated endpoint receives no page parameter, THE Mock_API_Service SHALL default page to 1
3. WHEN a paginated endpoint receives no pageSize parameter, THE Mock_API_Service SHALL default pageSize to 20
4. IF the requested page is beyond the available data (offset exceeds total item count), THEN THE Mock_API_Service SHALL return a Paginated_Response with an empty data array, hasMore set to false, and total reflecting the actual total count of matching items
5. IF (page - 1) * pageSize + pageSize is less than the total count of matching items, THEN THE Mock_API_Service SHALL set the hasMore field to true in Paginated_Response; otherwise THE Mock_API_Service SHALL set hasMore to false
6. THE Mock_API_Service SHALL set the total field in Paginated_Response to the total count of items matching any applied filters
7. IF a paginated endpoint receives a page value less than 1 or a pageSize value less than 1 or greater than 100, THEN THE Mock_API_Service SHALL return a 400 response with errorCode `INVALID_REQUEST`
