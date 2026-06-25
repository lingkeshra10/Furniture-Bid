# Requirements Document

## Introduction

The Furniture Bid System is a Vue.js front-end application that enables users to browse furniture listings, place bids in real-time auctions, and manage their auction activity. The platform serves three user roles: Buyers who browse and bid, Sellers who list furniture for auction, and Admins who oversee platform operations. The front-end integrates with backend microservices (Identity, User Profile, Auction, Furniture, Notification, and Payment services) via REST APIs and WebSocket connections.

## Glossary

- **Application**: The Furniture Bid System Vue.js front-end application
- **Auth_Module**: The authentication module handling login, registration, and session management
- **Listing_Page**: The page displaying a single furniture item with its auction details
- **Catalog_View**: The browsable collection of furniture listings with filtering and sorting
- **Bid_Panel**: The UI component where users enter and submit bids on a furniture item
- **Auto_Bid_Engine**: The client-side logic that manages automatic bidding up to a user-defined maximum
- **Watchlist_View**: The page displaying furniture items a buyer has saved for monitoring
- **Notification_Center**: The UI component displaying real-time and historical notifications
- **Seller_Dashboard**: The page where sellers manage their listings and track bid activity
- **Admin_Dashboard**: The page where admins manage users, listings, reports, and view analytics
- **Router**: The Vue Router instance managing navigation and route guards
- **State_Store**: The Pinia store instances managing application state
- **Socket_Client**: The Socket.IO client instance managing real-time WebSocket connections
- **Form_Validator**: The VeeValidate-based form validation logic
- **Buyer**: A registered user who browses furniture and places bids
- **Seller**: A registered user who lists furniture items for auction
- **Admin**: A privileged user who manages platform operations
- **Reserve_Price**: The minimum price a seller will accept; auction must meet or exceed this to result in a sale
- **Starting_Price**: The initial bid amount set by the seller for a furniture listing
- **Bid_Increment**: The minimum amount by which a new bid must exceed the current highest bid

## Requirements

### Requirement 1: User Authentication

**User Story:** As a user, I want to register and log in using email or social providers, so that I can access the platform securely.

#### Acceptance Criteria

1. THE Auth_Module SHALL provide a registration form collecting email, password, and display name with real-time field validation enforcing: email must be a valid email format, password must be between 8 and 64 characters containing at least one uppercase letter, one lowercase letter, and one digit, and display name must be between 3 and 50 characters
2. THE Auth_Module SHALL provide a login form accepting email and password credentials
3. THE Auth_Module SHALL provide login buttons for Google OAuth and Facebook OAuth social authentication
4. WHEN a user submits valid registration credentials, THE Auth_Module SHALL send the registration request to the Identity Service, display a loading indicator during the request, and redirect to the login page with a success message on success
5. WHEN a user submits valid login credentials, THE Auth_Module SHALL store the JWT token in the State_Store and redirect to the Catalog_View
6. WHEN a user submits a valid email address on the password reset form, THE Auth_Module SHALL send the reset request to the Identity Service and display a confirmation message indicating that a reset link has been sent if the email is associated with an account
7. IF the Identity Service returns an authentication error, THEN THE Auth_Module SHALL display the error message inline without clearing the form fields
8. WHEN a user clicks "Logout", THE Auth_Module SHALL clear the JWT token from the State_Store, disconnect the Socket_Client, and redirect to the login page
9. WHILE a user is not authenticated, THE Router SHALL redirect protected routes to the login page
10. WHEN a user completes social authentication via Google OAuth or Facebook OAuth, THE Auth_Module SHALL store the returned JWT token in the State_Store and redirect to the Catalog_View
11. IF the social authentication provider returns an error or the user cancels the OAuth flow, THEN THE Auth_Module SHALL redirect to the login page and display an error message indicating the social login was unsuccessful

### Requirement 2: Furniture Catalog Browsing

**User Story:** As a buyer, I want to browse furniture listings with filters and sorting, so that I can find items I am interested in bidding on.

#### Acceptance Criteria

