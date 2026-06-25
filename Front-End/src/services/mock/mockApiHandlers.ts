/**
 * Mock API request handlers that simulate backend responses.
 *
 * Each handler mirrors the contract of the corresponding real API service.
 * Includes simulated network delays (200-500ms) and pagination support.
 *
 * Requirement: 14.4
 */

import type { LoginResponse, User } from '@/types/auth';
import type { FurnitureListing, FurnitureListingSummary, CatalogFilters, CatalogSortOption } from '@/types/furniture';
import type { Bid, PlaceBidResponse, SellerActiveListing, SellerCompletedAuction } from '@/types/auction';
import type { Notification } from '@/types/notification';
import type { PaginatedResponse } from '@/types/common';
import type { AdminUserRow, AdminListingRow } from '@/types/user';
import {
  mockUsers,
  mockListings,
  mockBidsByListing,
  mockNotifications,
  mockWatchlist,
  currentMockUser,
  setCurrentMockUser,
  listingToSummary,
  generateId,
  getSellerActiveListings,
  getSellerCompletedAuctions,
  getAdminUserRows,
  getAdminListingRows,
} from './mockData';

// ─── Helpers ────────────────────────────────────────────────────────────────

interface MockResponse<T = unknown> {
  data: T;
  status: number;
}

function delay(ms?: number): Promise<void> {
  const wait = ms ?? (200 + Math.random() * 300);
  return new Promise((resolve) => setTimeout(resolve, wait));
}

function paginate<T>(items: T[], page: number, pageSize: number): PaginatedResponse<T> {
  const start = (page - 1) * pageSize;
  const sliced = items.slice(start, start + pageSize);
  return {
    data: sliced,
    total: items.length,
    page,
    pageSize,
    hasMore: start + pageSize < items.length,
  };
}

function ok<T>(data: T): MockResponse<T> {
  return { data, status: 200 };
}

function notFound(message = 'Not found'): MockResponse {
  return { data: { statusCode: 404, errorCode: 'NOT_FOUND', message }, status: 404 };
}

// ─── Route Matching ─────────────────────────────────────────────────────────

type RouteHandler = (params: RouteParams) => Promise<MockResponse>;

interface RouteParams {
  pathParams: Record<string, string>;
  body?: unknown;
  query?: Record<string, unknown>;
}

interface Route {
  method: string;
  pattern: RegExp;
  paramNames: string[];
  handler: RouteHandler;
}

const routes: Route[] = [];

function route(method: string, path: string, handler: RouteHandler): void {
  // Convert path pattern like '/auth/login' or '/furniture/:id' to regex
  const paramNames: string[] = [];
  const regexStr = path.replace(/:([^/]+)/g, (_, name) => {
    paramNames.push(name);
    return '([^/]+)';
  });
  routes.push({ method, pattern: new RegExp(`^${regexStr}$`), paramNames, handler });
}

/**
 * Main entry point for mock request handling.
 * Matches the request against registered routes and invokes the handler.
 */
export async function handleMockRequest(
  method: string,
  url: string,
  body?: unknown,
  query?: Record<string, unknown>,
): Promise<MockResponse> {
  await delay();

  // Strip base URL prefix if present
  const path = url.replace(/^(https?:\/\/[^/]+)?(\/?api)?/, '').replace(/\/$/, '') || '/';

  for (const r of routes) {
    if (r.method !== method) continue;
    const match = path.match(r.pattern);
    if (match) {
      const pathParams: Record<string, string> = {};
      r.paramNames.forEach((name, i) => {
        pathParams[name] = match[i + 1];
      });
      return r.handler({ pathParams, body, query });
    }
  }

  console.warn(`[Mock] Unhandled ${method.toUpperCase()} ${path}`);
  return { data: { statusCode: 404, errorCode: 'NOT_FOUND', message: `No mock handler for ${method.toUpperCase()} ${path}` }, status: 404 };
}

// ─── Auth Handlers ──────────────────────────────────────────────────────────

