# Implementation Plan: Furniture Bid System

## Overview

This plan implements a Vue 3 + TypeScript real-time furniture auction marketplace front-end. The implementation proceeds from foundational scaffolding (types, config, services) through core features (auth, catalog, bidding) to advanced features (seller/admin dashboards, analytics) and concludes with documentation and integration wiring.

## Tasks

- [x] 1. Project scaffolding and core type definitions
  - [x] 1.1 Set up project configuration and TailwindCSS theme
    - Configure `tailwind.config.ts` with custom colors (primary, secondary, accent, background, card, success, text), font family (Inter), responsive breakpoints (mobile, tablet, desktop), and touch target sizes
    - Create `src/assets/styles/tailwind.css` with Tailwind directives and custom utility classes
    - Configure Vitest in `vitest.config.ts` with Vue plugin, path aliases, and test setup file
    - _Requirements: 17.1, 17.2_

  - [x] 1.2 Create TypeScript type definitions
    - Create `src/types/auth.ts` with User, UserRole, LoginRequest, RegisterRequest, LoginResponse, AuthTokenPayload, PasswordResetRequest interfaces
    - Create `src/types/furniture.ts` with FurnitureListing, FurnitureListingSummary, CreateListingRequest, CatalogFilters, CatalogSortOption, CatalogQuery, FurnitureCategory, FurnitureCondition, ListingStatus, Dimensions interfaces
    - Create `src/types/auction.ts` with Bid, PlaceBidRequest, PlaceBidResponse, AutoBidConfig, AutoBidRequest, BidHistoryQuery, AuctionResult, SellerActiveListing, SellerCompletedAuction interfaces
    - Create `src/types/notification.ts` with Notification, NotificationType, NotificationListQuery interfaces
    - Create `src/types/user.ts` with AdminUserRow, AdminListingRow, ListingReport, AnalyticsSummary, AuctionTrend, CategoryDistribution, TopSeller, AnalyticsQuery, AccountStatus interfaces
    - Create `src/types/common.ts` with PaginatedResponse, ApiError, ConnectionStatus, WebSocketEvent, BidUpdateEvent, AuctionEndEvent interfaces
    - _Requirements: 14.3, 15.2, 15.3_

  - [x] 1.3 Create utility modules
    - Create `src/utils/constants.ts` with bid increment, pagination defaults (page size 20), toast durations, reconnection config, and max image upload count/size constants
    - Create `src/utils/formatters.ts` with currency formatting, date/time formatting, countdown timer formatting (days, hours, minutes, seconds), and bidder alias anonymization
    - Create `src/utils/validators.ts` with Zod schemas for registration (email, password 8-64 chars with uppercase/lowercase/digit, displayName 3-50 chars), login, create listing (title 5-100, description 20-2000, price ranges, dimensions 1-9999, weight 0.1-9999, end date 24h-30d), and bid validation
    - _Requirements: 1.1, 4.1, 5.1, 8.2_

  - [ ]* 1.4 Write property tests for validation schemas
    - **Property 1: Registration validation schema correctness** — test that the Zod schema accepts/rejects inputs based on email format, password 8-64 chars with required character classes, and displayName 3-50 chars
    - **Property 8: Listing form validation schema correctness** — test title 5-100, description 20-2000, starting price 0.01-999999.99, reserve ≥ starting, dimensions 1-9999, weight 0.1-9999, end date 24h-30d
    - **Validates: Requirements 1.1, 8.2**

  - [ ]* 1.5 Write property tests for sorting and formatting utilities
    - **Property 5: Catalog sort ordering** — test that sort function produces correct ordering for all four sort options (price-low-high, price-high-low, ending-soonest, newest)
    - **Property 7: Notification count display formatting** — test that counts ≤ 99 render as numeric string, counts > 99 render as "99+"
    - **Validates: Requirements 2.3, 7.1**

