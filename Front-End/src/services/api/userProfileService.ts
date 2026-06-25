import { getApiClient } from './client';
import type { User } from '@/types/auth';
import type { FurnitureListingSummary } from '@/types/furniture';
import type { PaginatedResponse } from '@/types/common';

const client = getApiClient();

export interface UpdateProfileRequest {
  displayName?: string;
  avatarUrl?: string;
}

export const userProfileService = {
  async getProfile(): Promise<User> {
    const { data } = await client.get<User>('/users/profile');
    return data;
  },

  async updateProfile(request: UpdateProfileRequest): Promise<User> {
    const { data } = await client.put<User>('/users/profile', request);
    return data;
  },

  async getWatchlist(page = 1, pageSize = 20): Promise<PaginatedResponse<FurnitureListingSummary>> {
    const { data } = await client.get<PaginatedResponse<FurnitureListingSummary>>('/users/watchlist', {
      params: { page, pageSize },
    });
    return data;
  },

  async addToWatchlist(listingId: string): Promise<void> {
    await client.post(`/users/watchlist/${listingId}`);
  },

  async removeFromWatchlist(listingId: string): Promise<void> {
    await client.delete(`/users/watchlist/${listingId}`);
  },
};