route('post', '/auth/login', async ({ body }) => {
  const { email } = body as { email: string; password: string };
  const user = mockUsers.find((u) => u.email === email) ?? mockUsers[0];
  setCurrentMockUser(user);
  const response: LoginResponse = { token: 'mock-jwt-token-' + user.id, user };
  return ok(response);
});

route('post', '/auth/register', async ({ body }) => {
  const { email, displayName } = body as { email: string; password: string; displayName: string };
  const newUser: User = {
    id: generateId(),
    email,
    displayName,
    role: 'buyer',
    createdAt: new Date().toISOString(),
  };
  setCurrentMockUser(newUser);
  const response: LoginResponse = { token: 'mock-jwt-token-' + newUser.id, user: newUser };
  return ok(response);
});

route('post', '/auth/reset-password', async () => {
  return ok(undefined);
});

route('post', '/auth/refresh-token', async () => {
  const user = currentMockUser ?? mockUsers[0];
  const response: LoginResponse = { token: 'mock-jwt-token-refreshed-' + user.id, user };
  return ok(response);
});

route('post', '/auth/social-login', async ({ body }) => {
  const { provider } = body as { provider: string; token: string };
  const user = mockUsers.find((u) => u.role === 'buyer') ?? mockUsers[1];
  setCurrentMockUser(user);
  const response: LoginResponse = { token: `mock-jwt-${provider}-` + user.id, user };
  return ok(response);
});

// ─── Furniture / Catalog Handlers ───────────────────────────────────────────

route('get', '/furniture', async ({ query }) => {
  const page = Number(query?.page ?? 1);
  const pageSize = Number(query?.pageSize ?? 20);
  const sort = (query?.sort as CatalogSortOption) ?? 'newest';
  const filters: CatalogFilters = {};

  if (query?.category) {
    filters.category = Array.isArray(query.category) ? query.category : [query.category as string];
  }
  if (query?.condition) {
    filters.condition = Array.isArray(query.condition) ? query.condition : [query.condition as string];
  }
  if (query?.priceMin) filters.priceMin = Number(query.priceMin);
  if (query?.priceMax) filters.priceMax = Number(query.priceMax);
  if (query?.location) filters.location = query.location as string;

  let filtered = [...mockListings].filter((l) => l.status === 'active');

  // Apply filters
  if (filters.category?.length) {
    filtered = filtered.filter((l) => filters.category!.includes(l.category));
  }
  if (filters.condition?.length) {
    filtered = filtered.filter((l) => filters.condition!.includes(l.condition));
  }
  if (filters.priceMin != null) {
    filtered = filtered.filter((l) => l.currentBid >= filters.priceMin!);
  }
  if (filters.priceMax != null) {
    filtered = filtered.filter((l) => l.currentBid <= filters.priceMax!);
  }
  if (filters.location) {
    filtered = filtered.filter((l) => l.location?.toLowerCase().includes(filters.location!.toLowerCase()));
  }

  // Apply sort
  switch (sort) {
    case 'ending-soonest':
      filtered.sort((a, b) => new Date(a.auctionEndDate).getTime() - new Date(b.auctionEndDate).getTime());
      break;
    case 'price-low-high':
      filtered.sort((a, b) => a.currentBid - b.currentBid);
      break;
    case 'price-high-low':
      filtered.sort((a, b) => b.currentBid - a.currentBid);
      break;
    case 'newest':
    default:
      filtered.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
      break;
  }

  const summaries: FurnitureListingSummary[] = filtered.map(listingToSummary);
  return ok(paginate(summaries, page, pageSize));
});

route('get', '/furniture/:id', async ({ pathParams }) => {
  const listing = mockListings.find((l) => l.id === pathParams.id);
  if (!listing) return notFound('Listing not found');
  return ok(listing);
});