- [x] 2. API service layer and mock infrastructure
  - [x] 2.1 Implement Axios client with interceptors
    - Create `src/services/api/client.ts` with Axios instance configured with base URL from `VITE_API_BASE_URL`, 30s timeout, JSON content type, request interceptor for JWT attachment, and response interceptor for 401/403/429/5xx error handling
    - Implement `getApiClient()` function that returns mock client when `VITE_USE_MOCKS=true`
    - _Requirements: 14.1, 14.5, 14.6, 14.7_

  - [ ]* 2.2 Write property tests for interceptors
    - **Property 14: Request interceptor JWT attachment** — test that Authorization header is set to "Bearer {token}" when token exists and not set when token is null
    - **Property 15: Response interceptor error routing** — test that 401 triggers refresh/logout, 403 redirects, 429 shows rate limit, 5xx shows server error
    - **Validates: Requirements 14.5, 14.6**

  - [x] 2.3 Implement API service modules
    - Create `src/services/api/authService.ts` with login, register, resetPassword, refreshToken, socialLogin typed functions
    - Create `src/services/api/userProfileService.ts` with getProfile, updateProfile, getWatchlist, addToWatchlist, removeFromWatchlist typed functions
    - Create `src/services/api/auctionService.ts` with placeBid, getBidHistory, activateAutoBid, deactivateAutoBid, getSellerActiveListings, getSellerCompletedAuctions typed functions
    - Create `src/services/api/furnitureService.ts` with getListings, getListingById, createListing, flagListing, removeListing typed functions
    - Create `src/services/api/notificationService.ts` with getNotifications, markAsRead, markAllAsRead typed functions
    - Create `src/services/api/paymentService.ts` with placeholder typed functions for payment endpoints
    - _Requirements: 14.2, 14.3_

  - [x] 2.4 Implement mock data layer
    - Create `src/services/mock/index.ts` with mock mode detection logic via `VITE_USE_MOCKS`
    - Create `src/services/mock/mockData.ts` with realistic sample data generators for all entity types (users, listings, bids, notifications)
    - Create `src/services/mock/mockApiHandlers.ts` implementing all API service functions with mock data and simulated delays
    - _Requirements: 14.4_

- [x] 3. WebSocket service layer
  - [x] 3.1 Implement Socket.IO singleton client
    - Create `src/services/websocket/events.ts` with typed event name constants and payload types
    - Create `src/services/websocket/socketClient.ts` as a singleton SocketService class with: connect(token), disconnect(), typed event subscription methods (onBidUpdate, onOutbid, onAuctionEnding, onAuctionWon, onAuctionLost, onNotification), room management (joinAuctionRoom, leaveAuctionRoom, subscribeNotifications), reactive connection status, exponential backoff reconnection (1s→30s, max 10 attempts), token expiry handling, and resubscription on reconnect
    - _Requirements: 15.1, 15.2, 15.3, 15.5_

  - [x] 3.2 Implement WebSocket mock mode
    - Create `src/services/mock/mockSocketHandlers.ts` that simulates WebSocket events at configurable intervals (bid updates, notifications, auction endings) when `VITE_USE_MOCKS=true`
    - _Requirements: 15.4_

  - [x] 3.3 Write property tests for WebSocket service
    - **Property 9: WebSocket reconnection backoff calculation** — test that delay equals min(1000 × 2^(N−1), 30000) for attempts 1–10
    - **Property 16: Socket subscription cleanup** — test that disconnect results in zero event listeners and empty subscribed rooms set
    - **Validates: Requirements 13.4, 15.6**

- [x] 4. State management (Pinia stores)
  - [x] 4.1 Implement auth store
    - Create `src/stores/auth.ts` with token, user, isLoading state; isAuthenticated, userRole, userId getters; login, loginWithOAuth, register, logout, refreshToken, restoreSession, clearAuth actions; localStorage persistence for token
    - _Requirements: 1.5, 1.8, 18.1, 18.2, 18.5_

  - [x] 4.2 Implement furniture store
    - Create `src/stores/furniture.ts` with listings, currentListing, filters, sort, page, hasMore, isLoading state; activeFiltersCount getter; fetchListings, fetchListingById, createListing, updateFilters, updateSort, updateCurrentBid actions
    - _Requirements: 2.1, 2.4, 18.1_

  - [x] 4.3 Implement auction store
    - Create `src/stores/auction.ts` with currentBids Map, autoBidConfigs Map, bidSubmitting state; getBidsForAuction, getAutoBidConfig, hasActiveBids getters; placeBid, fetchBidHistory, activateAutoBid, deactivateAutoBid, handleBidUpdate actions
    - _Requirements: 4.2, 5.2, 5.6, 18.1_

  - [x] 4.4 Implement notification store
    - Create `src/stores/notification.ts` with notifications, unreadCount, isLoading state; displayCount, recentNotifications getters; fetchNotifications, addNotification, markAsRead, markAllAsRead actions
    - _Requirements: 7.1, 7.4, 7.6, 18.1_

  - [x] 4.5 Implement watchlist store
    - Create `src/stores/watchlist.ts` with watchedItems, watchedIds Set, isLoading state; watchlistCount, isWatched getters; fetchWatchlist, addToWatchlist (optimistic), removeFromWatchlist (optimistic with rollback), updateWatchedItemBid actions
    - _Requirements: 6.1, 6.2, 6.3, 18.1_

  - [ ]* 4.6 Write property tests for store logic
    - **Property 11: Store getters derived state correctness** — test isAuthenticated returns true iff token non-null, userRole returns role or null, unreadCount equals count of unread notifications
    - **Property 17: Auth token persistence round-trip** — test that storing a token and retrieving it produces the identical string
    - **Property 18: Store reset on 401** — test that handling 401 resets all stores to defaults and clears localStorage
    - **Validates: Requirements 18.2, 18.3, 18.4**

