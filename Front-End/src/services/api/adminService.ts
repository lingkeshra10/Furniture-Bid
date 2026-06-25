import { getApiClient } from './client';
import type {
  AdminUserRow,
  AdminListingRow,
  ListingReport,
  AnalyticsSummary,
  AuctionTrend,
  CategoryDistribution,
  TopSeller,
  AnalyticsQuery,
} from '@/types/user';
import type { PaginatedResponse } from '@/types/common';

const client = getApiClient();

export const adminService = {
  async getUsers(page = 1, pageSize = 20): Promise<PaginatedResponse<AdminUserRow>> {
    const { data } = await client.get<PaginatedResponse<AdminUserRow>>('/admin/users', {
      params: { page, pageSize },
    });
    return data;
  },

  async suspendUser(userId: string): Promise<void> {
    await client.put(`/admin/users/${userId}/suspend`);
  },

  async activateUser(userId: string): Promise<void> {
    await client.put(`/admin/users/${userId}/activate`);
  },

  async deleteUser(userId: string): Promise<void> {
    await client.delete(`/admin/users/${userId}`);
  },

  async getListings(page = 1, pageSize = 20): Promise<PaginatedResponse<AdminListingRow>> {
    const { data } = await client.get<PaginatedResponse<AdminListingRow>>('/admin/listings', {
      params: { page, pageSize },
    });
    return data;
  },

  async removeListing(listingId: string): Promise<void> {
    await client.delete(`/admin/listings/${listingId}`);
  },

  async flagListing(listingId: string): Promise<void> {
    await client.put(`/admin/listings/${listingId}/flag`);
  },

  async getReportedListings(page = 1, pageSize = 20): Promise<PaginatedResponse<ListingReport>> {
    const { data } = await client.get<PaginatedResponse<ListingReport>>('/admin/listings/reports', {
      params: { page, pageSize },
    });
    return data;
  },

  async getAnalyticsSummary(query: AnalyticsQuery): Promise<AnalyticsSummary> {
    const { data } = await client.get<AnalyticsSummary>('/admin/analytics/summary', {
      params: { startDate: query.startDate, endDate: query.endDate },
    });
    return data;
  },

  async getAuctionTrends(query: AnalyticsQuery): Promise<AuctionTrend[]> {
    const { data } = await client.get<AuctionTrend[]>('/admin/analytics/auction-trends', {
      params: { startDate: query.startDate, endDate: query.endDate },
    });
    return data;
  },

  async getCategoryDistribution(query: AnalyticsQuery): Promise<CategoryDistribution[]> {
    const { data } = await client.get<CategoryDistribution[]>('/admin/analytics/category-distribution', {
      params: { startDate: query.startDate, endDate: query.endDate },
    });
    return data;
  },

  async getTopSellers(query: AnalyticsQuery): Promise<TopSeller[]> {
    const { data } = await client.get<TopSeller[]>('/admin/analytics/top-sellers', {
      params: { startDate: query.startDate, endDate: query.endDate },
    });
    return data;
  },
};