1. THE Catalog_View SHALL display furniture listings in a responsive grid layout showing thumbnail image, title, current bid price, time remaining, and condition
2. THE Catalog_View SHALL provide filter controls for category (Sofa, Dining Table, Office Chair, Wardrobe, Bed Frame, Coffee Table, Cabinet, Bookshelf), condition (New, Like New, Good, Fair, Poor), price range with a minimum of 0 and a maximum of 999,999, and location as a text-based search field
3. THE Catalog_View SHALL provide sorting options for price (low to high, high to low), ending soonest, and newest listed, with "ending soonest" as the default sort order
4. WHEN a user applies filters or sorting, THE Catalog_View SHALL update the displayed listings without a full page reload within 3 seconds of user action
5. THE Catalog_View SHALL implement infinite scroll or pagination to load listings in batches of 20 items from the Furniture Service
6. WHEN a user clicks a listing card, THE Router SHALL navigate to the Listing_Page for that furniture item
7. IF the Furniture Service returns an error during listing fetch, THEN THE Catalog_View SHALL display a retry button with an error message indicating the listings could not be loaded
8. IF no listings match the applied filters, THEN THE Catalog_View SHALL display an empty state message indicating no results were found and suggest adjusting the filter criteria

### Requirement 3: Furniture Listing Detail

**User Story:** As a buyer, I want to view complete details of a furniture item, so that I can make an informed decision before placing a bid.

#### Acceptance Criteria

1. THE Listing_Page SHALL display the furniture title, description, category, condition, brand, material, dimensions (width, height, length), weight, and location
2. THE Listing_Page SHALL display an image gallery supporting up to 10 images with previous/next navigation controls and an image position indicator
3. THE Listing_Page SHALL display the current highest bid (or the starting price if no bids have been placed), total number of bids, starting price, and a countdown timer showing days, hours, minutes, and seconds remaining until auction end
4. THE Listing_Page SHALL display the seller display name and rating as a numeric score
5. WHEN the auction countdown reaches zero, THE Listing_Page SHALL display the auction result indicating whether the item was won by the current user or the auction ended without their winning, and disable the Bid_Panel
6. WHILE the auction is active, THE Listing_Page SHALL update the current highest bid and bid count within 3 seconds of a new bid event received via the Socket_Client
7. IF the auction has already ended when the user navigates to the Listing_Page, THEN THE Listing_Page SHALL display the final auction result and the Bid_Panel shall be disabled
8. IF the Furniture Service returns an error when loading listing details, THEN THE Listing_Page SHALL display an error message indicating the listing could not be loaded and provide a retry button

### Requirement 4: Bidding System

**User Story:** As a buyer, I want to place bids on furniture items, so that I can compete to win auctions.

#### Acceptance Criteria

1. WHILE a user is authenticated and the auction is active, THE Bid_Panel SHALL display a bid input field pre-filled with the minimum next bid amount (current highest bid plus Bid_Increment), accepting only numeric values with up to two decimal places within the range of 0.01 to 999,999,999.99
2. WHEN a user submits a bid amount equal to or greater than the minimum next bid, THE Bid_Panel SHALL send the bid to the Auction Service and display an inline success confirmation message within the Bid_Panel
3. IF a user submits a bid amount less than the minimum next bid, THEN THE Bid_Panel SHALL display a validation error indicating the minimum acceptable bid amount
4. IF the Auction Service rejects a bid, THEN THE Bid_Panel SHALL display the rejection reason returned by the service and retain the user entered bid amount in the input field
5. THE Bid_Panel SHALL display the bid history for the current listing in reverse chronological order, showing bid amounts, anonymized bidder identifiers, and timestamps, limited to the 20 most recent bids with the ability to load older entries
6. WHEN a new bid is placed by another user, THE Socket_Client SHALL update the Bid_Panel with the new highest bid, updated bid count, and updated minimum next bid amount within 2 seconds of the event occurring on the server
7. IF a bid submission fails due to a network error, THEN THE Bid_Panel SHALL display an error message indicating the bid was not submitted and retain the user entered bid amount in the input field
8. IF the auction ends while the user is viewing the Bid_Panel, THEN THE Bid_Panel SHALL disable the bid input field and submit button and display a message indicating the auction has ended

### Requirement 5: Auto-Bid Feature

**User Story:** As a buyer, I want to set a maximum bid amount so that the system automatically bids on my behalf up to that limit.

#### Acceptance Criteria