- [x] 5. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 6. Router and navigation guards
  - [x] 6.1 Implement Vue Router configuration
    - Create `src/router/index.ts` with all route definitions: public routes (login, register, forgot-password, catalog, listing/:id), authenticated routes (watchlist, bidding-history, profile), seller routes (seller/dashboard, seller/create-listing), admin route (admin), and catch-all redirect to catalog
    - Use lazy loading via dynamic imports for all page components
    - _Requirements: 19.1, 19.7_

  - [x] 6.2 Implement navigation guards
    - Create `src/router/guards.ts` with authGuard (redirects unauthenticated users to login for protected routes) and roleGuard (redirects users without required role to catalog with unauthorized notification)
    - _Requirements: 1.9, 19.2, 19.3, 19.4, 19.5, 19.6_

  - [ ]* 6.3 Write property tests for route guards
    - **Property 12: Role-based route access control** — test that for any combination of user role and route path, access is granted iff route is public OR user is authenticated with a matching role; denied unauthenticated → login redirect; denied role → catalog redirect
    - **Validates: Requirements 19.1, 19.2, 19.3, 19.4, 19.5, 19.6**

- [x] 7. Common UI components
  - [x] 7.1 Implement shared layout components
    - Create `src/components/common/AppNavbar.vue` with responsive navigation (hamburger on mobile, condensed on tablet, full on desktop), user menu with role-based links, notification bell integration, and logout action
    - Create `src/components/common/ConnectionStatus.vue` displaying WebSocket connection state (connected/reconnecting/disconnected) as a top-of-page banner
    - Create `src/App.vue` with AppNavbar, ConnectionStatus, Toast manager, and RouterView
    - _Requirements: 13.4, 17.3_

  - [x] 7.2 Implement feedback and state components
    - Create `src/components/common/AppToast.vue` with success (3s), warning (5s), error (5s), info (4s) auto-dismiss timers, color coding, manual dismiss button, max 3 visible, stacked top-right
    - Create `src/components/common/AppModal.vue` with confirmation dialog pattern (title, message, confirm/cancel actions)
    - Create `src/components/common/LoadingSpinner.vue` for loading states
    - Create `src/components/common/ErrorState.vue` with contextual error message and retry button
    - Create `src/components/common/EmptyState.vue` with guidance message
    - Create `src/components/common/PaginationControls.vue` for paginated data
    - _Requirements: 2.7, 2.8, 10.3, 17.5_

- [x] 8. Authentication pages and components
  - [x] 8.1 Implement auth form components
    - Create `src/components/auth/LoginForm.vue` with email and password fields, real-time validation using VeeValidate + Zod, inline error display, loading indicator on submit, and form preservation on error
    - Create `src/components/auth/RegisterForm.vue` with email, password, and display name fields, real-time validation (email format, password 8-64 chars with uppercase/lowercase/digit, displayName 3-50 chars), and inline error display
    - Create `src/components/auth/ForgotPasswordForm.vue` with email field and confirmation message display
    - Create `src/components/auth/SocialLoginButtons.vue` with Google OAuth and Facebook OAuth buttons, error handling for cancelled/failed OAuth flows
    - _Requirements: 1.1, 1.2, 1.3, 1.6, 1.7, 1.10, 1.11_

  - [x] 8.2 Implement auth pages
    - Create `src/pages/LoginPage.vue` composing LoginForm and SocialLoginButtons with redirect to catalog on success
    - Create `src/pages/RegisterPage.vue` composing RegisterForm with redirect to login on success with success message
    - Create `src/pages/ForgotPasswordPage.vue` composing ForgotPasswordForm
    - _Requirements: 1.4, 1.5_

  - [ ]* 8.3 Write unit tests for auth components
    - Test LoginForm renders fields and shows validation errors on invalid input
    - Test RegisterForm enforces password complexity rules
    - Test SocialLoginButtons handles OAuth error/cancel states
    - Test auth store login/logout/restoreSession flows
    - _Requirements: 1.1, 1.5, 1.7, 1.8_

