import { describe, it, expect, vi, beforeEach } from 'vitest';
import { useFurnitureStore } from './furniture';
import { furnitureService } from '@/services/api/furnitureService';
import type { PaginatedResponse } from '@/types/common';
import type { FurnitureListingSummary, FurnitureListing } from '@/types/furniture';

vi.mock('@/services/api/furnitureService', () => ({
  furnitureService: {
    getListings: vi.fn(),
    getListingById: vi.fn(),
    createListing: vi.fn(),
  },
}));

const mockListingSummary: FurnitureListingSummary = {
  id: '1',
  title: 'Test Sofa',
  thumbnailUrl: 'https://example.com/sofa.jpg',
  currentBid: 100,
  timeRemaining: 3600000,
  condition: 'good',
  category: 'sofa',
};

const mockListing: FurnitureListing = {
  id: '1',
  title: 'Test Sofa',
  description: 'A comfortable sofa',
  category: 'sofa',
  condition: 'good',
  dimensions: { width: 200, height: 90, length: 85 },
  images: ['https://example.com/sofa.jpg'],
  startingPrice: 50,
  reservePrice: 200,
  currentBid: 100,
  bidCount: 5,
  auctionEndDate: '2025-12-31T00:00:00Z',
  status: 'active',
  sellerId: 'seller-1',
  sellerDisplayName: 'John',
  sellerRating: 4.5,
  createdAt: '2025-01-01T00:00:00Z',
};

