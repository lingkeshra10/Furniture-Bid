import { getApiClient } from './client';
import type { FurnitureListing, FurnitureListingSummary, CreateListingRequest, CatalogQuery } from '@/types/furniture';
import type { PaginatedResponse } from '@/types/common';

const client = getApiClient();

export const furnitureService = {
  async getListings(query: CatalogQuery): Promise<PaginatedResponse<FurnitureListingSummary>> {
    const { data } = await client.get<PaginatedResponse<FurnitureListingSummary>>('/furniture', {
      params: {
        ...query.filters,
        sort: query.sort,
        page: query.page,
        pageSize: query.pageSize,
      },
    });
    return data;
  },

  async getListingById(id: string): Promise<FurnitureListing> {
    const { data } = await client.get<FurnitureListing>(`/furniture/${id}`);
    return data;
  },

  async createListing(request: CreateListingRequest): Promise<FurnitureListing> {
    const formData = new FormData();
    formData.append('title', request.title);
    formData.append('description', request.description);
    formData.append('category', request.category);
    formData.append('condition', request.condition);
    if (request.brand) formData.append('brand', request.brand);
    if (request.material) formData.append('material', request.material);
    formData.append('dimensions', JSON.stringify(request.dimensions));
    if (request.weight) formData.append('weight', String(request.weight));
    if (request.location) formData.append('location', request.location);
    formData.append('startingPrice', String(request.startingPrice));
    formData.append('reservePrice', String(request.reservePrice));
    formData.append('auctionEndDate', request.auctionEndDate);
    request.images.forEach((file) => {
      formData.append('images', file);
    });

    const { data } = await client.post<FurnitureListing>('/furniture', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return data;
  },

  async flagListing(id: string, reason: string): Promise<void> {
    await client.put(`/furniture/${id}/flag`, { reason });
  },

  async removeListing(id: string): Promise<void> {
    await client.delete(`/furniture/${id}`);
  },
};
