import { describe, it, expect, vi, beforeEach } from 'vitest';
import { useWatchlistStore } from './watchlist';
import type { FurnitureListingSummary } from '@/types/furniture';
import type { PaginatedResponse } from '@/types/common';

vi.mock('@/services/api/userProfileService', () => ({
  userProfileService: {
    getWatchlist: vi.fn(),
    addToWatchlist: vi.fn(),
    removeFromWatchlist: vi.fn(),
  },
}));

import { userProfileService } from '@/services/api/userProfileService';

const mockedService = userProfileService as {
  getWatchlist: ReturnType<typeof vi.fn>;
  addToWatchlist: ReturnType<typeof vi.fn>;
  removeFromWatchlist: ReturnType<typeof vi.fn>;
};

function createMockItem(overrides: Partial<FurnitureListingSummary> = {}): FurnitureListingSummary {
  return {
    id: 'item-1',
    title: 'Test Chair',
    thumbnailUrl: 'https://example.com/chair.jpg',
    currentBid: 100,
    timeRemaining: 60000,
    condition: 'good',
    category: 'office-chair',
    ...overrides,
  };
}

describe('useWatchlistStore', () => {
  let store: ReturnType<typeof useWatchlistStore>;

  beforeEach(() => {
    store = useWatchlistStore();
    // Reset state between tests
    store.watchedItems.value = [];
    store.watchedIds.value = new Set();
    store.isLoading.value = false;
    vi.clearAllMocks();
  });

  describe('initial state', () => {
    it('should have empty watchedItems', () => {
      expect(store.watchedItems.value).toEqual([]);
    });

    it('should have empty watchedIds', () => {
      expect(store.watchedIds.value.size).toBe(0);
    });

    it('should have isLoading as false', () => {
      expect(store.isLoading.value).toBe(false);
    });

    it('should return watchlistCount as 0', () => {
      expect(store.watchlistCount.value).toBe(0);
    });
  });

  describe('isWatched', () => {
    it('should return false for items not in watchlist', () => {
      expect(store.isWatched('nonexistent-id')).toBe(false);
    });

    it('should return true for items in watchlist', () => {
      store.watchedIds.value = new Set(['item-1']);
      expect(store.isWatched('item-1')).toBe(true);
    });
  });

  describe('fetchWatchlist', () => {
    it('should fetch and sort items by timeRemaining ascending', async () => {
      const items: FurnitureListingSummary[] = [
        createMockItem({ id: 'item-a', timeRemaining: 90000 }),
        createMockItem({ id: 'item-b', timeRemaining: 30000 }),
        createMockItem({ id: 'item-c', timeRemaining: 60000 }),
      ];
      const response: PaginatedResponse<FurnitureListingSummary> = {
        data: items,
        total: 3,
        page: 1,
        pageSize: 20,
        hasMore: false,
      };
      mockedService.getWatchlist.mockResolvedValue(response);

      await store.fetchWatchlist();

      expect(store.watchedItems.value[0].id).toBe('item-b');
      expect(store.watchedItems.value[1].id).toBe('item-c');
      expect(store.watchedItems.value[2].id).toBe('item-a');
    });

    it('should populate watchedIds from fetched items', async () => {
      const items: FurnitureListingSummary[] = [
        createMockItem({ id: 'item-x' }),
        createMockItem({ id: 'item-y' }),
      ];
      const response: PaginatedResponse<FurnitureListingSummary> = {
        data: items,
        total: 2,
        page: 1,
        pageSize: 20,
        hasMore: false,
      };
      mockedService.getWatchlist.mockResolvedValue(response);

      await store.fetchWatchlist();

      expect(store.watchedIds.value.has('item-x')).toBe(true);
      expect(store.watchedIds.value.has('item-y')).toBe(true);
      expect(store.watchlistCount.value).toBe(2);
    });

    it('should set isLoading during fetch', async () => {
      let resolvePromise: (value: unknown) => void;
      const pendingPromise = new Promise((resolve) => { resolvePromise = resolve; });
      mockedService.getWatchlist.mockReturnValue(pendingPromise);

      const fetchPromise = store.fetchWatchlist();
      expect(store.isLoading.value).toBe(true);

      resolvePromise!({ data: [], total: 0, page: 1, pageSize: 20, hasMore: false });
      await fetchPromise;
      expect(store.isLoading.value).toBe(false);
    });

    it('should set isLoading to false on error', async () => {
      mockedService.getWatchlist.mockRejectedValue(new Error('Network error'));

      await expect(store.fetchWatchlist()).rejects.toThrow('Network error');
      expect(store.isLoading.value).toBe(false);
    });
  });

  describe('addToWatchlist', () => {
    it('should optimistically add listingId to watchedIds', async () => {
      mockedService.addToWatchlist.mockResolvedValue(undefined);

      const promise = store.addToWatchlist('new-item');
      // Optimistic: id added immediately
      expect(store.watchedIds.value.has('new-item')).toBe(true);

      await promise;
      expect(store.watchedIds.value.has('new-item')).toBe(true);
    });

    it('should rollback on API error', async () => {
      mockedService.addToWatchlist.mockRejectedValue(new Error('Server error'));

      await expect(store.addToWatchlist('fail-item')).rejects.toThrow('Server error');
      expect(store.watchedIds.value.has('fail-item')).toBe(false);
    });

    it('should update watchlistCount after add', async () => {
      mockedService.addToWatchlist.mockResolvedValue(undefined);

      await store.addToWatchlist('item-1');
      expect(store.watchlistCount.value).toBe(1);
    });
  });

  describe('removeFromWatchlist', () => {
    it('should optimistically remove from watchedIds and watchedItems', async () => {
      store.watchedIds.value = new Set(['item-1', 'item-2']);
      store.watchedItems.value = [
        createMockItem({ id: 'item-1' }),
        createMockItem({ id: 'item-2' }),
      ];
      mockedService.removeFromWatchlist.mockResolvedValue(undefined);

      const promise = store.removeFromWatchlist('item-1');
      // Optimistic: removed immediately
      expect(store.watchedIds.value.has('item-1')).toBe(false);
      expect(store.watchedItems.value.find((i) => i.id === 'item-1')).toBeUndefined();

      await promise;
      expect(store.watchedIds.value.has('item-1')).toBe(false);
    });

    it('should rollback on API error', async () => {
      store.watchedIds.value = new Set(['item-1']);
      store.watchedItems.value = [createMockItem({ id: 'item-1' })];
      mockedService.removeFromWatchlist.mockRejectedValue(new Error('Server error'));

      await expect(store.removeFromWatchlist('item-1')).rejects.toThrow('Server error');
      expect(store.watchedIds.value.has('item-1')).toBe(true);
      expect(store.watchedItems.value.length).toBe(1);
    });
  });

  describe('updateWatchedItemBid', () => {
    it('should update currentBid for a matched item', () => {
      store.watchedItems.value = [
        createMockItem({ id: 'auction-1', currentBid: 50 }),
        createMockItem({ id: 'auction-2', currentBid: 75 }),
      ];

      store.updateWatchedItemBid('auction-1', 120);

      expect(store.watchedItems.value[0].currentBid).toBe(120);
      expect(store.watchedItems.value[1].currentBid).toBe(75);
    });

    it('should do nothing if auctionId is not found', () => {
      store.watchedItems.value = [createMockItem({ id: 'auction-1', currentBid: 50 })];

      store.updateWatchedItemBid('nonexistent', 200);

      expect(store.watchedItems.value[0].currentBid).toBe(50);
    });
  });
});
