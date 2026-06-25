/**
 * Mock data generators and sample datasets for front-end development.
 *
 * Provides realistic sample data for all entity types:
 * users, furniture listings, bids, and notifications.
 *
 * Requirement: 14.4
 */

import type { User, UserRole } from '@/types/auth';
import type {
  FurnitureListing,
  FurnitureListingSummary,
  FurnitureCategory,
  FurnitureCondition,
  Dimensions,
} from '@/types/furniture';
import type { Bid, SellerActiveListing, SellerCompletedAuction } from '@/types/auction';
import type { Notification, NotificationType } from '@/types/notification';
import type { AdminUserRow, AdminListingRow } from '@/types/user';

// ─── Utility Helpers ────────────────────────────────────────────────────────

let idCounter = 1000;

export function generateId(): string {
  return `mock-${++idCounter}`;
}

export function randomDate(daysAgo: number, daysAhead = 0): string {
  const now = Date.now();
  const min = now - daysAgo * 86_400_000;
  const max = now + daysAhead * 86_400_000;
  return new Date(min + Math.random() * (max - min)).toISOString();
}

function randomInt(min: number, max: number): number {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function pick<T>(arr: readonly T[]): T {
  return arr[Math.floor(Math.random() * arr.length)];
}

// ─── Sample Lookup Data ─────────────────────────────────────────────────────

const CATEGORIES: FurnitureCategory[] = [
  'sofa', 'dining-table', 'office-chair', 'wardrobe',
  'bed-frame', 'coffee-table', 'cabinet', 'bookshelf',
];

const CONDITIONS: FurnitureCondition[] = ['new', 'like-new', 'good', 'fair', 'poor'];

const FURNITURE_TITLES: Record<FurnitureCategory, string[]> = {
  'sofa': ['Modern L-Shape Sofa', 'Vintage Chesterfield Sofa', 'Scandinavian 3-Seater', 'Leather Recliner Sofa', 'Velvet Sectional Sofa'],
  'dining-table': ['Oak Farmhouse Dining Table', 'Glass Top Dining Table', 'Extendable Walnut Table', 'Industrial Steel Dining Table', 'Round Marble Table'],
  'office-chair': ['Ergonomic Mesh Chair', 'Executive Leather Chair', 'Standing Desk Chair', 'Herman Miller Aeron Clone', 'Adjustable Drafting Chair'],
  'wardrobe': ['Walk-in Wardrobe System', 'Sliding Door Wardrobe', 'Antique Mahogany Wardrobe', 'IKEA PAX Wardrobe', 'Mirrored Wardrobe'],
  'bed-frame': ['Queen Platform Bed Frame', 'King Upholstered Bed', 'Minimalist Metal Frame', 'Solid Wood Canopy Bed', 'Storage Bed Frame'],
  'coffee-table': ['Mid-Century Coffee Table', 'Rustic Reclaimed Wood Table', 'Lift-Top Coffee Table', 'Nesting Side Tables Set', 'Glass & Chrome Table'],
  'cabinet': ['Media Console Cabinet', 'Vintage China Cabinet', 'Industrial Storage Cabinet', 'Floating Wall Cabinet', 'Bar Cabinet with Wine Rack'],
  'bookshelf': ['Floor-to-Ceiling Bookshelf', 'Ladder Shelf Unit', 'Modular Cube Bookcase', 'Solid Oak Library Shelf', 'Corner Bookshelf'],
};

const MATERIALS = ['oak', 'walnut', 'pine', 'steel', 'glass', 'leather', 'velvet', 'marble', 'bamboo', 'teak'];
const BRANDS = ['IKEA', 'West Elm', 'Pottery Barn', 'Crate & Barrel', 'CB2', 'Article', 'Restoration Hardware', undefined];
const LOCATIONS = ['New York, NY', 'Los Angeles, CA', 'Chicago, IL', 'Houston, TX', 'Phoenix, AZ', 'San Francisco, CA', 'Seattle, WA', 'Austin, TX'];

// ─── Users ──────────────────────────────────────────────────────────────────

export const mockUsers: User[] = [
  { id: 'user-1', email: 'alice@example.com', displayName: 'Alice Johnson', role: 'seller', avatarUrl: 'https://i.pravatar.cc/150?u=alice', createdAt: randomDate(365) },
  { id: 'user-2', email: 'bob@example.com', displayName: 'Bob Smith', role: 'buyer', avatarUrl: 'https://i.pravatar.cc/150?u=bob', createdAt: randomDate(300) },
  { id: 'user-3', email: 'carol@example.com', displayName: 'Carol Davis', role: 'seller', avatarUrl: 'https://i.pravatar.cc/150?u=carol', createdAt: randomDate(250) },
  { id: 'user-4', email: 'dave@example.com', displayName: 'Dave Wilson', role: 'buyer', avatarUrl: 'https://i.pravatar.cc/150?u=dave', createdAt: randomDate(200) },
  { id: 'user-5', email: 'eve@example.com', displayName: 'Eve Martinez', role: 'admin', avatarUrl: 'https://i.pravatar.cc/150?u=eve', createdAt: randomDate(400) },
  { id: 'user-6', email: 'frank@example.com', displayName: 'Frank Lee', role: 'seller', avatarUrl: 'https://i.pravatar.cc/150?u=frank', createdAt: randomDate(180) },
  { id: 'user-7', email: 'grace@example.com', displayName: 'Grace Kim', role: 'buyer', avatarUrl: 'https://i.pravatar.cc/150?u=grace', createdAt: randomDate(150) },
  { id: 'user-8', email: 'henry@example.com', displayName: 'Henry Brown', role: 'buyer', avatarUrl: 'https://i.pravatar.cc/150?u=henry', createdAt: randomDate(120) },
];

// Currently logged-in user (for mock auth)
export let currentMockUser: User | null = mockUsers[0];

export function setCurrentMockUser(user: User | null): void {
  currentMockUser = user;
}

// ─── Furniture Listings ─────────────────────────────────────────────────────

function generateDimensions(): Dimensions {
  return {
    width: randomInt(40, 250),
    height: randomInt(30, 200),
    length: randomInt(40, 300),
  };
}

function generateListing(id: string, seller: User): FurnitureListing {
  const category = pick(CATEGORIES);
  const titles = FURNITURE_TITLES[category];
  const title = pick(titles);
  const startingPrice = randomInt(50, 2000);
  const bidCount = randomInt(0, 15);
  const currentBid = bidCount > 0 ? startingPrice + randomInt(10, 500) : startingPrice;

  return {
    id,
    title,
    description: `Beautiful ${title.toLowerCase()} in excellent condition. Perfect for any modern home. Well-maintained and ready for pickup or delivery.`,
    category,
    condition: pick(CONDITIONS),
    brand: pick(BRANDS),
    material: pick(MATERIALS),
    dimensions: generateDimensions(),
    weight: randomInt(5, 80),
    location: pick(LOCATIONS),
    images: [
      `https://picsum.photos/seed/${id}-1/800/600`,
      `https://picsum.photos/seed/${id}-2/800/600`,
      `https://picsum.photos/seed/${id}-3/800/600`,
    ],
    startingPrice,
    reservePrice: startingPrice + randomInt(100, 500),
    currentBid,
    bidCount,
    auctionEndDate: randomDate(0, 14),
    status: 'active',
    sellerId: seller.id,
    sellerDisplayName: seller.displayName,
    sellerRating: parseFloat((3.5 + Math.random() * 1.5).toFixed(1)),
    createdAt: randomDate(30),
  };
}

const sellers = mockUsers.filter((u) => u.role === 'seller');

export const mockListings: FurnitureListing[] = Array.from({ length: 25 }, (_, i) => {
  return generateListing(`listing-${i + 1}`, pick(sellers));
});

export function listingToSummary(listing: FurnitureListing): FurnitureListingSummary {
  const endDate = new Date(listing.auctionEndDate).getTime();
  return {
    id: listing.id,
    title: listing.title,
    thumbnailUrl: listing.images[0] ?? '',
    currentBid: listing.currentBid,
    timeRemaining: Math.max(0, endDate - Date.now()),
    condition: listing.condition,
    category: listing.category,
  };
}

// ─── Bids ───────────────────────────────────────────────────────────────────

export function generateBidsForListing(listing: FurnitureListing): Bid[] {
  if (listing.bidCount === 0) return [];

  const bids: Bid[] = [];
  let amount = listing.startingPrice;
  const buyers = mockUsers.filter((u) => u.role === 'buyer');

  for (let i = 0; i < listing.bidCount; i++) {
    amount += randomInt(5, 50);
    const bidder = pick(buyers);
    bids.push({
      id: `bid-${listing.id}-${i + 1}`,
      auctionId: listing.id,
      bidderId: bidder.id,
      bidderAlias: `Bidder${bidder.id.slice(-1)}`,
      amount,
      timestamp: randomDate(7),
    });
  }

  // Sort by timestamp descending (most recent first)
  bids.sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());
  return bids;
}