- [x] 9. Catalog browsing
  - [x] 9.1 Implement catalog components
    - Create `src/components/catalog/ListingCard.vue` displaying thumbnail, title, current bid, time remaining, condition badge with responsive card sizing
    - Create `src/components/catalog/CatalogGrid.vue` with responsive grid layout (1-col mobile, 2-col tablet, 3-4 col desktop)
    - Create `src/components/catalog/CatalogFilters.vue` with category multi-select, condition multi-select, price range inputs (min 0, max 999,999), and location text field
    - Create `src/components/catalog/CatalogSortDropdown.vue` with ending soonest (default), price low-high, price high-low, and newest options
    - _Requirements: 2.1, 2.2, 2.3, 17.2_

  - [x] 9.2 Implement catalog page with infinite scroll
    - Create `src/composables/useInfiniteScroll.ts` for detecting scroll position and triggering next page loads
    - Create `src/pages/CatalogPage.vue` composing CatalogFilters, CatalogSortDropdown, CatalogGrid with infinite scroll loading 20 items per batch, error state with retry, and empty state when no results match
    - _Requirements: 2.4, 2.5, 2.6, 2.7, 2.8_

  - [ ]* 9.3 Write unit tests for catalog components
    - Test ListingCard renders bid price and time remaining correctly
    - Test CatalogFilters emits filter change events
    - Test CatalogGrid displays correct number of items
    - Test infinite scroll triggers fetch on scroll threshold
    - _Requirements: 2.1, 2.5_

- [x] 10. Listing detail page and bidding
  - [x] 10.1 Implement listing detail components
    - Create `src/composables/useCountdown.ts` for reactive countdown timer (days, hours, minutes, seconds) with automatic update and zero-state detection
    - Create `src/composables/useImageGallery.ts` for image navigation state (current index, next, previous, position indicator)
    - Create `src/components/listing/ImageGallery.vue` with prev/next navigation, image position indicator, and support for up to 10 images
    - Create `src/components/listing/AuctionCountdown.vue` displaying days, hours, minutes, seconds remaining
    - Create `src/components/listing/SellerInfo.vue` displaying seller name and rating
    - Create `src/components/listing/ListingDetail.vue` displaying title, description, category, condition, brand, material, dimensions, weight, location
    - _Requirements: 3.1, 3.2, 3.3, 3.4_

  - [x] 10.2 Implement bid panel and bid history
    - Create `src/composables/useAuction.ts` for bid placement logic, auto-bid management, and WebSocket bid update handling
    - Create `src/components/listing/BidPanel.vue` with bid input (pre-filled with minimum next bid), submit button, inline success/error messages, disabled state when auction ended, and real-time update via WebSocket
    - Create `src/components/listing/BidHistory.vue` displaying 20 most recent bids in reverse chronological order with anonymized bidder aliases and timestamps, and load-more capability
    - Create `src/components/listing/AutoBidToggle.vue` with max amount input, activate/deactivate toggle, active status display, and limit-reached notification
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7_

  - [x] 10.3 Implement listing detail page with WebSocket integration
    - Create `src/pages/ListingDetailPage.vue` composing ImageGallery, ListingDetail, BidPanel, BidHistory, AutoBidToggle, AuctionCountdown, SellerInfo with WebSocket subscription on mount and unsubscription on unmount, real-time bid/count updates, auction end detection, and watchlist toggle button
    - _Requirements: 3.5, 3.6, 3.7, 3.8, 6.1, 13.2, 13.3, 13.5_

  - [ ]* 10.4 Write property tests for bidding logic
    - **Property 2: Bid amount validation** — test that bids ≥ currentHighestBid + increment within 0.01–999,999,999.99 with ≤ 2 decimal places are accepted; others rejected
    - **Property 3: Auto-bid maximum amount validation** — test that max amount ≥ currentHighestBid + increment and ≤ 999,999,999.99 are accepted
    - **Property 4: Auto-bid limit detection** — test that auto-bid deactivates iff newHighestBid + increment > maxAmount
    - **Validates: Requirements 4.1, 4.3, 5.1, 5.4**

  - [ ]* 10.5 Write unit tests for listing detail components
    - Test ImageGallery navigation and position indicator
    - Test AuctionCountdown displays correct values and handles zero state
    - Test BidPanel disabled state when auction ended
    - Test AutoBidToggle state transitions
    - _Requirements: 3.2, 3.3, 3.5, 4.8, 5.5_