1. THE Bid_Panel SHALL provide an auto-bid toggle with a maximum bid amount input field that accepts values from the current highest bid plus one Bid_Increment up to 999,999,999.99
2. WHEN a user activates auto-bid with a maximum amount that is greater than or equal to the current highest bid plus one Bid_Increment, THE Application SHALL send the auto-bid configuration to the Auction Service and display a confirmation indicating auto-bid is active
3. WHEN the Socket_Client receives an outbid notification and auto-bid is active for that auction, THE Application SHALL display a toast notification indicating the auto-bid system has placed a bid on the user behalf
4. IF the current highest bid plus one Bid_Increment exceeds the user maximum auto-bid amount, THEN THE Application SHALL display a toast notification and add an entry to the Notification_Center indicating that their auto-bid limit has been reached and auto-bid has been deactivated
5. WHILE auto-bid is enabled for an auction, THE Bid_Panel SHALL display the auto-bid status as active and the configured maximum amount
6. WHEN a user deactivates the auto-bid toggle, THE Application SHALL send the deactivation request to the Auction Service and update the Bid_Panel to display the auto-bid status as inactive
7. IF the Auction Service returns an error when processing an auto-bid configuration or deactivation request, THEN THE Application SHALL display a toast notification indicating the error reason and retain the previous auto-bid state

### Requirement 6: Watchlist Management

**User Story:** As a buyer, I want to add furniture items to a watchlist, so that I can track auctions I am interested in.

#### Acceptance Criteria

1. THE Listing_Page SHALL display a "Add to Watchlist" button that toggles to "Remove from Watchlist" based on the current watchlist state
2. WHEN a user clicks "Add to Watchlist", THE State_Store SHALL add the listing to the user watchlist and persist it via the User Profile Service, and WHEN a user clicks "Remove from Watchlist", THE State_Store SHALL remove the listing from the user watchlist and persist the change via the User Profile Service
3. THE Watchlist_View SHALL display all watched items sorted by auction ending soonest first, showing thumbnail, title, current bid, and time remaining, and SHALL display an empty state message when no items are in the watchlist
4. THE Watchlist_View SHALL indicate items where the user has been outbid by displaying a distinguishable "Outbid" badge on the affected listing card
5. WHEN an auction on a watched item ends, THE Watchlist_View SHALL update the item status to display one of: "Won" if the user placed the highest bid, "Lost" if the user was outbid, or "Reserve Not Met" if the reserve price was not reached
6. IF the User Profile Service returns an error when adding or removing a watchlist item, THEN THE Application SHALL display an error message indicating the operation failed and revert the watchlist state to its previous value

### Requirement 7: Notification System

**User Story:** As a user, I want to receive real-time notifications about auction activity, so that I can respond to bidding events promptly.

#### Acceptance Criteria

1. THE Notification_Center SHALL display a bell icon in the application header showing the count of unread notifications, displaying "99+" when the count exceeds 99
2. WHEN a user clicks the bell icon, THE Notification_Center SHALL display a dropdown list of the 20 most recent notifications sorted by most recent first, with the ability to load older notifications
3. THE Socket_Client SHALL receive and display real-time notifications for: outbid events, auction ending within 15 minutes, auction won, and auction lost
4. WHEN a notification is received, THE Notification_Center SHALL increment the unread count and display a toast message that auto-dismisses after 5 seconds
5. WHEN a user clicks a notification, THE Router SHALL navigate to the relevant Listing_Page and mark that notification as read
6. THE Notification_Center SHALL provide a "Mark all as read" action that resets the unread count to zero and marks all notifications as read

### Requirement 8: Seller Listing Creation

**User Story:** As a seller, I want to create furniture listings with detailed information, so that buyers can find and bid on my items.

#### Acceptance Criteria

1. THE Seller_Dashboard SHALL provide a "Create Listing" form with required fields: title, description, category (dropdown), condition (dropdown), dimensions (width, height, length in centimeters), starting price, reserve price, auction end date, and at least one image; and optional fields: brand, material, weight (in kilograms), and location
2. THE Form_Validator SHALL validate that title is between 5 and 100 characters, description is between 20 and 2000 characters, starting price is between 0.01 and 999,999.99, reserve price is greater than or equal to starting price and no greater than 999,999.99, dimensions are between 1 and 9999 centimeters per axis, weight (if provided) is between 0.1 and 9999 kilograms, and auction end date is between 24 hours and 30 days in the future
3. THE Seller_Dashboard SHALL support uploading between 1 and 10 images per listing in JPEG, PNG, or WebP format with a maximum file size of 5 MB per image, providing drag-and-drop functionality and image preview
4. WHEN a seller submits a valid listing form, THE Application SHALL disable the submit button to prevent duplicate submissions, send the listing data to the Furniture Service, and on success display a confirmation with a link to the new listing
5. IF the Furniture Service returns a validation error, THEN THE Seller_Dashboard SHALL display the specific field errors inline on the form and re-enable the submit button
6. IF the Furniture Service is unreachable or returns a non-validation error, THEN THE Seller_Dashboard SHALL display an error message indicating submission failure, preserve all entered form data, and re-enable the submit button