route('post', '/furniture', async ({ body }) => {
  const req = body as Record<string, unknown>;
  const newListing: FurnitureListing = {
    id: generateId(),
    title: (req.title as string) ?? 'New Listing',
    description: (req.description as string) ?? '',
    category: (req.category as FurnitureListing['category']) ?? 'sofa',
    condition: (req.condition as FurnitureListing['condition']) ?? 'good',
    brand: req.brand as string | undefined,
    material: req.material as string | undefined,
    dimensions: (req.dimensions as FurnitureListing['dimensions']) ?? { width: 100, height: 80, length: 100 },
    weight: req.weight as number | undefined,
    location: req.location as string | undefined,
    images: ['https://picsum.photos/seed/new/800/600'],
    startingPrice: (req.startingPrice as number) ?? 100,
    reservePrice: (req.reservePrice as number) ?? 200,
    currentBid: (req.startingPrice as number) ?? 100,
    bidCount: 0,
    auctionEndDate: (req.auctionEndDate as string) ?? new Date(Date.now() + 7 * 86_400_000).toISOString(),
    status: 'active',
    sellerId: currentMockUser?.id ?? 'user-1',
    sellerDisplayName: currentMockUser?.displayName ?? 'Mock Seller',
    sellerRating: 4.5,
    createdAt: new Date().toISOString(),
  };
  mockListings.push(newListing);
  return ok(newListing);
});

route('put', '/furniture/:id/flag', async ({ pathParams }) => {
  const listing = mockListings.find((l) => l.id === pathParams.id);
  if (listing) listing.status = 'flagged';
  return ok(undefined);
});

route('delete', '/furniture/:id', async ({ pathParams }) => {
  const listing = mockListings.find((l) => l.id === pathParams.id);
  if (listing) listing.status = 'removed';
  return ok(undefined);
});

// ─── Auction / Bid Handlers ─────────────────────────────────────────────────

route('post', '/auctions/bids', async ({ body }) => {
  const { auctionId, amount } = body as { auctionId: string; amount: number };
  const listing = mockListings.find((l) => l.id === auctionId);

  if (!listing) {
    const resp: PlaceBidResponse = { success: false, error: 'Listing not found' };
    return ok(resp);
  }

  if (amount <= listing.currentBid) {
    const resp: PlaceBidResponse = { success: false, error: 'Bid must be higher than current bid' };
    return ok(resp);
  }

  const newBid: Bid = {
    id: generateId(),
    auctionId,
    bidderId: currentMockUser?.id ?? 'user-2',
    bidderAlias: 'You',
    amount,
    timestamp: new Date().toISOString(),
  };

  listing.currentBid = amount;
  listing.bidCount += 1;

  if (!mockBidsByListing[auctionId]) mockBidsByListing[auctionId] = [];
  mockBidsByListing[auctionId].unshift(newBid);

  const resp: PlaceBidResponse = { success: true, bid: newBid };
  return ok(resp);
});

route('get', '/auctions/:auctionId/bids', async ({ pathParams, query }) => {
  const page = Number(query?.page ?? 1);
  const pageSize = Number(query?.pageSize ?? 20);
  const bids = mockBidsByListing[pathParams.auctionId] ?? [];
  return ok(paginate(bids, page, pageSize));
});

route('post', '/auctions/:auctionId/auto-bid', async () => {
  return ok(undefined);
});

route('delete', '/auctions/:auctionId/auto-bid', async () => {
  return ok(undefined);
});

// ─── Seller Dashboard Handlers ──────────────────────────────────────────────

route('get', '/seller/active-listings', async ({ query }) => {
  const page = Number(query?.page ?? 1);
  const pageSize = Number(query?.pageSize ?? 20);
  const sellerId = currentMockUser?.id ?? 'user-1';
  const items: SellerActiveListing[] = getSellerActiveListings(sellerId);
  return ok(paginate(items, page, pageSize));
});

route('get', '/seller/completed-auctions', async ({ query }) => {
  const page = Number(query?.page ?? 1);
  const pageSize = Number(query?.pageSize ?? 20);
  const sellerId = currentMockUser?.id ?? 'user-1';
  const items: SellerCompletedAuction[] = getSellerCompletedAuctions(sellerId);
  return ok(paginate(items, page, pageSize));
});

// ─── User Profile Handlers ──────────────────────────────────────────────────

route('get', '/users/profile', async () => {
  const user = currentMockUser ?? mockUsers[0];
  return ok(user);
});