- [x] 11. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 12. Watchlist feature
  - [x] 12.1 Implement watchlist components and page
    - Create `src/components/watchlist/WatchlistCard.vue` displaying thumbnail, title, current bid, time remaining, and status badges (Outbid, Won, Lost, Reserve Not Met)
    - Create `src/components/watchlist/WatchlistGrid.vue` displaying watched items sorted by ending soonest
    - Create `src/pages/WatchlistPage.vue` composing WatchlistGrid with empty state, loading state, and error state with retry
    - _Requirements: 6.2, 6.3, 6.4, 6.5_

  - [ ]* 12.2 Write property tests for watchlist logic
    - **Property 6: Watchlist sort ordering** — test that watchlist items are ordered by non-decreasing time remaining (ending soonest first)
    - **Property 8: Watchlist item status mapping** — test Won if user is highest bidder, Lost if outbid and reserve met, Reserve Not Met if reserve not reached, Outbid badge for active auctions where outbid
    - **Validates: Requirements 6.3, 6.4, 6.5**

- [x] 13. Notification system
  - [x] 13.1 Implement notification components
    - Create `src/composables/useNotification.ts` for notification management (receive, mark read, count formatting)
    - Create `src/components/notifications/NotificationBell.vue` with unread count badge (numeric or "99+") in app header
    - Create `src/components/notifications/NotificationDropdown.vue` displaying 20 most recent notifications sorted by most recent, with load-more capability and "Mark all as read" action
    - Create `src/components/notifications/NotificationItem.vue` rendering individual notification with type icon, title, message, timestamp, and read/unread state
    - _Requirements: 7.1, 7.2, 7.3, 7.5, 7.6_

  - [x] 13.2 Wire notification real-time updates
    - Integrate NotificationBell into AppNavbar
    - Connect WebSocket onNotification handler to notification store (increment unread count, add notification, display toast that auto-dismisses in 5 seconds)
    - Implement click-through navigation from notification to relevant listing page with mark-as-read
    - _Requirements: 7.3, 7.4, 7.5_

  - [ ]* 13.3 Write unit tests for notification components
    - Test NotificationBell displays correct badge count
    - Test NotificationDropdown renders notification list
    - Test mark all as read resets count
    - Test toast auto-dismiss after 5 seconds
    - _Requirements: 7.1, 7.4, 7.6_

- [x] 14. Seller dashboard and listing creation
  - [x] 14.1 Implement seller dashboard components
    - Create `src/components/seller/ActiveListings.vue` displaying paginated list of active listings sorted by ending soonest (title, current bid, bid count, time remaining) with real-time bid updates via WebSocket
    - Create `src/components/seller/CompletedAuctions.vue` displaying paginated list of completed auctions sorted by most recently ended (title, winning bid, winner name, reserve met status)
    - Create `src/components/seller/SellerListingCard.vue` for individual listing row rendering
    - Create `src/pages/SellerDashboardPage.vue` composing ActiveListings and CompletedAuctions with empty states, error states with retry, and navigation to listing detail on click
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6_

  - [x] 14.2 Implement listing creation form
    - Create `src/components/seller/ImageUploader.vue` with drag-and-drop, file picker, 1-10 image limit, 5MB max per image, JPEG/PNG/WebP validation, preview display, and removal capability
    - Create `src/components/seller/CreateListingForm.vue` with all required fields (title, description, category, condition, dimensions, starting price, reserve price, auction end date, images) and optional fields (brand, material, weight, location), VeeValidate + Zod validation, inline errors, duplicate submission prevention
    - Create `src/pages/CreateListingPage.vue` composing CreateListingForm with success confirmation and link to new listing
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6_

  - [ ]* 14.3 Write property tests for listing creation validation
    - **Property 10: Image upload validation** — test that file sets are accepted iff count 1-10, each ≤ 5MB, each JPEG/PNG/WebP; rejected otherwise
    - **Property 20: Date range validation** — test that auction end date is accepted iff between 24 hours and 30 days in the future
    - **Validates: Requirements 8.3, 12.5**

  - [ ]* 14.4 Write unit tests for seller components
    - Test ImageUploader validates file type and size
    - Test CreateListingForm disables submit during submission
    - Test ActiveListings renders real-time bid updates
    - Test CompletedAuctions shows reserve met indicator
    - _Requirements: 8.3, 8.4, 9.1, 9.2_

