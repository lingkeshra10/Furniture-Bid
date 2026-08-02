# Furniture Bid

A full-stack real-time furniture auction platform where buyers browse and place live bids, sellers create and manage listings, and admins oversee the entire marketplace. The backend is a Spring Boot mock API with an in-memory data store, making it ideal for frontend development and rapid prototyping without needing a database.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Features](#features)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [API Reference](#api-reference)
- [WebSocket Events](#websocket-events)
- [User Roles](#user-roles)

---

## Overview

Furniture Bid simulates a real-world online auction experience. Users register as buyers or sellers. Sellers list furniture items with a starting price, optional reserve price, and auction end time. Buyers browse the catalog, watch listings, and place competitive bids in real time. When a bid is placed, all connected clients watching that auction receive an instant update via WebSocket, and the outbid user gets a personal notification.

The backend is intentionally stateless and persistence-free — all data lives in ConcurrentHashMaps — making the server fast to boot and easy to reset for development.

---

## Tech Stack

### Frontend
| Technology | Version | Purpose |
|---|---|---|
| Vue 3 | 3.5 | UI framework (Composition API) |
| TypeScript | 6.0 | Type safety |
| Vite | 8.0 | Build tool and dev server |
| Vue Router | 4.6 | Client-side routing with role guards |
| Pinia | 3.0 | State management (singleton composables) |
| Axios | 1.17 | HTTP client with JWT interceptors |
| Socket.IO Client | 4.8 | Real-time WebSocket communication |
| TailwindCSS | 3.4 | Utility-first styling |
| Chart.js + vue-chartjs | 4.5 / 5.3 | Admin analytics charts |
| Zod | 4.4 | Schema validation |
| Vitest + Vue Test Utils | 4.1 | Unit testing |

### Backend
| Technology | Version | Purpose |
|---|---|---|
| Spring Boot | 4.1 | Application framework |
| Java | 17 | Language |
| Maven | — | Build and dependency management |
| jjwt | 0.12.5 | JWT token generation and validation |
| netty-socketio | 2.0.9 | Socket.IO server for real-time events |
| Lombok | 1.18 | Boilerplate reduction |
| Bean Validation | — | Request validation |
| jqwik | 1.8 | Property-based testing |

---

## Features

### Authentication
- Email/password registration with validation (length, complexity rules)
- JWT-based login with token stored in `localStorage`
- Token refresh endpoint
- Social login support (Google / Facebook — mock provider)
- Password reset endpoint (returns 204, no-op in mock)
- Session restore on app load by validating the persisted token

### Furniture Catalog
- Browse all active listings with filtering by category, condition, price range, and location
- Sort by ending soonest, newest, price low/high, or most bids
- Infinite scroll pagination
- Detailed listing view with dimensions, images, bid history, and auction countdown
- Flag a listing for review

### Bidding
- Place a bid (minimum increment of $5.00 above the current highest bid)
- Real-time bid updates pushed to all watchers of an auction via WebSocket
- Outbid notification sent instantly to the previously highest bidder
- Full paginated bid history per auction
- Auto-bid: set a maximum amount and the system bids on your behalf up to that cap

### Watchlist
- Add or remove listings from a personal watchlist
- View your watchlist as a paginated, filterable list

### Seller Dashboard
- Create new furniture listings (multipart form with image uploads)
- View active listings with current bid, bid count, and time remaining
- View completed auctions with final price, winner name, and reserve status

### Notifications
- In-app notification feed (paginated)
- Mark individual or all notifications as read
- Real-time delivery via WebSocket for: outbid, auction ending soon, auction won, auction lost

### Payments
- Create a payment intent for a won auction
- Confirm payment (Stripe-style flow, simulated in mock)
- View paginated payment history

### Admin Dashboard
- User management: list, suspend, activate, and delete users
- Listing management: list, flag, and remove listings
- Listing reports: view flagged/reported listings
- Analytics: summary metrics, auction trends over time, category distribution, and top sellers by revenue (all date-range filtered)

---

## Project Structure

```
Furniture-Bid/
├── Back-End/                          # Spring Boot mock API
│   └── src/main/java/com/furniturebid/mockapi/
│       ├── config/                    # CORS, Security, Socket.IO config
│       ├── controller/                # REST controllers (7 controllers)
│       │   ├── IdentityController     # /api/auth/*
│       │   ├── FurnitureController    # /api/furniture/*
│       │   ├── AuctionController      # /api/auctions/*, /api/seller/*
│       │   ├── UserProfileController  # /api/users/*
│       │   ├── NotificationController # /api/notifications/*
│       │   ├── PaymentController      # /api/payments/*
│       │   └── AdminController        # /api/admin/*
│       ├── service/                   # Business logic layer
│       ├── dto/
│       │   ├── request/               # Inbound request DTOs
│       │   └── response/              # Outbound response DTOs
│       ├── entity/                    # In-memory data model classes
│       ├── store/                     # MockDataStore (in-memory ConcurrentHashMaps)
│       ├── websocket/                 # SocketIOHandler (real-time events)
│       ├── security/                  # JWT filter, JwtUtility, AuthenticatedUser
│       └── exception/                 # Custom exceptions + global handler
│
└── Front-End/                         # Vue 3 + TypeScript SPA
    └── src/
        ├── pages/                     # Route-level page components
        │   ├── LoginPage.vue
        │   ├── RegisterPage.vue
        │   ├── CatalogPage.vue
        │   ├── ListingDetailPage.vue
        │   ├── WatchlistPage.vue
        │   ├── BiddingHistoryPage.vue
        │   ├── UserProfilePage.vue
        │   ├── SellerDashboardPage.vue
        │   ├── CreateListingPage.vue
        │   └── AdminDashboardPage.vue
        ├── components/                # Reusable UI components
        ├── stores/                    # Pinia stores (singleton composables)
        │   ├── auth.ts
        │   ├── furniture.ts
        │   ├── auction.ts
        │   ├── notification.ts
        │   └── watchlist.ts
        ├── services/
        │   ├── api/                   # Axios service modules per domain
        │   └── websocket/             # Socket.IO client (socketClient.ts)
        ├── router/                    # Vue Router + auth/role guards
        ├── types/                     # TypeScript type definitions
        └── utils/                     # Constants, helpers
```

---

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- Node.js 20+ and npm

### Running the Backend

```bash
cd Back-End
mvn spring-boot:run
```

The API server starts on `http://localhost:8080`.
The Socket.IO server starts on port `9092` (configurable in `application.properties`).

### Running the Frontend

```bash
cd Front-End
npm install
npm run dev
```

The dev server starts on `http://localhost:5173`.

### Running Tests

```bash
# Backend
cd Back-End
mvn test

# Frontend
cd Front-End
npm test
```

---

## API Reference

All protected endpoints require a `Bearer <token>` header. Roles: `buyer`, `seller`, `admin`.

### Auth — `/api/auth`
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/login` | Public | Email + password login |
| POST | `/register` | Public | Create a new account |
| POST | `/reset-password` | Public | Trigger password reset (no-op) |
| POST | `/refresh-token` | Any role | Issue a new JWT |
| POST | `/social-login` | Public | OAuth login (google / facebook) |

### Furniture — `/api/furniture`
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/` | Public | Browse catalog (filter, sort, paginate) |
| GET | `/{id}` | Public | Get listing detail |
| POST | `/` | Seller / Admin | Create listing (multipart) |
| PUT | `/{id}/flag` | Any role | Flag listing for review |
| DELETE | `/{id}` | Owner / Admin | Delete listing |

### Auctions — `/api/auctions`
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/bids` | Any role | Place a bid |
| GET | `/{auctionId}/bids` | Public | Get bid history |
| POST | `/{auctionId}/auto-bid` | Any role | Enable auto-bid |
| DELETE | `/{auctionId}/auto-bid` | Any role | Disable auto-bid |

### Seller — `/api/seller`
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/active-listings` | Seller / Admin | Active listings with stats |
| GET | `/completed-auctions` | Seller / Admin | Completed auctions with winner |

### Users — `/api/users`
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/profile` | Any role | Get own profile |
| PUT | `/profile` | Any role | Update display name / avatar |
| GET | `/watchlist` | Any role | Get paginated watchlist |
| POST | `/watchlist/{listingId}` | Any role | Add to watchlist |
| DELETE | `/watchlist/{listingId}` | Any role | Remove from watchlist |

### Notifications — `/api/notifications`
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/` | Any role | Get paginated notifications |
| PUT | `/{id}/read` | Any role | Mark one as read |
| PUT | `/read-all` | Any role | Mark all as read |

### Payments — `/api/payments`
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/create-payment-intent` | Any role | Create payment intent |
| POST | `/confirm-payment` | Any role | Confirm payment |
| GET | `/history` | Any role | Paginated payment history |

### Admin — `/api/admin`
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/users` | Admin | List all users |
| PUT | `/users/{id}/suspend` | Admin | Suspend user |
| PUT | `/users/{id}/activate` | Admin | Activate user |
| DELETE | `/users/{id}` | Admin | Delete user |
| GET | `/listings` | Admin | List all listings |
| DELETE | `/listings/{id}` | Admin | Remove listing |
| PUT | `/listings/{id}/flag` | Admin | Flag listing |
| GET | `/listings/reports` | Admin | View listing reports |
| GET | `/analytics/summary` | Admin | Analytics summary |
| GET | `/analytics/auction-trends` | Admin | Auction trends over time |
| GET | `/analytics/category-distribution` | Admin | Distribution by category |
| GET | `/analytics/top-sellers` | Admin | Top sellers by revenue |

---

## WebSocket Events

The backend runs a Socket.IO server. Clients authenticate by passing the JWT as a URL parameter on connect: `?token=<jwt>`.

### Client → Server

| Event | Payload | Description |
|---|---|---|
| `join:auction` | `auctionId: string` | Subscribe to live bid updates for an auction |
| `leave:auction` | `auctionId: string` | Unsubscribe from an auction room |
| `subscribe:notifications` | `userId: string` | Subscribe to personal notifications |

### Server → Client

| Event | Payload | Description |
|---|---|---|
| `bid:update` | `{ auctionId, currentBid, bidCount, bidderAlias, timestamp }` | New bid placed on a watched auction |
| `outbid` | `{ auctionId, currentBid }` | You were outbid on an auction |
| `notification` | `{ ... }` | General in-app notification |
| `auction:ending` | `{ auctionId, minutesRemaining }` | Auction closing soon warning |
| `auction:won` | `{ ... }` | You won an auction |
| `auction:lost` | `{ ... }` | Auction ended, you did not win |
| `error` | `{ message }` | Auth error — client is disconnected |

---

## User Roles

| Role | Capabilities |
|---|---|
| `buyer` | Browse catalog, place bids, watchlist, notifications, payment history |
| `seller` | Everything a buyer can do + create listings, seller dashboard |
| `admin` | Everything a seller can do + admin dashboard, user/listing management, analytics |
