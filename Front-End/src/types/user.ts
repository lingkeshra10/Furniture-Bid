import type { UserRole } from './auth';
import type { FurnitureCategory } from './furniture';
import type { ListingStatus } from './furniture';

export type AccountStatus = 'active' | 'suspended' | 'deleted';

export interface AdminUserRow {
  id: string;
  displayName: string;
  email: string;
  role: UserRole;
  registeredAt: string;
  status: AccountStatus;
}

export interface AdminListingRow {
  id: string;
  title: string;
  sellerDisplayName: string;
  status: ListingStatus;
  currentBid: number;
  reportCount: number;
}

export interface ListingReport {
  id: string;
  listingId: string;
  reason: string;
  reporterDisplayName: string;
  reportDate: string;
}

export interface AnalyticsSummary {
  totalUsers: number;
  activeAuctions: number;
  completedAuctions: number;
  totalRevenue: number;
}

export interface AuctionTrend {
  date: string;
  auctionsCreated: number;
  auctionsCompleted: number;
}

export interface CategoryDistribution {
  category: FurnitureCategory;
  count: number;
}

export interface TopSeller {
  displayName: string;
  completedAuctions: number;
  totalRevenue: number;
}

export interface AnalyticsQuery {
  startDate: string;
  endDate: string;
}