- [x] 15. Admin dashboard
  - [x] 15.1 Implement admin user management
    - Create `src/components/admin/UserManagementTable.vue` with paginated table (20 per page) displaying display name, email, role, registration date, account status; search by name/email; suspend/activate/delete actions with confirmation dialog for delete
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

  - [x] 15.2 Implement admin listing management
    - Create `src/components/admin/ListingManagementTable.vue` with searchable paginated table (20 per page) of listings showing title, seller, status (active/flagged/removed badge), current bid, report count; remove and flag actions with confirmation dialog
    - Create `src/components/admin/ReportedListings.vue` displaying reported listings with report reason, reporter name, and date sorted by most recent
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6_

  - [x] 15.3 Implement admin analytics
    - Create `src/components/admin/AnalyticsSummaryCards.vue` displaying total users, active auctions, completed auctions, total revenue
    - Create `src/components/admin/AuctionLineChart.vue` using vue-chartjs for auctions created/completed per day over selected date range
    - Create `src/components/admin/CategoryBarChart.vue` using vue-chartjs for listings count per category
    - Create `src/components/admin/TopSellersTable.vue` displaying top 10 sellers by completed auctions and revenue
    - Create `src/components/admin/DateRangePicker.vue` defaulting to last 30 days, allowing up to 12 months range, triggering data refresh on change
    - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5, 12.6_

  - [x] 15.4 Implement admin dashboard page
    - Create `src/pages/AdminDashboardPage.vue` composing UserManagementTable, ListingManagementTable, ReportedListings, AnalyticsSummaryCards, AuctionLineChart, CategoryBarChart, TopSellersTable, DateRangePicker with error states and retry buttons
    - _Requirements: 10.1, 11.1, 12.1_

  - [ ]* 15.5 Write property tests for admin search
    - **Property 12: Admin search filtering** — test that search returns only items where query appears case-insensitively as substring of searchable fields (displayName/email for users; title/seller displayName for listings)
    - **Validates: Requirements 10.1, 11.1**

  - [ ]* 15.6 Write unit tests for admin components
    - Test UserManagementTable search filtering and pagination
    - Test confirmation dialog appears before delete
    - Test AnalyticsSummaryCards renders correct values
    - Test DateRangePicker enforces 12-month max range
    - _Requirements: 10.1, 10.3, 12.1, 12.5_

- [x] 16. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 17. Application entry point and session management
  - [x] 17.1 Implement main application entry and session restore
    - Create `src/main.ts` with Vue app creation, Pinia plugin, Router plugin, and app mount
    - Implement session restoration in App.vue: on load, check localStorage for JWT token, validate by fetching user profile, restore auth store state, then allow navigation; handle localStorage unavailability with in-memory fallback and warning notification
    - _Requirements: 18.5, 18.6_

  - [x] 17.2 Implement global 401 handling and store reset
    - Wire response interceptor 401 handling to clear all Pinia domain stores (auth, auction, furniture, notifications, watchlist) to initial defaults, clear localStorage token, and redirect to login
    - _Requirements: 18.3_

  - [ ]* 17.3 Write property test for 401 store reset
    - **Property 10: Unauthorized response clears all stores** — test that after 401 handling, all stores are at initial defaults and localStorage is cleared
    - **Validates: Requirements 18.3**

- [x] 18. Responsive layout and mobile optimization
  - [x] 18.1 Implement responsive layouts
    - Apply responsive grid breakpoints to CatalogGrid (1/2/3-4 columns)
    - Convert data tables (UserManagementTable, ListingManagementTable, TopSellersTable) to stacked card layout on mobile viewports (< 768px)
    - Ensure all interactive elements have minimum 44x44px touch targets on mobile
    - Verify forms, cards, and tables render without horizontal scrolling at all breakpoints
    - _Requirements: 17.2, 17.4, 17.5_

