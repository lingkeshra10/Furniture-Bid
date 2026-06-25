import { getApiClient } from './client';
import type { PlaceBidRequest, PlaceBidResponse, BidHistoryQuery, Bid, AutoBidRequest, SellerActiveListing, SellerCompletedAuction, UserBidHistoryItem } from '@/types/auction';
import type { PaginatedResponse } from '@/types/common';

const client = getApiClient();

export const auctionService = {
  async placeBid(request: PlaceBidRequest): Promise<PlaceBidResponse> {
    const { data } = await client.post<PlaceBidResponse>('/auctions/bids', request);
    return data;
  },

  async getBidHistory(query: BidHistoryQuery): Promise<PaginatedResponse<Bid>> {
    const { data } = await client.get<PaginatedResponse<Bid>>(`/auctions/${query.auctionId}/bids`, {
      params: { page: query.page, pageSize: query.pageSize },
    });
    return data;
  },

  async getUserBidHistory(page = 1, pageSize = 20): Promise<PaginatedResponse<UserBidHistoryItem>> {
    const { data } = await client.get<PaginatedResponse<UserBidHistoryItem>>('/users/bids', {
      params: { page, pageSize },
    });
    return data;
  },

  async activateAutoBid(request: AutoBidRequest): Promise<void> {
    await client.post(`/auctions/${request.auctionId}/auto-bid`, { maxAmount: request.maxAmount });
  },

  async deactivateAutoBid(auctionId: string): Promise<void> {
    await client.delete(`/auctions/${auctionId}/auto-bid`);
  },

  async getSellerActiveListings(page = 1, pageSize = 20): Promise<PaginatedResponse<SellerActiveListing>> {
    const { data } = await client.get<PaginatedResponse<SellerActiveListing>>('/seller/active-listings', {
      params: { page, pageSize },
    });
    return data;
  },

  async getSellerCompletedAuctions(page = 1, pageSize = 20): Promise<PaginatedResponse<SellerCompletedAuction>> {
    const { data } = await client.get<PaginatedResponse<SellerCompletedAuction>>('/seller/completed-auctions', {
      params: { page, pageSize },
    });
    return data;
  },
};