describe('useFurnitureStore', () => {
  let store: ReturnType<typeof useFurnitureStore>;

  beforeEach(() => {
    store = useFurnitureStore();
    // Reset state between tests
    store.listings.value = [];
    store.currentListing.value = null;
    store.filters.value = {};
    store.sort.value = 'ending-soonest';
    store.page.value = 1;
    store.hasMore.value = true;
    store.isLoading.value = false;
    vi.clearAllMocks();
  });

  it('returns shared singleton state', () => {
    const store1 = useFurnitureStore();
    const store2 = useFurnitureStore();
    expect(store1.listings).toBe(store2.listings);
    expect(store1.currentListing).toBe(store2.currentListing);
  });

  describe('activeFiltersCount', () => {
    it('returns 0 when no filters are set', () => {
      expect(store.activeFiltersCount.value).toBe(0);
    });

    it('counts category filter', () => {
      store.filters.value = { category: ['sofa'] };
      expect(store.activeFiltersCount.value).toBe(1);
    });

    it('counts multiple filters', () => {
      store.filters.value = {
        category: ['sofa', 'bed-frame'],
        condition: ['good'],
        priceMin: 10,
        priceMax: 500,
        location: 'NYC',
      };
      expect(store.activeFiltersCount.value).toBe(5);
    });

    it('does not count empty arrays', () => {
      store.filters.value = { category: [], condition: [] };
      expect(store.activeFiltersCount.value).toBe(0);
    });
  });

  describe('fetchListings', () => {
    it('fetches listings and appends to state', async () => {
      const response: PaginatedResponse<FurnitureListingSummary> = {
        data: [mockListingSummary],
        total: 1,
        page: 1,
        pageSize: 20,
        hasMore: false,
      };
      vi.mocked(furnitureService.getListings).mockResolvedValue(response);

      await store.fetchListings();

      expect(furnitureService.getListings).toHaveBeenCalledWith({
        filters: {},
        sort: 'ending-soonest',
        page: 1,
        pageSize: 20,
      });
      expect(store.listings.value).toEqual([mockListingSummary]);
      expect(store.hasMore.value).toBe(false);
      expect(store.page.value).toBe(2);
    });

    it('resets listings when reset=true', async () => {
      store.listings.value = [mockListingSummary];
      store.page.value = 3;

      const response: PaginatedResponse<FurnitureListingSummary> = {
        data: [{ ...mockListingSummary, id: '2' }],
        total: 1,
        page: 1,
        pageSize: 20,
        hasMore: true,
      };
      vi.mocked(furnitureService.getListings).mockResolvedValue(response);

      await store.fetchListings(true);

      expect(store.listings.value).toEqual([{ ...mockListingSummary, id: '2' }]);
      expect(store.hasMore.value).toBe(true);
      expect(store.page.value).toBe(2);
    });

    it('sets isLoading during fetch', async () => {
      let resolvePromise: (value: PaginatedResponse<FurnitureListingSummary>) => void;
      const pendingPromise = new Promise<PaginatedResponse<FurnitureListingSummary>>((resolve) => {
        resolvePromise = resolve;
      });
      vi.mocked(furnitureService.getListings).mockReturnValue(pendingPromise);

      const fetchPromise = store.fetchListings();
      expect(store.isLoading.value).toBe(true);

      resolvePromise!({ data: [], total: 0, page: 1, pageSize: 20, hasMore: false });
      await fetchPromise;
      expect(store.isLoading.value).toBe(false);
    });

    it('resets isLoading on error', async () => {
      vi.mocked(furnitureService.getListings).mockRejectedValue(new Error('Network error'));

      await expect(store.fetchListings()).rejects.toThrow('Network error');
      expect(store.isLoading.value).toBe(false);
    });
  });

  describe('fetchListingById', () => {
    it('fetches a listing and sets currentListing', async () => {
      vi.mocked(furnitureService.getListingById).mockResolvedValue(mockListing);

      await store.fetchListingById('1');

      expect(furnitureService.getListingById).toHaveBeenCalledWith('1');
      expect(store.currentListing.value).toEqual(mockListing);
    });
  });

  describe('createListing', () => {
    it('creates a listing and returns its id', async () => {
      vi.mocked(furnitureService.createListing).mockResolvedValue(mockListing);

      const request = {
        title: 'Test Sofa',
        description: 'A comfortable sofa',
        category: 'sofa' as const,
        condition: 'good' as const,
        dimensions: { width: 200, height: 90, length: 85 },
        startingPrice: 50,
        reservePrice: 200,
        auctionEndDate: '2025-12-31T00:00:00Z',
        images: [] as File[],
      };

      const id = await store.createListing(request);

      expect(furnitureService.createListing).toHaveBeenCalledWith(request);
      expect(id).toBe('1');
    });
  });

  describe('updateFilters', () => {
    it('sets new filters and refetches with reset', async () => {
      const response: PaginatedResponse<FurnitureListingSummary> = {
        data: [mockListingSummary],
        total: 1,
        page: 1,
        pageSize: 20,
        hasMore: false,
      };
      vi.mocked(furnitureService.getListings).mockResolvedValue(response);

      await store.updateFilters({ category: ['sofa'] });

      expect(store.filters.value).toEqual({ category: ['sofa'] });
      expect(furnitureService.getListings).toHaveBeenCalledWith(
        expect.objectContaining({ filters: { category: ['sofa'] }, page: 1 })
      );
    });
  });

  describe('updateSort', () => {
    it('sets new sort and refetches with reset', async () => {
      const response: PaginatedResponse<FurnitureListingSummary> = {
        data: [mockListingSummary],
        total: 1,
        page: 1,
        pageSize: 20,
        hasMore: false,
      };
      vi.mocked(furnitureService.getListings).mockResolvedValue(response);

      await store.updateSort('price-low-high');

      expect(store.sort.value).toBe('price-low-high');
      expect(furnitureService.getListings).toHaveBeenCalledWith(
        expect.objectContaining({ sort: 'price-low-high', page: 1 })
      );
    });
  });

  describe('updateCurrentBid', () => {
    it('updates currentListing bid when id matches', () => {
      store.currentListing.value = { ...mockListing };

      store.updateCurrentBid('1', 150, 8);

      expect(store.currentListing.value.currentBid).toBe(150);
      expect(store.currentListing.value.bidCount).toBe(8);
    });

    it('does not update when id does not match', () => {
      store.currentListing.value = { ...mockListing };

      store.updateCurrentBid('different-id', 150, 8);

      expect(store.currentListing.value.currentBid).toBe(100);
      expect(store.currentListing.value.bidCount).toBe(5);
    });

    it('does nothing when currentListing is null', () => {
      store.currentListing.value = null;

      store.updateCurrentBid('1', 150, 8);

      expect(store.currentListing.value).toBeNull();
    });
  });
});