### Requirement 9: Seller Bid Tracking

**User Story:** As a seller, I want to track bids on my listings, so that I can monitor auction progress.

#### Acceptance Criteria

1. THE Seller_Dashboard SHALL display a paginated list of the seller active listings sorted by ending soonest, showing title, current highest bid, number of bids, and time remaining in days, hours, and minutes format
2. THE Seller_Dashboard SHALL display a paginated list of completed auctions sorted by most recently ended, showing title, winning bid, winner display name, and whether reserve price was met
3. WHEN a new bid is placed on a seller listing, THE Socket_Client SHALL update the current highest bid and number of bids for that listing on the Seller_Dashboard without requiring a page refresh
4. WHEN a user clicks on a listing in the Seller_Dashboard, THE Router SHALL navigate to the Listing_Page for that item
5. IF the Auction Service or Furniture Service returns an error when loading seller listings, THEN THE Seller_Dashboard SHALL display an error message with a retry button while preserving any previously loaded data
6. IF a seller has no active listings or no completed auctions, THEN THE Seller_Dashboard SHALL display an empty state message indicating no items are available in that section

### Requirement 10: Admin User Management

**User Story:** As an admin, I want to manage platform users, so that I can maintain a safe marketplace.

#### Acceptance Criteria

1. THE Admin_Dashboard SHALL display a paginated table (20 users per page) of all registered users showing display name, email, role, registration date, and account status, with a search field that filters results by display name or email
2. THE Admin_Dashboard SHALL provide actions to suspend, activate, or delete user accounts
3. WHEN an admin selects the delete action on a user account, THE Admin_Dashboard SHALL display a confirmation dialog requiring the admin to confirm before sending the request to the Identity Service
4. WHEN an admin performs a suspend or activate action, THE Application SHALL send the request to the Identity Service and update the account status column of the affected table row to reflect the new status on success
5. IF the Identity Service returns an error during a user management action, THEN THE Admin_Dashboard SHALL display the error message in a toast notification that auto-dismisses after 5 seconds

### Requirement 11: Admin Listing Management

**User Story:** As an admin, I want to manage furniture listings, so that I can remove inappropriate content and resolve disputes.

#### Acceptance Criteria

1. THE Admin_Dashboard SHALL display a searchable and paginated table of all listings showing title, seller display name, status, current bid, and report count, with 20 listings per page and search matching against title and seller display name
2. THE Admin_Dashboard SHALL provide actions to remove a listing or flag it for review, and SHALL display a visual status indicator distinguishing between active, flagged, and removed listings
3. WHEN an admin removes a listing, THE Application SHALL display a confirmation dialog before sending the removal request to the Furniture Service, and SHALL remove the listing from the table upon success
4. IF the Furniture Service returns an error during a listing removal or flag action, THEN THE Admin_Dashboard SHALL display the error message in a toast notification and leave the listing unchanged in the table
5. THE Admin_Dashboard SHALL display a list of reported listings with the report reason, reporter display name, and report date, sorted by most recent report first
6. WHEN an admin flags a listing for review, THE Application SHALL send the flag request to the Furniture Service and update the listing status indicator to flagged in the table

### Requirement 12: Admin Analytics Dashboard

**User Story:** As an admin, I want to view platform analytics, so that I can understand usage patterns and platform health.

#### Acceptance Criteria

1. THE Admin_Dashboard SHALL display summary cards showing total users, active auctions, completed auctions, and total revenue
2. THE Admin_Dashboard SHALL display a line chart using Chart.js showing the number of auctions created and the number of auctions completed per day over the selected date range
3. THE Admin_Dashboard SHALL display a bar chart showing listings count per category using Chart.js
4. THE Admin_Dashboard SHALL display a table of the top 10 sellers ranked by completed auctions and total revenue, showing seller display name, number of completed auctions, and total revenue
5. THE Admin_Dashboard SHALL provide a date range picker that defaults to the last 30 days and allows selecting a custom range up to 12 months, and SHALL update all charts and summary cards when the date range is changed
6. IF the analytics data fails to load from the backend service, THEN THE Admin_Dashboard SHALL display an error message with a retry button while preserving the currently selected date range

### Requirement 13: Real-Time WebSocket Connection

**User Story:** As a user, I want real-time updates on auction activity, so that I have the most current information without refreshing the page.

#### Acceptance Criteria