route('put', '/users/profile', async ({ body }) => {
  const updates = body as { displayName?: string; avatarUrl?: string };
  const user = currentMockUser ?? mockUsers[0];
  const updated: User = { ...user, ...updates };
  setCurrentMockUser(updated);
  return ok(updated);
});

route('get', '/users/watchlist', async ({ query }) => {
  const page = Number(query?.page ?? 1);
  const pageSize = Number(query?.pageSize ?? 20);
  const watchlistListings = mockListings
    .filter((l) => mockWatchlist.has(l.id))
    .map(listingToSummary);
  return ok(paginate(watchlistListings, page, pageSize));
});

route('post', '/users/watchlist/:listingId', async ({ pathParams }) => {
  mockWatchlist.add(pathParams.listingId);
  return ok(undefined);
});

route('delete', '/users/watchlist/:listingId', async ({ pathParams }) => {
  mockWatchlist.delete(pathParams.listingId);
  return ok(undefined);
});

// ─── Notification Handlers ──────────────────────────────────────────────────

route('get', '/notifications', async ({ query }) => {
  const page = Number(query?.page ?? 1);
  const pageSize = Number(query?.pageSize ?? 20);
  return ok(paginate(mockNotifications, page, pageSize));
});

route('put', '/notifications/:id/read', async ({ pathParams }) => {
  const notif = mockNotifications.find((n) => n.id === pathParams.id);
  if (notif) notif.isRead = true;
  return ok(undefined);
});

route('put', '/notifications/read-all', async () => {
  mockNotifications.forEach((n) => { n.isRead = true; });
  return ok(undefined);
});

// ─── Payment Handlers ───────────────────────────────────────────────────────

route('post', '/payments/create-payment-intent', async ({ body }) => {
  const { auctionId, amount } = body as { auctionId: string; amount: number };
  return ok({
    id: generateId(),
    clientSecret: 'mock_secret_' + generateId(),
    amount,
    currency: 'usd',
    status: 'requires_confirmation',
  });
});

route('post', '/payments/confirm-payment', async () => {
  return ok(undefined);
});

route('get', '/payments/history', async ({ query }) => {
  const page = Number(query?.page ?? 1);
  const pageSize = Number(query?.pageSize ?? 20);
  const records = Array.from({ length: 5 }, (_, i) => ({
    id: `payment-${i + 1}`,
    auctionId: mockListings[i]?.id ?? `listing-${i}`,
    amount: 100 + i * 75,
    currency: 'usd',
    status: 'succeeded',
    createdAt: new Date(Date.now() - i * 86_400_000).toISOString(),
  }));
  const start = (page - 1) * pageSize;
  const sliced = records.slice(start, start + pageSize);
  return ok({ data: sliced, total: records.length });
});

// ─── Admin Handlers ─────────────────────────────────────────────────────────

route('get', '/admin/users', async ({ query }) => {
  const page = Number(query?.page ?? 1);
  const pageSize = Number(query?.pageSize ?? 20);
  const rows: AdminUserRow[] = getAdminUserRows();
  return ok(paginate(rows, page, pageSize));
});

route('put', '/admin/users/:id/suspend', async ({ pathParams }) => {
  const user = mockUsers.find((u) => u.id === pathParams.id);
  if (!user) return notFound('User not found');
  return ok(undefined);
});

route('put', '/admin/users/:id/activate', async ({ pathParams }) => {
  const user = mockUsers.find((u) => u.id === pathParams.id);
  if (!user) return notFound('User not found');
  return ok(undefined);
});

route('delete', '/admin/users/:id', async ({ pathParams }) => {
  const user = mockUsers.find((u) => u.id === pathParams.id);
  if (!user) return notFound('User not found');
  return ok(undefined);
});

route('get', '/admin/listings', async ({ query }) => {
  const page = Number(query?.page ?? 1);
  const pageSize = Number(query?.pageSize ?? 20);
  const rows: AdminListingRow[] = getAdminListingRows();
  return ok(paginate(rows, page, pageSize));
});