// Pre-generate bid histories for all listings
export const mockBidsByListing: Record<string, Bid[]> = {};
for (const listing of mockListings) {
  mockBidsByListing[listing.id] = generateBidsForListing(listing);
}

// ─── Notifications ──────────────────────────────────────────────────────────

const NOTIFICATION_TEMPLATES: Array<{ type: NotificationType; title: string; message: string }> = [
  { type: 'outbid', title: 'You have been outbid', message: 'Someone placed a higher bid on "Modern L-Shape Sofa". Current bid is now $450.' },
  { type: 'auction-ending', title: 'Auction ending soon', message: 'The auction for "Oak Farmhouse Dining Table" ends in 1 hour.' },
  { type: 'auction-won', title: 'Congratulations! You won', message: 'You won the auction for "Ergonomic Mesh Chair" with a bid of $320.' },
  { type: 'auction-lost', title: 'Auction ended', message: 'The auction for "Vintage Chesterfield Sofa" has ended. You were outbid.' },
  { type: 'auto-bid-placed', title: 'Auto-bid placed', message: 'Your auto-bid placed $280 on "Rustic Reclaimed Wood Table".' },
  { type: 'auto-bid-limit-reached', title: 'Auto-bid limit reached', message: 'Your maximum auto-bid amount of $500 has been reached on "King Upholstered Bed".' },
];