1. WHEN a user logs in, THE Socket_Client SHALL establish a WebSocket connection to the Notification Service using the JWT token for authentication within 5 seconds of login completion
2. WHILE a user is viewing a Listing_Page, THE Socket_Client SHALL subscribe to the bid channel for that specific auction
3. WHEN the Socket_Client receives a bid update event, THE Application SHALL update the current highest bid amount, bid count, and countdown timer in all visible UI components (Bid_Panel, Listing_Page, Catalog_View) within 1 second of receiving the event
4. IF the WebSocket connection is lost, THEN THE Socket_Client SHALL attempt to reconnect starting with a 1-second delay, doubling the delay on each attempt up to a maximum of 30 seconds, for a maximum of 10 attempts, and display a visible connection status indicator showing either "connected", "reconnecting", or "disconnected" state
5. WHEN a user navigates away from a Listing_Page, THE Socket_Client SHALL unsubscribe from that auction bid channel
6. IF the WebSocket connection is re-established after a disconnection, THEN THE Socket_Client SHALL re-subscribe to all channels the user was subscribed to prior to disconnection
7. IF the WebSocket connection fails due to an expired or invalid JWT token, THEN THE Socket_Client SHALL trigger a token refresh via the Auth_Module and reattempt connection once with the new token

### Requirement 14: API Service Layer and Mock Integration

**User Story:** As a developer, I want a centralized API service layer with proper placeholders, so that the front-end can be developed independently of the backend and easily connected later.

#### Acceptance Criteria

1. THE Application SHALL implement an API service layer using Axios with a centralized base configuration including base URL (configurable via environment variable VITE_API_BASE_URL), default headers (Content-Type: application/json, Authorization: Bearer {token}), request timeout of 30 seconds, and response interceptors for error handling
2. THE Application SHALL organize API service modules by backend microservice domain: authService (Identity Service calls), userProfileService (User Profile Service calls), auctionService (Auction Service calls), furnitureService (Furniture Service calls), notificationService (Notification Service calls), and paymentService (Payment Service calls)
3. EACH API service module SHALL export typed functions with clearly named parameters matching the expected backend contract, using TypeScript interfaces for request payloads and response shapes
4. THE Application SHALL implement a mock/stub mode activated via environment variable VITE_USE_MOCKS=true that returns realistic sample data for all API endpoints, enabling front-end development without a running backend
5. THE Application SHALL implement request interceptors that automatically attach the JWT token from the State_Store to all authenticated API requests
6. THE Application SHALL implement response interceptors that handle HTTP 401 by triggering token refresh or logout, HTTP 403 by redirecting to the unauthorized page, HTTP 429 by displaying a rate limit message, and HTTP 5xx by displaying a generic server error notification
7. IF an API request fails due to network unavailability, THEN THE Application SHALL display a connection error toast notification and provide a retry mechanism for the failed request

### Requirement 15: WebSocket Service Layer

**User Story:** As a developer, I want a centralized WebSocket service with proper event handlers and mock support, so that real-time features can be developed and tested independently.

#### Acceptance Criteria

1. THE Socket_Client SHALL be implemented as a singleton service module that initializes a Socket.IO client connection with configurable server URL (via environment variable VITE_WS_URL), authentication via JWT token passed as a handshake query parameter, and automatic reconnection enabled
2. THE Socket_Client SHALL expose typed event subscription methods for: onBidUpdate(auctionId, callback), onOutbid(callback), onAuctionEnding(callback), onAuctionWon(callback), onAuctionLost(callback), and onNotification(callback)
3. THE Socket_Client SHALL expose typed event emission methods for: joinAuctionRoom(auctionId), leaveAuctionRoom(auctionId), and subscribeNotifications(userId)
4. THE Socket_Client SHALL implement a mock mode activated via environment variable VITE_USE_MOCKS=true that simulates WebSocket events at configurable intervals for testing real-time UI updates without a running backend
5. THE Socket_Client SHALL provide a connection state observable (connected, reconnecting, disconnected) that UI components can subscribe to for displaying connection status
6. THE Socket_Client SHALL clean up all event listeners and subscriptions when the user logs out or the application is destroyed to prevent memory leaks

### Requirement 16: API Documentation Deliverable

**User Story:** As a developer who will build the backend, I want a complete API and WebSocket event reference document, so that I can implement the backend services with the correct endpoints, parameters, and response formats.

#### Acceptance Criteria