- [x] 19. Additional pages and remaining wiring
  - [x] 19.1 Implement remaining pages
    - Create `src/pages/BiddingHistoryPage.vue` displaying user's bid history with pagination
    - Create `src/pages/UserProfilePage.vue` with user profile display and edit capability
    - Create `src/pages/NotFoundPage.vue` with redirect to catalog
    - _Requirements: 19.7_

  - [x] 19.2 Wire WebSocket lifecycle to application
    - Connect WebSocket on login (establish connection with JWT token within 5 seconds)
    - Subscribe to notification channel on authentication
    - Join auction room on ListingDetailPage mount; leave on unmount
    - Disconnect WebSocket on logout
    - Handle token refresh for expired WebSocket connections
    - Re-subscribe to all channels on reconnection
    - _Requirements: 13.1, 13.2, 13.5, 13.6, 13.7, 15.6_

  - [ ]* 19.3 Write integration tests
    - Test auth flow: login → token stored → redirect to catalog → logout → stores cleared
    - Test bidding flow: place bid → success response → bid history updated → WebSocket update received
    - Test watchlist flow: add item → optimistic update → API confirms; add item → API fails → rollback
    - Test WebSocket lifecycle: connect on login → join room → receive event → leave room → disconnect on logout
    - _Requirements: 1.5, 1.8, 4.2, 6.2, 13.1_

- [x] 20. API documentation deliverable
  - [x] 20.1 Create API reference document
    - Create `docs/api-reference.md` documenting all REST API endpoints organized by microservice (Identity, User Profile, Auction, Furniture, Notification, Payment) with: HTTP method, URL path, path/query parameters, request body schema (TypeScript interface), response body schema (TypeScript interface), authentication requirement, and example request/response pairs
    - Include WebSocket Events section listing all Socket.IO events (bid:{auctionId}, outbid, auction:ending, auction:won, auction:lost, notification, join:auction, leave:auction, subscribe:notifications) with event name, payload schema, direction, and description
    - Include Error Response Format section defining standard error shape (statusCode, errorCode, message, fieldErrors)
    - Include Authentication section describing JWT format (userId, role, exp claims), token refresh flow, and OAuth callback URL patterns
    - _Requirements: 16.1, 16.2, 16.3, 16.4, 16.5_

- [x] 21. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation at logical boundaries
- Property tests validate universal correctness properties from the design document
- Unit tests validate specific examples and edge cases
- The mock layer (`VITE_USE_MOCKS=true`) enables full front-end development without backend services
- All components use Vue 3 Composition API with TypeScript
- TailwindCSS utility classes are used for all styling with the custom theme

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2"] },
    { "id": 1, "tasks": ["1.3", "2.1"] },
    { "id": 2, "tasks": ["1.4", "1.5", "2.2", "2.3", "3.1"] },
    { "id": 3, "tasks": ["2.4", "3.2", "3.3", "4.1"] },
    { "id": 4, "tasks": ["4.2", "4.3", "4.4", "4.5"] },
    { "id": 5, "tasks": ["4.6", "6.1"] },
    { "id": 6, "tasks": ["6.2", "7.1", "7.2"] },
    { "id": 7, "tasks": ["6.3", "8.1", "9.1"] },
    { "id": 8, "tasks": ["8.2", "8.3", "9.2"] },
    { "id": 9, "tasks": ["9.3", "10.1"] },
    { "id": 10, "tasks": ["10.2", "10.3"] },
    { "id": 11, "tasks": ["10.4", "10.5", "12.1"] },
    { "id": 12, "tasks": ["12.2", "13.1"] },
    { "id": 13, "tasks": ["13.2", "13.3", "14.1"] },
    { "id": 14, "tasks": ["14.2", "14.3", "14.4"] },
    { "id": 15, "tasks": ["15.1", "15.2", "15.3"] },
    { "id": 16, "tasks": ["15.4", "15.5", "15.6"] },
    { "id": 17, "tasks": ["17.1", "17.2"] },
    { "id": 18, "tasks": ["17.3", "18.1"] },
    { "id": 19, "tasks": ["19.1", "19.2"] },
    { "id": 20, "tasks": ["19.3", "20.1"] }
  ]
}
```
