# Furniture Bid - Mock API Service

A Spring Boot application that provides all REST API endpoints and WebSocket events consumed by the Furniture Bid front-end. Uses in-memory data structures with pre-seeded realistic data for development and testing — no external database or services required.

## Prerequisites

- Java 17 or later
- Maven 3.6 or later

## Getting Started

### Build

```bash
mvn clean package
```

### Run

```bash
mvn spring-boot:run
```

- REST API available at: **http://localhost:8080**
- WebSocket (Socket.IO) available at: **http://localhost:9092**

## Mock User Credentials

The service starts with pre-seeded users. Use these credentials to log in:

| Role   | Email              | Password    |
|--------|--------------------|-------------|
| Buyer  | buyer@example.com  | SecurePass1 |
| Seller | seller@example.com | SecurePass1 |
| Admin  | admin@example.com  | SecurePass1 |

## Available REST Endpoints

All endpoints are served under the `/api` base path. CORS is enabled for all origins.

### Auth (`/api/auth`)

| Method | Endpoint                    | Auth Required | Description            |
|--------|-----------------------------|---------------|------------------------|
| POST   | `/api/auth/login`           | No            | Login with credentials |
| POST   | `/api/auth/register`        | No            | Register a new user    |
| POST   | `/api/auth/reset-password`  | No            | Request password reset |
| POST   | `/api/auth/refresh-token`   | Yes (expired OK) | Refresh JWT token   |
| POST   | `/api/auth/social-login`    | No            | Login via Google/Facebook |

### Users (`/api/users`)

| Method | Endpoint                         | Auth Required | Description                |
|--------|----------------------------------|---------------|----------------------------|
| GET    | `/api/users/profile`             | Yes           | Get authenticated user profile |
| PUT    | `/api/users/profile`             | Yes           | Update display name/avatar |
| GET    | `/api/users/watchlist`           | Yes           | Get watchlist (paginated)  |
| POST   | `/api/users/watchlist/{listingId}` | Yes         | Add listing to watchlist   |
| DELETE | `/api/users/watchlist/{listingId}` | Yes         | Remove from watchlist      |

### Auctions (`/api/auctions`)

| Method | Endpoint                              | Auth Required | Description              |
|--------|---------------------------------------|---------------|--------------------------|
| POST   | `/api/auctions/bids`                  | Yes           | Place a bid              |
| GET    | `/api/auctions/{auctionId}/bids`      | No            | Get bid history (paginated) |
| POST   | `/api/auctions/{auctionId}/auto-bid`  | Yes           | Set auto-bid             |
| DELETE | `/api/auctions/{auctionId}/auto-bid`  | Yes           | Remove auto-bid          |

### Seller (`/api/seller`)

| Method | Endpoint                          | Auth Required | Description                    |
|--------|-----------------------------------|---------------|--------------------------------|
| GET    | `/api/seller/active-listings`     | Yes (seller/admin) | Get seller's active listings |
| GET    | `/api/seller/completed-auctions`  | Yes (seller/admin) | Get seller's completed auctions |

### Furniture (`/api/furniture`)

| Method | Endpoint                    | Auth Required | Description                  |
|--------|-----------------------------|---------------|------------------------------|
| GET    | `/api/furniture`            | No            | Browse catalog (filtered, sorted, paginated) |
| GET    | `/api/furniture/{id}`       | No            | Get listing details          |
| POST   | `/api/furniture`            | Yes (seller/admin) | Create new listing (multipart) |
| PUT    | `/api/furniture/{id}/flag`  | Yes           | Flag a listing               |
| DELETE | `/api/furniture/{id}`       | Yes (owner/admin) | Remove a listing           |

### Notifications (`/api/notifications`)

| Method | Endpoint                           | Auth Required | Description             |
|--------|------------------------------------|---------------|-------------------------|
| GET    | `/api/notifications`               | Yes           | Get notifications (paginated) |
| PUT    | `/api/notifications/{id}/read`     | Yes           | Mark notification as read |
| PUT    | `/api/notifications/read-all`      | Yes           | Mark all as read         |

### Payments (`/api/payments`)

| Method | Endpoint                              | Auth Required | Description              |
|--------|---------------------------------------|---------------|--------------------------|
| POST   | `/api/payments/create-payment-intent` | Yes           | Create a payment intent  |
| POST   | `/api/payments/confirm-payment`       | Yes           | Confirm a payment        |
| GET    | `/api/payments/history`               | Yes           | Get payment history (paginated) |

### Admin (`/api/admin`)

All admin endpoints require a valid token with `admin` role.

| Method | Endpoint                                       | Description                    |
|--------|------------------------------------------------|--------------------------------|
| GET    | `/api/admin/users`                             | List all users (paginated)     |
| PUT    | `/api/admin/users/{userId}/suspend`            | Suspend a user                 |
| PUT    | `/api/admin/users/{userId}/activate`           | Activate a user                |
| DELETE | `/api/admin/users/{userId}`                    | Delete a user                  |
| GET    | `/api/admin/listings`                          | List all listings (paginated)  |
| DELETE | `/api/admin/listings/{listingId}`              | Remove a listing               |
| PUT    | `/api/admin/listings/{listingId}/flag`         | Flag a listing                 |
| GET    | `/api/admin/listings/reports`                  | Get listing reports (paginated)|
| GET    | `/api/admin/analytics/summary`                 | Analytics summary              |
| GET    | `/api/admin/analytics/auction-trends`          | Auction trends by day          |
| GET    | `/api/admin/analytics/category-distribution`   | Category distribution          |
| GET    | `/api/admin/analytics/top-sellers`             | Top sellers by revenue         |

## WebSocket Events

The service uses Socket.IO (via netty-socketio) on port 9092. Connect with a JWT token in the `auth.token` handshake field.

### Client → Server Events

| Event                    | Payload          | Description                        |
|--------------------------|------------------|------------------------------------|
| `join:auction`           | `auctionId`      | Subscribe to bid updates for an auction |
| `leave:auction`          | `auctionId`      | Unsubscribe from auction updates   |
| `subscribe:notifications`| `userId`         | Subscribe to real-time notifications |

### Server → Client Events

| Event               | Payload                                                       | Description                          |
|---------------------|---------------------------------------------------------------|--------------------------------------|
| `bid:update`        | `{ auctionId, currentBid, bidCount, bidderAlias, timestamp }` | New bid placed on a subscribed auction |
| `outbid`            | `{ auctionId, currentBid }`                                   | You have been outbid                 |
| `auction:ending`    | `{ auctionId, minutesRemaining }`                             | Auction ending soon (15 min warning) |
| `auction:won`       | `{ auctionId, result, winningBid, winnerId }`                 | You won the auction                  |
| `auction:lost`      | `{ auctionId, result, winningBid, winnerId }`                 | Auction ended, you did not win       |
| `notification`      | `{ id, type, title, message, auctionId, isRead, createdAt }`  | New notification                     |

## Testing

Run all tests including property-based tests:

```bash
mvn test
```

The test suite includes:
- Unit tests for services and utilities
- Integration tests for controllers and WebSocket
- Property-based tests (via jqwik) for correctness properties

## Notes

- All data is stored in-memory. Restarting the service resets data to the seeded state.
- JWT tokens use a hardcoded signing key — not suitable for production use.
- CORS is fully open (all origins, methods, and headers) for local development convenience.
- Pagination defaults: page=1, pageSize=20. Maximum pageSize is 100.
- Minimum bid increment is 5.00 above the current highest bid.