1. THE Application repository SHALL include a document at docs/api-reference.md listing every REST API endpoint the front-end calls, organized by microservice (Identity, User Profile, Auction, Furniture, Notification, Payment)
2. EACH API endpoint entry SHALL document: HTTP method, URL path, path parameters, query parameters, request body schema (with TypeScript interface), response body schema (with TypeScript interface), authentication requirement (public or authenticated), and example request/response pairs
3. THE document SHALL include a WebSocket Events section listing every Socket.IO event the front-end emits and listens to, with event name, payload schema, direction (client-to-server or server-to-client), and description
4. THE document SHALL include an Error Response Format section defining the standard error response shape (status code, error code, message, field-level errors for validation) used across all services
5. THE document SHALL include an Authentication section describing the JWT token format expectations (claims: userId, role, exp), token refresh flow, and OAuth callback URL patterns
6. WHEN any API call is added or modified during front-end development, THE developer SHALL update the docs/api-reference.md document to reflect the change

### Requirement 17: Responsive Layout and Theming

**User Story:** As a user, I want the application to work well on all devices with a cohesive visual design, so that I can use the platform from desktop or mobile.

#### Acceptance Criteria

1. THE Application SHALL use TailwindCSS configured with the design system colors: primary (#8B5E3C), secondary (#C19A6B), accent (#D97706), background (#FAF7F2), card (#FFFFFF), success (#16A34A), and text (#1F2937)
2. THE Application SHALL render a responsive layout with breakpoints at mobile (below 768px) displaying a single-column layout, tablet (768px to 1024px) displaying a two-column grid, and desktop (above 1024px) displaying a three or four-column grid for listing cards
3. THE Application SHALL display a responsive navigation bar with a hamburger menu on mobile viewports, a condensed horizontal menu on tablet viewports, and a full horizontal menu with all navigation items visible on desktop viewports
4. THE Application SHALL render all listing cards, forms, and data tables in a mobile-friendly layout without horizontal scrolling, with data tables converting to a stacked card layout on viewports below 768px
5. THE Application SHALL ensure all interactive elements (buttons, links, form controls) have a minimum touch target size of 44x44 pixels on mobile viewports

### Requirement 18: Application State Management

**User Story:** As a developer, I want centralized state management, so that the application data flows are predictable and maintainable.

#### Acceptance Criteria

1. THE State_Store SHALL use Pinia stores organized by domain: auth (JWT token, user role, login state), auction (active bids, bid history), furniture (listings cache, filters), notifications (unread count, notification list), and watchlist (watched item IDs)
2. THE State_Store SHALL persist the auth token across browser sessions using localStorage
3. WHEN the Application receives an HTTP 401 Unauthorized response from any backend service, THE Auth_Module SHALL treat the JWT token as expired, clear all domain stores in the State_Store to their initial default values, and redirect the user to the login page
4. THE State_Store SHALL provide getter functions for derived state: whether the user is authenticated, the current user role, unread notification count, active watchlist count, and whether an auction has active bids
5. WHEN the Application is loaded and a JWT token exists in localStorage, THE Auth_Module SHALL restore the token into the auth store and validate it by requesting the user profile from the Identity Service before allowing navigation to protected routes
6. IF localStorage is unavailable or a read/write operation to localStorage fails, THEN THE State_Store SHALL fall back to in-memory-only storage for the auth token and display a warning notification indicating that the session will not persist across browser sessions

### Requirement 19: Route Navigation and Guards

**User Story:** As a developer, I want route-based access control, so that users can only access pages appropriate to their role.

#### Acceptance Criteria

1. THE Router SHALL define public routes accessible without authentication (login, register, forgot password, catalog, and listing detail) and protected routes requiring authentication (watchlist, bidding history, seller dashboard, create listing, admin dashboard, and user profile)
2. WHILE a user has the Buyer role, THE Router SHALL allow access to catalog, listing detail, watchlist, bidding history, and user profile routes
3. WHILE a user has the Seller role, THE Router SHALL allow access to seller dashboard and create listing routes in addition to all buyer-accessible routes
4. WHILE a user has the Admin role, THE Router SHALL allow access to the admin dashboard route in addition to all other protected and public routes
5. IF an authenticated user navigates to a route they are not authorized to access based on their role, THEN THE Router SHALL redirect to the catalog page and display an unauthorized access notification
6. IF an unauthenticated user navigates to a protected route, THEN THE Router SHALL redirect to the login page
7. IF a user navigates to a URL that does not match any defined route, THEN THE Router SHALL redirect to the catalog page
