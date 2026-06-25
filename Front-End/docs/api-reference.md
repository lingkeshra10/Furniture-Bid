# API Reference

This document describes all REST API endpoints and WebSocket events consumed by the Furniture Bid System front-end. Backend services should implement these contracts to ensure compatibility with the client application.

**Base URL:** Configured via `VITE_API_BASE_URL` environment variable (default: `/api`)

---

## Table of Contents

- [Authentication](#authentication)
- [Identity Service](#identity-service)
- [User Profile Service](#user-profile-service)
- [Auction Service](#auction-service)
- [Furniture Service](#furniture-service)
- [Notification Service](#notification-service)
- [Payment Service](#payment-service)
- [Admin Endpoints](#admin-endpoints)
- [WebSocket Events](#websocket-events)
- [Error Response Format](#error-response-format)

---

## Authentication

### JWT Token Format

All authenticated requests include a JWT token in the `Authorization` header:

```
Authorization: Bearer <token>
```

The JWT payload contains the following claims:

```typescript
interface AuthTokenPayload {
  userId: string;   // Unique user identifier
  role: UserRole;   // 'buyer' | 'seller' | 'admin'
  exp: number;      // Expiration timestamp (Unix seconds)
}
```

### Token Refresh Flow

1. Client detects a `401 Unauthorized` response from any API call
2. Client sends a `POST /auth/refresh-token` request (the existing token is attached via the request interceptor)
3. If refresh succeeds, the client receives a new token and retries the original request
4. If refresh fails, the client clears all stores, removes the token from `localStorage`, and redirects to `/login`

### OAuth Callback URL Patterns

Social login uses a two-step flow:

1. Client redirects user to the OAuth provider's authorization page
2. Provider redirects back to the application callback URL with an authorization code/token
3. Client sends the provider token to `POST /auth/social-login`

Expected callback URL patterns:
- Google: `{APP_URL}/auth/callback/google`
- Facebook: `{APP_URL}/auth/callback/facebook`

---

## Identity Service

### POST /auth/login

Log in with email and password credentials.

| Property | Value |
|----------|-------|
| **Auth Required** | No |

**Request Body:**

```typescript
interface LoginRequest {
  email: string;
  password: string;
}
```

**Response Body:**

```typescript
interface LoginResponse {
  token: string;
  user: User;
}

interface User {
  id: string;
  email: string;
  displayName: string;
  role: 'buyer' | 'seller' | 'admin';
  avatarUrl?: string;
  createdAt: string;
}
```

**Example Request:**

```json
POST /auth/login
Content-Type: application/json

{
  "email": "buyer@example.com",
  "password": "SecurePass1"
}
```

**Example Response:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "usr_abc123",
    "email": "buyer@example.com",
    "displayName": "Jane Doe",
    "role": "buyer",
    "avatarUrl": "https://cdn.example.com/avatars/abc123.jpg",
    "createdAt": "2024-01-15T10:30:00Z"
  }
}
```

---

### POST /auth/register

Register a new user account.

| Property | Value |
|----------|-------|
| **Auth Required** | No |

**Request Body:**

```typescript
interface RegisterRequest {
  email: string;        // Valid email format
  password: string;     // 8-64 chars, at least 1 uppercase, 1 lowercase, 1 digit
  displayName: string;  // 3-50 characters
}
```

**Response Body:**

```typescript
interface LoginResponse {
  token: string;
  user: User;
}
```

**Example Request:**

```json
POST /auth/register
Content-Type: application/json

{
  "email": "newuser@example.com",
  "password": "MyPassword1",
  "displayName": "John Smith"
}
```

**Example Response:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "usr_def456",
    "email": "newuser@example.com",
    "displayName": "John Smith",
    "role": "buyer",
    "createdAt": "2024-03-10T14:00:00Z"
  }
}
```

---

### POST /auth/reset-password

Request a password reset email.

| Property | Value |
|----------|-------|
| **Auth Required** | No |

**Request Body:**

```typescript
interface PasswordResetRequest {
  email: string;
}
```

**Response Body:** None (204 No Content)

**Example Request:**

```json
POST /auth/reset-password
Content-Type: application/json

{
  "email": "user@example.com"
}
```

---

### POST /auth/refresh-token

Refresh an expired JWT token.

| Property | Value |
|----------|-------|
| **Auth Required** | Yes (expired token accepted) |

**Request Body:** None (token is sent via Authorization header)

**Response Body:**

```typescript
interface LoginResponse {
  token: string;
  user: User;
}
```

**Example Request:**

```json
POST /auth/refresh-token
Authorization: Bearer <expired-token>
```

**Example Response:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "usr_abc123",
    "email": "buyer@example.com",
    "displayName": "Jane Doe",
    "role": "buyer",
    "createdAt": "2024-01-15T10:30:00Z"
  }
}
```

---

### POST /auth/social-login

Authenticate using a social provider (Google or Facebook) OAuth token.

| Property | Value |
|----------|-------|
| **Auth Required** | No |

**Request Body:**

```typescript
interface SocialLoginRequest {
  provider: 'google' | 'facebook';
  token: string;  // OAuth access token from provider
}
```

**Response Body:**

```typescript
interface LoginResponse {
  token: string;
  user: User;
}
```

**Example Request:**

```json
POST /auth/social-login
Content-Type: application/json

{
  "provider": "google",
  "token": "ya29.a0AfH6SMBx..."
}
```

**Example Response:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "usr_ghi789",
    "email": "social@gmail.com",
    "displayName": "Social User",
    "role": "buyer",
    "avatarUrl": "https://lh3.googleusercontent.com/...",
    "createdAt": "2024-02-20T08:15:00Z"
  }
}
```

---

## User Profile Service

### GET /users/profile

Get the authenticated user's profile.

| Property | Value |
|----------|-------|
| **Auth Required** | Yes |

**Query Parameters:** None

**Response Body:**

```typescript
interface User {
  id: string;
  email: string;
  displayName: string;
  role: 'buyer' | 'seller' | 'admin';
  avatarUrl?: string;
  createdAt: string;
}
```

**Example Request:**

```
GET /users/profile
Authorization: Bearer <token>
```

**Example Response:**

```json
{
  "id": "usr_abc123",
  "email": "buyer@example.com",
  "displayName": "Jane Doe",
  "role": "buyer",
  "avatarUrl": "https://cdn.example.com/avatars/abc123.jpg",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

---

### PUT /users/profile

Update the authenticated user's profile.

| Property | Value |
|----------|-------|
| **Auth Required** | Yes |

**Request Body:**

```typescript
interface UpdateProfileRequest {
  displayName?: string;
  avatarUrl?: string;
}
```

**Response Body:**

```typescript
interface User {
  id: string;
  email: string;
  displayName: string;
  role: 'buyer' | 'seller' | 'admin';
  avatarUrl?: string;
  createdAt: string;
}
```

**Example Request:**

```json
PUT /users/profile
Authorization: Bearer <token>
Content-Type: application/json

{
  "displayName": "Jane Updated"
}
```

**Example Response:**

```json
{
  "id": "usr_abc123",
  "email": "buyer@example.com",
  "displayName": "Jane Updated",
  "role": "buyer",
  "avatarUrl": "https://cdn.example.com/avatars/abc123.jpg",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

---

### GET /users/watchlist

Get the authenticated user's watchlist (paginated).

| Property | Value |
|----------|-------|
| **Auth Required** | Yes |

**Query Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | number | 1 | Page number |
| `pageSize` | number | 20 | Items per page |

**Response Body:**

```typescript
interface PaginatedResponse<FurnitureListingSummary> {
  data: FurnitureListingSummary[];
  total: number;
  page: number;
  pageSize: number;
  hasMore: boolean;
}

interface FurnitureListingSummary {
  id: string;
  title: string;
  thumbnailUrl: string;
  currentBid: number;
  timeRemaining: number;       // milliseconds
  condition: FurnitureCondition;
  category: FurnitureCategory;
}
```

**Example Request:**

```
GET /users/watchlist?page=1&pageSize=20
Authorization: Bearer <token>
```

**Example Response:**

```json
{
  "data": [
    {
      "id": "lst_001",
      "title": "Vintage Oak Dining Table",
      "thumbnailUrl": "https://cdn.example.com/images/lst_001_thumb.jpg",
      "currentBid": 450.00,
      "timeRemaining": 86400000,
      "condition": "good",
      "category": "dining-table"
    }
  ],
  "total": 5,
  "page": 1,
  "pageSize": 20,
  "hasMore": false
}
```

---

### POST /users/watchlist/:listingId

Add a listing to the user's watchlist.

| Property | Value |
|----------|-------|
| **Auth Required** | Yes |

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `listingId` | string | ID of the listing to watch |

**Request Body:** None

**Response Body:** None (204 No Content)

**Example Request:**

```
POST /users/watchlist/lst_001
Authorization: Bearer <token>
```

---

### DELETE /users/watchlist/:listingId

Remove a listing from the user's watchlist.

| Property | Value |
|----------|-------|
| **Auth Required** | Yes |

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `listingId` | string | ID of the listing to unwatch |

**Request Body:** None

**Response Body:** None (204 No Content)

**Example Request:**

```
DELETE /users/watchlist/lst_001
Authorization: Bearer <token>
```

---

## Auction Service

### POST /auctions/bids

Place a bid on an active auction.

| Property | Value |
|----------|-------|
| **Auth Required** | Yes |

**Request Body:**

```typescript
interface PlaceBidRequest {
  auctionId: string;
  amount: number;  // Must be >= currentHighestBid + increment, max 2 decimal places
}
```

**Response Body:**

```typescript
interface PlaceBidResponse {
  success: boolean;
  bid?: Bid;
  error?: string;
}

interface Bid {
  id: string;
  auctionId: string;
  bidderId: string;
  bidderAlias: string;   // Anonymized identifier
  amount: number;
  timestamp: string;     // ISO 8601
}
```

**Example Request:**

```json
POST /auctions/bids
Authorization: Bearer <token>
Content-Type: application/json

{
  "auctionId": "lst_001",
  "amount": 475.00
}
```

**Example Response (Success):**

```json
{
  "success": true,
  "bid": {
    "id": "bid_xyz789",
    "auctionId": "lst_001",
    "bidderId": "usr_abc123",
    "bidderAlias": "Bidder #7",
    "amount": 475.00,
    "timestamp": "2024-03-15T14:30:00Z"
  }
}
```

**Example Response (Failure):**

```json
{
  "success": false,
  "error": "Bid amount must be at least $480.00"
}
```

---

### GET /auctions/:auctionId/bids

Get bid history for an auction (paginated, reverse chronological order).

| Property | Value |
|----------|-------|
| **Auth Required** | No |

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `auctionId` | string | Auction/listing ID |

**Query Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | number | 1 | Page number |
| `pageSize` | number | 20 | Items per page |

**Response Body:**

```typescript
interface PaginatedResponse<Bid> {
  data: Bid[];
  total: number;
  page: number;
  pageSize: number;
  hasMore: boolean;
}
```

**Example Request:**

```
GET /auctions/lst_001/bids?page=1&pageSize=20
```

**Example Response:**

```json
{
  "data": [
    {
      "id": "bid_xyz789",
      "auctionId": "lst_001",
      "bidderId": "usr_abc123",
      "bidderAlias": "Bidder #7",
      "amount": 475.00,
      "timestamp": "2024-03-15T14:30:00Z"
    },
    {
      "id": "bid_xyz788",
      "auctionId": "lst_001",
      "bidderId": "usr_def456",
      "bidderAlias": "Bidder #3",
      "amount": 450.00,
      "timestamp": "2024-03-15T14:25:00Z"
    }
  ],
  "total": 12,
  "page": 1,
  "pageSize": 20,
  "hasMore": false
}
```

---

### POST /auctions/:auctionId/auto-bid

Activate auto-bid for an auction.

| Property | Value |
|----------|-------|
| **Auth Required** | Yes |

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `auctionId` | string | Auction/listing ID |

**Request Body:**

```typescript
{
  maxAmount: number;  // Must be >= currentHighestBid + increment, max 999,999,999.99
}
```

**Response Body:** None (204 No Content)

**Example Request:**

```json
POST /auctions/lst_001/auto-bid
Authorization: Bearer <token>
Content-Type: application/json

{
  "maxAmount": 600.00
}
```

---

### DELETE /auctions/:auctionId/auto-bid

Deactivate auto-bid for an auction.

| Property | Value |
|----------|-------|
| **Auth Required** | Yes |

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `auctionId` | string | Auction/listing ID |

**Request Body:** None

**Response Body:** None (204 No Content)

**Example Request:**

```
DELETE /auctions/lst_001/auto-bid
Authorization: Bearer <token>
```

---

### GET /seller/active-listings

Get the authenticated seller's active auction listings (paginated).

| Property | Value |
|----------|-------|
| **Auth Required** | Yes (Seller or Admin role) |

**Query Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | number | 1 | Page number |
| `pageSize` | number | 20 | Items per page |

**Response Body:**

```typescript
interface PaginatedResponse<SellerActiveListing> {
  data: SellerActiveListing[];
  total: number;
  page: number;
  pageSize: number;
  hasMore: boolean;
}

interface SellerActiveListing {
  id: string;
  title: string;
  currentBid: number;
  bidCount: number;
  timeRemaining: number;  // milliseconds
}
```

**Example Request:**

```
GET /seller/active-listings?page=1&pageSize=20
Authorization: Bearer <token>
```

**Example Response:**

```json
{
  "data": [
    {
      "id": "lst_001",
      "title": "Vintage Oak Dining Table",
      "currentBid": 475.00,
      "bidCount": 12,
      "timeRemaining": 172800000
    }
  ],
  "total": 3,
  "page": 1,
  "pageSize": 20,
  "hasMore": false
}
```

---

### GET /seller/completed-auctions

Get the authenticated seller's completed auctions (paginated).

| Property | Value |
|----------|-------|
| **Auth Required** | Yes (Seller or Admin role) |

**Query Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | number | 1 | Page number |
| `pageSize` | number | 20 | Items per page |

**Response Body:**

```typescript
interface PaginatedResponse<SellerCompletedAuction> {
  data: SellerCompletedAuction[];
  total: number;
  page: number;
  pageSize: number;
  hasMore: boolean;
}

interface SellerCompletedAuction {
  id: string;
  title: string;
  winningBid: number;
  winnerDisplayName: string;
  reserveMet: boolean;
  endedAt: string;  // ISO 8601
}
```

**Example Request:**

```
GET /seller/completed-auctions?page=1&pageSize=20
Authorization: Bearer <token>
```

**Example Response:**

```json
{
  "data": [
    {
      "id": "lst_099",
      "title": "Mid-Century Modern Sofa",
      "winningBid": 1200.00,
      "winnerDisplayName": "John Smith",
      "reserveMet": true,
      "endedAt": "2024-03-10T18:00:00Z"
    }
  ],
  "total": 8,
  "page": 1,
  "pageSize": 20,
  "hasMore": false
}
```

---

## Furniture Service

### GET /furniture

Get furniture listings with filtering, sorting, and pagination.

| Property | Value |
|----------|-------|
| **Auth Required** | No |

**Query Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | number | 1 | Page number |
| `pageSize` | number | 20 | Items per page |
| `sort` | string | `'ending-soonest'` | Sort order: `ending-soonest`, `price-low-high`, `price-high-low`, `newest` |
| `category` | string[] | — | Filter by category (comma-separated) |
| `condition` | string[] | — | Filter by condition (comma-separated) |
| `priceMin` | number | — | Minimum price filter |
| `priceMax` | number | — | Maximum price filter (max 999,999) |
| `location` | string | — | Location text search |

**Response Body:**

```typescript
interface PaginatedResponse<FurnitureListingSummary> {
  data: FurnitureListingSummary[];
  total: number;
  page: number;
  pageSize: number;
  hasMore: boolean;
}

interface FurnitureListingSummary {
  id: string;
  title: string;
  thumbnailUrl: string;
  currentBid: number;
  timeRemaining: number;       // milliseconds
  condition: FurnitureCondition;
  category: FurnitureCategory;
}

type FurnitureCategory = 'sofa' | 'dining-table' | 'office-chair' | 'wardrobe'
  | 'bed-frame' | 'coffee-table' | 'cabinet' | 'bookshelf';

type FurnitureCondition = 'new' | 'like-new' | 'good' | 'fair' | 'poor';
```

**Example Request:**

```
GET /furniture?page=1&pageSize=20&sort=ending-soonest&category=sofa,dining-table&priceMin=100&priceMax=5000
```

**Example Response:**

```json
{
  "data": [
    {
      "id": "lst_001",
      "title": "Vintage Oak Dining Table",
      "thumbnailUrl": "https://cdn.example.com/images/lst_001_thumb.jpg",
      "currentBid": 475.00,
      "timeRemaining": 86400000,
      "condition": "good",
      "category": "dining-table"
    },
    {
      "id": "lst_002",
      "title": "Modern Leather Sofa",
      "thumbnailUrl": "https://cdn.example.com/images/lst_002_thumb.jpg",
      "currentBid": 800.00,
      "timeRemaining": 172800000,
      "condition": "like-new",
      "category": "sofa"
    }
  ],
  "total": 42,
  "page": 1,
  "pageSize": 20,
  "hasMore": true
}
```

---

### GET /furniture/:id

Get full details for a single furniture listing.

| Property | Value |
|----------|-------|
| **Auth Required** | No |

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | string | Listing ID |

**Response Body:**

```typescript
interface FurnitureListing {
  id: string;
  title: string;
  description: string;
  category: FurnitureCategory;
  condition: FurnitureCondition;
  brand?: string;
  material?: string;
  dimensions: Dimensions;
  weight?: number;           // kilograms
  location?: string;
  images: string[];          // URL array, 1-10 images
  startingPrice: number;
  reservePrice: number;
  currentBid: number;
  bidCount: number;
  auctionEndDate: string;    // ISO 8601
  status: ListingStatus;
  sellerId: string;
  sellerDisplayName: string;
  sellerRating: number;
  createdAt: string;
}

interface Dimensions {
  width: number;   // centimeters
  height: number;  // centimeters
  length: number;  // centimeters
}

type ListingStatus = 'active' | 'ended' | 'flagged' | 'removed';
```

**Example Request:**

```
GET /furniture/lst_001
```

**Example Response:**

```json
{
  "id": "lst_001",
  "title": "Vintage Oak Dining Table",
  "description": "Beautiful hand-crafted oak dining table from the 1960s. Seats 6 comfortably. Minor surface wear consistent with age.",
  "category": "dining-table",
  "condition": "good",
  "brand": "Heritage Furniture Co.",
  "material": "Oak wood",
  "dimensions": { "width": 180, "height": 75, "length": 90 },
  "weight": 45.5,
  "location": "San Francisco, CA",
  "images": [
    "https://cdn.example.com/images/lst_001_1.jpg",
    "https://cdn.example.com/images/lst_001_2.jpg",
    "https://cdn.example.com/images/lst_001_3.jpg"
  ],
  "startingPrice": 200.00,
  "reservePrice": 400.00,
  "currentBid": 475.00,
  "bidCount": 12,
  "auctionEndDate": "2024-03-20T18:00:00Z",
  "status": "active",
  "sellerId": "usr_seller01",
  "sellerDisplayName": "Antique Treasures",
  "sellerRating": 4.8,
  "createdAt": "2024-03-05T10:00:00Z"
}
```

---

### POST /furniture

Create a new furniture listing. Accepts multipart form data for image uploads.

| Property | Value |
|----------|-------|
| **Auth Required** | Yes (Seller or Admin role) |
| **Content-Type** | `multipart/form-data` |

**Request Body (form fields):**

```typescript
interface CreateListingRequest {
  title: string;             // 5-100 characters
  description: string;       // 20-2000 characters
  category: FurnitureCategory;
  condition: FurnitureCondition;
  brand?: string;
  material?: string;
  dimensions: string;        // JSON-stringified Dimensions object
  weight?: string;           // Numeric string (kg)
  location?: string;
  startingPrice: string;     // Numeric string, 0.01-999,999.99
  reservePrice: string;      // Numeric string, must be >= startingPrice
  auctionEndDate: string;    // ISO 8601, 24h to 30d from now
  images: File[];            // 1-10 files, max 5MB each, JPEG/PNG/WebP
}
```

**Response Body:**

```typescript
interface FurnitureListing {
  // Full listing object (same as GET /furniture/:id response)
}
```

**Example Request:**

```
POST /furniture
Authorization: Bearer <token>
Content-Type: multipart/form-data

--boundary
Content-Disposition: form-data; name="title"

Scandinavian Coffee Table
--boundary
Content-Disposition: form-data; name="description"

Minimalist Scandinavian design coffee table in excellent condition. Perfect for modern living rooms.
--boundary
Content-Disposition: form-data; name="category"

coffee-table
--boundary
Content-Disposition: form-data; name="condition"

like-new
--boundary
Content-Disposition: form-data; name="dimensions"

{"width":120,"height":45,"length":60}
--boundary
Content-Disposition: form-data; name="startingPrice"

150.00
--boundary
Content-Disposition: form-data; name="reservePrice"

300.00
--boundary
Content-Disposition: form-data; name="auctionEndDate"

2024-04-01T18:00:00Z
--boundary
Content-Disposition: form-data; name="images"; filename="table1.jpg"
Content-Type: image/jpeg

<binary data>
--boundary--
```

---

### PUT /furniture/:id/flag

Flag a listing for review (report).

| Property | Value |
|----------|-------|
| **Auth Required** | Yes |

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | string | Listing ID |

**Request Body:**

```typescript
{
  reason: string;  // Description of why the listing is being flagged
}
```

**Response Body:** None (204 No Content)

**Example Request:**

```json
PUT /furniture/lst_001/flag
Authorization: Bearer <token>
Content-Type: application/json

{
  "reason": "Listing appears to contain misleading photos"
}
```

---

### DELETE /furniture/:id

Remove a furniture listing (seller removes their own listing).

| Property | Value |
|----------|-------|
| **Auth Required** | Yes (Owner or Admin) |

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | string | Listing ID |

**Request Body:** None

**Response Body:** None (204 No Content)

**Example Request:**

```
DELETE /furniture/lst_001
Authorization: Bearer <token>
```

---

## Notification Service

### GET /notifications

Get notifications for the authenticated user (paginated, most recent first).

| Property | Value |
|----------|-------|
| **Auth Required** | Yes |

**Query Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | number | 1 | Page number |
| `pageSize` | number | 20 | Items per page |

**Response Body:**

```typescript
interface PaginatedResponse<Notification> {
  data: Notification[];
  total: number;
  page: number;
  pageSize: number;
  hasMore: boolean;
}

interface Notification {
  id: string;
  type: NotificationType;
  title: string;
  message: string;
  auctionId: string;
  isRead: boolean;
  createdAt: string;  // ISO 8601
}

type NotificationType = 'outbid' | 'auction-ending' | 'auction-won'
  | 'auction-lost' | 'auto-bid-placed' | 'auto-bid-limit-reached';
```

**Example Request:**

```
GET /notifications?page=1&pageSize=20
Authorization: Bearer <token>
```

**Example Response:**

```json
{
  "data": [
    {
      "id": "ntf_001",
      "type": "outbid",
      "title": "You've been outbid!",
      "message": "Someone placed a higher bid of $480.00 on Vintage Oak Dining Table",
      "auctionId": "lst_001",
      "isRead": false,
      "createdAt": "2024-03-15T15:00:00Z"
    },
    {
      "id": "ntf_002",
      "type": "auction-ending",
      "title": "Auction ending soon",
      "message": "The auction for Modern Leather Sofa ends in 15 minutes",
      "auctionId": "lst_002",
      "isRead": true,
      "createdAt": "2024-03-15T14:45:00Z"
    }
  ],
  "total": 25,
  "page": 1,
  "pageSize": 20,
  "hasMore": true
}
```

---

### PUT /notifications/:id/read

Mark a single notification as read.

| Property | Value |
|----------|-------|
| **Auth Required** | Yes |

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | string | Notification ID |

**Request Body:** None

**Response Body:** None (204 No Content)

**Example Request:**

```
PUT /notifications/ntf_001/read
Authorization: Bearer <token>
```

---

### PUT /notifications/read-all

Mark all notifications as read for the authenticated user.

| Property | Value |
|----------|-------|
| **Auth Required** | Yes |

**Request Body:** None

**Response Body:** None (204 No Content)

**Example Request:**

```
PUT /notifications/read-all
Authorization: Bearer <token>
```

---

## Payment Service

### POST /payments/create-payment-intent

Create a payment intent for a won auction.

| Property | Value |
|----------|-------|
| **Auth Required** | Yes |

**Request Body:**

```typescript
interface CreatePaymentIntentRequest {
  auctionId: string;
  amount: number;
}
```

**Response Body:**

```typescript
interface PaymentIntent {
  id: string;
  clientSecret: string;
  amount: number;
  currency: string;
  status: string;
}
```

**Example Request:**

```json
POST /payments/create-payment-intent
Authorization: Bearer <token>
Content-Type: application/json

{
  "auctionId": "lst_001",
  "amount": 475.00
}
```

**Example Response:**

```json
{
  "id": "pi_abc123",
  "clientSecret": "pi_abc123_secret_xyz",
  "amount": 475.00,
  "currency": "USD",
  "status": "requires_payment_method"
}
```

---

### POST /payments/confirm-payment

Confirm a payment after client-side payment method collection.

| Property | Value |
|----------|-------|
| **Auth Required** | Yes |

**Request Body:**

```typescript
interface ConfirmPaymentRequest {
  paymentIntentId: string;
}
```

**Response Body:** None (204 No Content)

**Example Request:**

```json
POST /payments/confirm-payment
Authorization: Bearer <token>
Content-Type: application/json

{
  "paymentIntentId": "pi_abc123"
}
```

---

### GET /payments/history

Get the authenticated user's payment history (paginated).

| Property | Value |
|----------|-------|
| **Auth Required** | Yes |

**Query Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | number | 1 | Page number |
| `pageSize` | number | 20 | Items per page |

**Response Body:**

```typescript
{
  data: PaymentRecord[];
  total: number;
}

interface PaymentRecord {
  id: string;
  auctionId: string;
  amount: number;
  currency: string;
  status: string;
  createdAt: string;  // ISO 8601
}
```

**Example Request:**

```
GET /payments/history?page=1&pageSize=20
Authorization: Bearer <token>
```

**Example Response:**

```json
{
  "data": [
    {
      "id": "pay_001",
      "auctionId": "lst_099",
      "amount": 1200.00,
      "currency": "USD",
      "status": "succeeded",
      "createdAt": "2024-03-11T10:00:00Z"
    }
  ],
  "total": 3
}
```

---

## Admin Endpoints

All admin endpoints require authentication with the `admin` role.

### GET /admin/users

Get paginated list of all users.

| Property | Value |
|----------|-------|
| **Auth Required** | Yes (Admin role) |

**Query Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | number | 1 | Page number |
| `pageSize` | number | 20 | Items per page |

**Response Body:**

```typescript
interface PaginatedResponse<AdminUserRow> {
  data: AdminUserRow[];
  total: number;
  page: number;
  pageSize: number;
  hasMore: boolean;
}

interface AdminUserRow {
  id: string;
  displayName: string;
  email: string;
  role: 'buyer' | 'seller' | 'admin';
  registeredAt: string;  // ISO 8601
  status: AccountStatus;
}

type AccountStatus = 'active' | 'suspended' | 'deleted';
```

**Example Request:**

```
GET /admin/users?page=1&pageSize=20
Authorization: Bearer <admin-token>
```

**Example Response:**

```json
{
  "data": [
    {
      "id": "usr_abc123",
      "displayName": "Jane Doe",
      "email": "jane@example.com",
      "role": "buyer",
      "registeredAt": "2024-01-15T10:30:00Z",
      "status": "active"
    }
  ],
  "total": 150,
  "page": 1,
  "pageSize": 20,
  "hasMore": true
}
```

---

### PUT /admin/users/:userId/suspend

Suspend a user account.

| Property | Value |
|----------|-------|
| **Auth Required** | Yes (Admin role) |

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `userId` | string | User ID to suspend |

**Request Body:** None

**Response Body:** None (204 No Content)

---

### PUT /admin/users/:userId/activate

Reactivate a suspended user account.

| Property | Value |
|----------|-------|
| **Auth Required** | Yes (Admin role) |

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `userId` | string | User ID to activate |

**Request Body:** None

**Response Body:** None (204 No Content)

---

### DELETE /admin/users/:userId

Delete a user account.

| Property | Value |
|----------|-------|
| **Auth Required** | Yes (Admin role) |

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `userId` | string | User ID to delete |

**Request Body:** None

**Response Body:** None (204 No Content)

---

### GET /admin/listings

Get paginated list of all listings for admin management.

| Property | Value |
|----------|-------|
| **Auth Required** | Yes (Admin role) |

**Query Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | number | 1 | Page number |
| `pageSize` | number | 20 | Items per page |

**Response Body:**

```typescript
interface PaginatedResponse<AdminListingRow> {
  data: AdminListingRow[];
  total: number;
  page: number;
  pageSize: number;
  hasMore: boolean;
}

interface AdminListingRow {
  id: string;
  title: string;
  sellerDisplayName: string;
  status: ListingStatus;    // 'active' | 'ended' | 'flagged' | 'removed'
  currentBid: number;
  reportCount: number;
}
```

**Example Request:**

```
GET /admin/listings?page=1&pageSize=20
Authorization: Bearer <admin-token>
```

**Example Response:**

```json
{
  "data": [
    {
      "id": "lst_001",
      "title": "Vintage Oak Dining Table",
      "sellerDisplayName": "Antique Treasures",
      "status": "active",
      "currentBid": 475.00,
      "reportCount": 0
    },
    {
      "id": "lst_005",
      "title": "Suspicious Listing",
      "sellerDisplayName": "New Seller",
      "status": "flagged",
      "currentBid": 50.00,
      "reportCount": 3
    }
  ],
  "total": 200,
  "page": 1,
  "pageSize": 20,
  "hasMore": true
}
```

---

### DELETE /admin/listings/:listingId

Remove a listing (admin action).

| Property | Value |
|----------|-------|
| **Auth Required** | Yes (Admin role) |

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `listingId` | string | Listing ID to remove |

**Request Body:** None

**Response Body:** None (204 No Content)

---

### PUT /admin/listings/:listingId/flag

Flag a listing (admin action).

| Property | Value |
|----------|-------|
| **Auth Required** | Yes (Admin role) |

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `listingId` | string | Listing ID to flag |

**Request Body:** None

**Response Body:** None (204 No Content)

---

### GET /admin/listings/reports

Get reported listings with report details (paginated).

| Property | Value |
|----------|-------|
| **Auth Required** | Yes (Admin role) |

**Query Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | number | 1 | Page number |
| `pageSize` | number | 20 | Items per page |

**Response Body:**

```typescript
interface PaginatedResponse<ListingReport> {
  data: ListingReport[];
  total: number;
  page: number;
  pageSize: number;
  hasMore: boolean;
}

interface ListingReport {
  id: string;
  listingId: string;
  reason: string;
  reporterDisplayName: string;
  reportDate: string;  // ISO 8601
}
```

**Example Request:**

```
GET /admin/listings/reports?page=1&pageSize=20
Authorization: Bearer <admin-token>
```

**Example Response:**

```json
{
  "data": [
    {
      "id": "rpt_001",
      "listingId": "lst_005",
      "reason": "Misleading product photos",
      "reporterDisplayName": "Jane Doe",
      "reportDate": "2024-03-14T09:00:00Z"
    }
  ],
  "total": 5,
  "page": 1,
  "pageSize": 20,
  "hasMore": false
}
```

---

### GET /admin/analytics/summary

Get platform analytics summary for a date range.

| Property | Value |
|----------|-------|
| **Auth Required** | Yes (Admin role) |

**Query Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `startDate` | string | Start date (ISO 8601 date) |
| `endDate` | string | End date (ISO 8601 date) |

**Response Body:**

```typescript
interface AnalyticsSummary {
  totalUsers: number;
  activeAuctions: number;
  completedAuctions: number;
  totalRevenue: number;
}
```

**Example Request:**

```
GET /admin/analytics/summary?startDate=2024-02-15&endDate=2024-03-15
Authorization: Bearer <admin-token>
```

**Example Response:**

```json
{
  "totalUsers": 1250,
  "activeAuctions": 87,
  "completedAuctions": 342,
  "totalRevenue": 156750.00
}
```

---

### GET /admin/analytics/auction-trends

Get auction creation/completion trends over a date range.

| Property | Value |
|----------|-------|
| **Auth Required** | Yes (Admin role) |

**Query Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `startDate` | string | Start date (ISO 8601 date) |
| `endDate` | string | End date (ISO 8601 date) |

**Response Body:**

```typescript
type AuctionTrend[] = Array<{
  date: string;              // ISO 8601 date
  auctionsCreated: number;
  auctionsCompleted: number;
}>
```

**Example Request:**

```
GET /admin/analytics/auction-trends?startDate=2024-03-01&endDate=2024-03-15
Authorization: Bearer <admin-token>
```

**Example Response:**

```json
[
  { "date": "2024-03-01", "auctionsCreated": 12, "auctionsCompleted": 8 },
  { "date": "2024-03-02", "auctionsCreated": 15, "auctionsCompleted": 10 },
  { "date": "2024-03-03", "auctionsCreated": 9, "auctionsCompleted": 11 }
]
```

---

### GET /admin/analytics/category-distribution

Get listing count per furniture category for a date range.

| Property | Value |
|----------|-------|
| **Auth Required** | Yes (Admin role) |

**Query Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `startDate` | string | Start date (ISO 8601 date) |
| `endDate` | string | End date (ISO 8601 date) |

**Response Body:**

```typescript
type CategoryDistribution[] = Array<{
  category: FurnitureCategory;
  count: number;
}>
```

**Example Request:**

```
GET /admin/analytics/category-distribution?startDate=2024-02-15&endDate=2024-03-15
Authorization: Bearer <admin-token>
```

**Example Response:**

```json
[
  { "category": "sofa", "count": 45 },
  { "category": "dining-table", "count": 32 },
  { "category": "office-chair", "count": 28 },
  { "category": "wardrobe", "count": 18 },
  { "category": "bed-frame", "count": 22 },
  { "category": "coffee-table", "count": 35 },
  { "category": "cabinet", "count": 15 },
  { "category": "bookshelf", "count": 12 }
]
```

---

### GET /admin/analytics/top-sellers

Get top 10 sellers by completed auctions and revenue for a date range.

| Property | Value |
|----------|-------|
| **Auth Required** | Yes (Admin role) |

**Query Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `startDate` | string | Start date (ISO 8601 date) |
| `endDate` | string | End date (ISO 8601 date) |

**Response Body:**

```typescript
type TopSeller[] = Array<{
  displayName: string;
  completedAuctions: number;
  totalRevenue: number;
}>
```

**Example Request:**

```
GET /admin/analytics/top-sellers?startDate=2024-02-15&endDate=2024-03-15
Authorization: Bearer <admin-token>
```

**Example Response:**

```json
[
  { "displayName": "Antique Treasures", "completedAuctions": 24, "totalRevenue": 18500.00 },
  { "displayName": "Modern Finds", "completedAuctions": 18, "totalRevenue": 12300.00 },
  { "displayName": "Vintage Vault", "completedAuctions": 15, "totalRevenue": 9800.00 }
]
```

---

## WebSocket Events

The application uses Socket.IO for real-time communication. Connection is established at the URL configured via `VITE_WS_URL` with JWT authentication.

**Connection:**

```typescript
const socket = io(VITE_WS_URL, {
  auth: { token: '<jwt-token>' },
  reconnection: true,
  reconnectionDelay: 1000,
  reconnectionDelayMax: 30000,
  reconnectionAttempts: 10,
});
```

### Server → Client Events

| Event Name | Payload | Description |
|------------|---------|-------------|
| `bid:update` | `BidUpdateEvent` | New bid placed on a watched auction |
| `outbid` | `OutbidPayload` | Current user has been outbid |
| `auction:ending` | `AuctionEndingPayload` | Auction ending soon (15 min warning) |
| `auction:won` | `AuctionEndEvent` | User won an auction |
| `auction:lost` | `AuctionEndEvent` | User lost an auction |
| `notification` | `Notification` | General notification for the user |

#### bid:update

Emitted when a new bid is placed on an auction the client has joined.

```typescript
interface BidUpdateEvent {
  auctionId: string;
  currentBid: number;
  bidCount: number;
  bidderAlias: string;
  timestamp: string;  // ISO 8601
}
```

**Example payload:**

```json
{
  "auctionId": "lst_001",
  "currentBid": 480.00,
  "bidCount": 13,
  "bidderAlias": "Bidder #4",
  "timestamp": "2024-03-15T15:05:00Z"
}
```

#### outbid

Emitted when the authenticated user has been outbid on an auction they bid on.

```typescript
interface OutbidPayload {
  auctionId: string;
  currentBid: number;
}
```

**Example payload:**

```json
{
  "auctionId": "lst_001",
  "currentBid": 480.00
}
```

#### auction:ending

Emitted when an auction the user is watching/bidding on is about to end.

```typescript
interface AuctionEndingPayload {
  auctionId: string;
  minutesRemaining: number;
}
```

**Example payload:**

```json
{
  "auctionId": "lst_001",
  "minutesRemaining": 15
}
```

#### auction:won

Emitted when the user wins an auction.

```typescript
interface AuctionEndEvent {
  auctionId: string;
  result: 'won';
  winningBid?: number;
  winnerId?: string;
}
```

**Example payload:**

```json
{
  "auctionId": "lst_001",
  "result": "won",
  "winningBid": 475.00,
  "winnerId": "usr_abc123"
}
```

#### auction:lost

Emitted when the user loses an auction they bid on.

```typescript
interface AuctionEndEvent {
  auctionId: string;
  result: 'lost' | 'reserve-not-met';
  winningBid?: number;
  winnerId?: string;
}
```

**Example payload:**

```json
{
  "auctionId": "lst_002",
  "result": "lost",
  "winningBid": 1200.00,
  "winnerId": "usr_other"
}
```

#### notification

Emitted for general notifications (auto-bid placed, limit reached, etc.).

```typescript
interface Notification {
  id: string;
  type: NotificationType;
  title: string;
  message: string;
  auctionId: string;
  isRead: boolean;
  createdAt: string;
}

type NotificationType = 'outbid' | 'auction-ending' | 'auction-won'
  | 'auction-lost' | 'auto-bid-placed' | 'auto-bid-limit-reached';
```

**Example payload:**

```json
{
  "id": "ntf_003",
  "type": "auto-bid-placed",
  "title": "Auto-bid placed",
  "message": "Your auto-bid placed $485.00 on Vintage Oak Dining Table",
  "auctionId": "lst_001",
  "isRead": false,
  "createdAt": "2024-03-15T15:05:01Z"
}
```

### Client → Server Events

| Event Name | Payload | Description |
|------------|---------|-------------|
| `join:auction` | `{ auctionId: string }` | Subscribe to bid updates for a specific auction |
| `leave:auction` | `{ auctionId: string }` | Unsubscribe from bid updates for a specific auction |
| `subscribe:notifications` | `{ userId: string }` | Subscribe to user-specific notifications |

#### join:auction

Emitted when the user navigates to a listing detail page. After joining, the client will receive `bid:update` events for that auction.

```typescript
socket.emit('join:auction', { auctionId: 'lst_001' });
```

#### leave:auction

Emitted when the user navigates away from a listing detail page.

```typescript
socket.emit('leave:auction', { auctionId: 'lst_001' });
```

#### subscribe:notifications

Emitted after authentication to receive real-time notifications.

```typescript
socket.emit('subscribe:notifications', { userId: 'usr_abc123' });
```

---

## Error Response Format

All API services return errors in a consistent format:

```typescript
interface ApiError {
  statusCode: number;
  errorCode: string;
  message: string;
  fieldErrors?: Record<string, string>;
}
```

| Field | Type | Description |
|-------|------|-------------|
| `statusCode` | number | HTTP status code (400, 401, 403, 404, 409, 422, 429, 500) |
| `errorCode` | string | Machine-readable error identifier (e.g., `INVALID_CREDENTIALS`, `BID_TOO_LOW`) |
| `message` | string | Human-readable error message |
| `fieldErrors` | object | Optional field-level validation errors (key = field name, value = error message) |

### Common Error Codes

| Status | Error Code | Description |
|--------|------------|-------------|
| 400 | `INVALID_REQUEST` | Request body failed validation |
| 401 | `UNAUTHORIZED` | Missing or invalid authentication token |
| 401 | `TOKEN_EXPIRED` | JWT token has expired |
| 401 | `INVALID_CREDENTIALS` | Wrong email or password |
| 403 | `FORBIDDEN` | User lacks required role/permission |
| 404 | `NOT_FOUND` | Requested resource does not exist |
| 409 | `CONFLICT` | Resource conflict (e.g., email already registered) |
| 422 | `BID_TOO_LOW` | Bid amount is below the minimum next bid |
| 422 | `AUCTION_ENDED` | Attempting to bid on an ended auction |
| 429 | `RATE_LIMITED` | Too many requests, try again later |
| 500 | `INTERNAL_ERROR` | Unexpected server error |

### Example Error Responses

**Validation error (400):**

```json
{
  "statusCode": 400,
  "errorCode": "INVALID_REQUEST",
  "message": "Request validation failed",
  "fieldErrors": {
    "email": "Must be a valid email address",
    "password": "Must be at least 8 characters"
  }
}
```

**Authentication error (401):**

```json
{
  "statusCode": 401,
  "errorCode": "INVALID_CREDENTIALS",
  "message": "Invalid email or password"
}
```

**Bid rejection (422):**

```json
{
  "statusCode": 422,
  "errorCode": "BID_TOO_LOW",
  "message": "Bid amount must be at least $480.00"
}
```

**Rate limiting (429):**

```json
{
  "statusCode": 429,
  "errorCode": "RATE_LIMITED",
  "message": "Rate limit exceeded. Please try again in 60 seconds."
}
```

### Client-Side Error Handling

The front-end Axios interceptor handles errors as follows:

| Status Code | Client Behavior |
|-------------|----------------|
| 401 | Attempt token refresh; if failed, clear stores and redirect to login |
| 403 | Redirect to catalog page |
| 429 | Display "Rate limit exceeded" warning toast |
| 5xx | Display "Server error" error toast |
| Network error | Display "Network error" error toast |