export const mockNotifications: Notification[] = Array.from({ length: 12 }, (_, i) => {
  const template = pick(NOTIFICATION_TEMPLATES);
  return {
    id: `notif-${i + 1}`,
    type: template.type,
    title: template.title,
    message: template.message,
    auctionId: pick(mockListings).id,
    isRead: i > 3, // first few are unread
    createdAt: randomDate(7),
  };
}).sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());

// ─── Seller Dashboard Data ──────────────────────────────────────────────────

export function getSellerActiveListings(sellerId: string): SellerActiveListing[] {
  return mockListings
    .filter((l) => l.sellerId === sellerId && l.status === 'active')
    .map((l) => ({
      id: l.id,
      title: l.title,
      currentBid: l.currentBid,
      bidCount: l.bidCount,
      timeRemaining: Math.max(0, new Date(l.auctionEndDate).getTime() - Date.now()),
    }));
}

export function getSellerCompletedAuctions(sellerId: string): SellerCompletedAuction[] {
  // Simulate some completed auctions for the seller
  return mockListings
    .filter((l) => l.sellerId === sellerId)
    .slice(0, 5)
    .map((l) => ({
      id: l.id,
      title: l.title,
      winningBid: l.currentBid + randomInt(50, 200),
      winnerDisplayName: pick(mockUsers.filter((u) => u.role === 'buyer')).displayName,
      reserveMet: Math.random() > 0.3,
      endedAt: randomDate(30),
    }));
}

// ─── Admin Data ─────────────────────────────────────────────────────────────

export function getAdminUserRows(): AdminUserRow[] {
  return mockUsers.map((u) => ({
    id: u.id,
    displayName: u.displayName,
    email: u.email,
    role: u.role,
    registeredAt: u.createdAt,
    status: 'active' as const,
  }));
}

export function getAdminListingRows(): AdminListingRow[] {
  return mockListings.map((l) => ({
    id: l.id,
    title: l.title,
    sellerDisplayName: l.sellerDisplayName,
    status: l.status,
    currentBid: l.currentBid,
    reportCount: randomInt(0, 3),
  }));
}

// ─── Watchlist ──────────────────────────────────────────────────────────────

export const mockWatchlist: Set<string> = new Set([
  mockListings[0]?.id,
  mockListings[2]?.id,
  mockListings[5]?.id,
].filter(Boolean) as string[]);
