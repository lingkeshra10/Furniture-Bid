import { vi } from 'vitest';
import type { PlaceBidResponse, Bid } from '@/types/auction';
import type { BidUpdateEvent, PaginatedResponse } from '@/types/common';

// Mock the auction service
vi.mock('@/services/api/auctionService', () => ({
  auctionService: {
    placeBid: vi.fn(),
    getBidHistory: vi.fn(),
    activateAutoBid: vi.fn(),
    deactivateAutoBid: vi.fn(),
  },
}));

import { useAuctionStore } from './auction';
import { auctionService } from '@/services/api/auctionService';

const mockedService = vi.mocked(auctionService);

function createBid(overrides: Partial<Bid> = {}): Bid {
  return {
    id: 'bid-1',
    auctionId: 'auction-1',
    bidderId: 'user-1',
    bidderAlias: 'Bidder A',
    amount: 100,
    timestamp: '2024-01-01T00:00:00Z',
    ...overrides,
  };
}

describe('useAuctionStore', () => {
  let store: ReturnType<typeof useAuctionStore>;

  beforeEach(() => {
    store = useAuctionStore();
    // Reset state between tests
    store.currentBids.value = new Map();
    store.autoBidConfigs.value = new Map();
    store.bidSubmitting.value = false;
    vi.clearAllMocks();
  });

  describe('getters', () => {
    it('getBidsForAuction returns empty array when no bids exist', () => {
      expect(store.getBidsForAuction('auction-1')).toEqual([]);
    });

    it('getBidsForAuction returns bids for a given auction', () => {
      const bid = createBid();
      store.currentBids.value.set('auction-1', [bid]);
      expect(store.getBidsForAuction('auction-1')).toEqual([bid]);
    });

    it('getAutoBidConfig returns undefined when no config exists', () => {
      expect(store.getAutoBidConfig('auction-1')).toBeUndefined();
    });

    it('getAutoBidConfig returns config for a given auction', () => {
      const config = { auctionId: 'auction-1', maxAmount: 500, isActive: true };
      store.autoBidConfigs.value.set('auction-1', config);
      expect(store.getAutoBidConfig('auction-1')).toEqual(config);
    });

    it('hasActiveBids returns false when no bids exist', () => {
      expect(store.hasActiveBids('auction-1')).toBe(false);
    });

    it('hasActiveBids returns true when bids exist', () => {
      store.currentBids.value.set('auction-1', [createBid()]);
      expect(store.hasActiveBids('auction-1')).toBe(true);
    });
  });

  describe('placeBid', () => {
    it('sets bidSubmitting to true during request', async () => {
      const response: PlaceBidResponse = { success: true, bid: createBid() };
      mockedService.placeBid.mockImplementation(async () => {
        expect(store.bidSubmitting.value).toBe(true);
        return response;
      });

      await store.placeBid({ auctionId: 'auction-1', amount: 100 });
      expect(store.bidSubmitting.value).toBe(false);
    });

    it('prepends bid to currentBids on success', async () => {
      const bid = createBid({ id: 'bid-new', amount: 200 });
      const response: PlaceBidResponse = { success: true, bid };
      mockedService.placeBid.mockResolvedValue(response);

      // Seed an existing bid
      store.currentBids.value.set('auction-1', [createBid()]);

      await store.placeBid({ auctionId: 'auction-1', amount: 200 });
      const bids = store.getBidsForAuction('auction-1');
      expect(bids[0]).toEqual(bid);
      expect(bids.length).toBe(2);
    });

    it('does not modify bids on failure response', async () => {
      const response: PlaceBidResponse = { success: false, error: 'Bid too low' };
      mockedService.placeBid.mockResolvedValue(response);

      await store.placeBid({ auctionId: 'auction-1', amount: 50 });
      expect(store.getBidsForAuction('auction-1')).toEqual([]);
    });

    it('resets bidSubmitting on error', async () => {
      mockedService.placeBid.mockRejectedValue(new Error('Network error'));

      await expect(store.placeBid({ auctionId: 'auction-1', amount: 100 })).rejects.toThrow();
      expect(store.bidSubmitting.value).toBe(false);
    });
  });

  describe('fetchBidHistory', () => {
    it('sets bids for auction on page 1', async () => {
      const bids = [createBid({ id: 'bid-1' }), createBid({ id: 'bid-2' })];
      const response: PaginatedResponse<Bid> = {
        data: bids,
        total: 2,
        page: 1,
        pageSize: 20,
        hasMore: false,
      };
      mockedService.getBidHistory.mockResolvedValue(response);

      await store.fetchBidHistory({ auctionId: 'auction-1', page: 1, pageSize: 20 });
      expect(store.getBidsForAuction('auction-1')).toEqual(bids);
    });

    it('appends bids on subsequent pages', async () => {
      const existingBid = createBid({ id: 'bid-existing' });
      store.currentBids.value.set('auction-1', [existingBid]);

      const newBids = [createBid({ id: 'bid-3' })];
      const response: PaginatedResponse<Bid> = {
        data: newBids,
        total: 3,
        page: 2,
        pageSize: 20,
        hasMore: false,
      };
      mockedService.getBidHistory.mockResolvedValue(response);

      await store.fetchBidHistory({ auctionId: 'auction-1', page: 2, pageSize: 20 });
      const allBids = store.getBidsForAuction('auction-1');
      expect(allBids.length).toBe(2);
      expect(allBids[0]).toEqual(existingBid);
      expect(allBids[1]).toEqual(newBids[0]);
    });
  });

  describe('activateAutoBid', () => {
    it('calls service and sets config with isActive true', async () => {
      mockedService.activateAutoBid.mockResolvedValue(undefined);

      await store.activateAutoBid({ auctionId: 'auction-1', maxAmount: 500 });

      expect(mockedService.activateAutoBid).toHaveBeenCalledWith({ auctionId: 'auction-1', maxAmount: 500 });
      expect(store.getAutoBidConfig('auction-1')).toEqual({
        auctionId: 'auction-1',
        maxAmount: 500,
        isActive: true,
      });
    });
  });

  describe('deactivateAutoBid', () => {
    it('calls service and sets config isActive to false', async () => {
      mockedService.deactivateAutoBid.mockResolvedValue(undefined);
      store.autoBidConfigs.value.set('auction-1', {
        auctionId: 'auction-1',
        maxAmount: 500,
        isActive: true,
      });

      await store.deactivateAutoBid('auction-1');

      expect(mockedService.deactivateAutoBid).toHaveBeenCalledWith('auction-1');
      expect(store.getAutoBidConfig('auction-1')).toEqual({
        auctionId: 'auction-1',
        maxAmount: 500,
        isActive: false,
      });
    });
  });

  describe('handleBidUpdate', () => {
    it('prepends a new bid entry from BidUpdateEvent', () => {
      const event: BidUpdateEvent = {
        auctionId: 'auction-1',
        currentBid: 250,
        bidCount: 5,
        bidderAlias: 'Bidder X',
        timestamp: '2024-01-02T00:00:00Z',
      };

      store.handleBidUpdate(event);

      const bids = store.getBidsForAuction('auction-1');
      expect(bids.length).toBe(1);
      expect(bids[0].amount).toBe(250);
      expect(bids[0].bidderAlias).toBe('Bidder X');
      expect(bids[0].auctionId).toBe('auction-1');
      expect(bids[0].timestamp).toBe('2024-01-02T00:00:00Z');
    });

    it('prepends to existing bids', () => {
      store.currentBids.value.set('auction-1', [createBid({ amount: 100 })]);

      const event: BidUpdateEvent = {
        auctionId: 'auction-1',
        currentBid: 150,
        bidCount: 2,
        bidderAlias: 'Bidder Y',
        timestamp: '2024-01-03T00:00:00Z',
      };

      store.handleBidUpdate(event);

      const bids = store.getBidsForAuction('auction-1');
      expect(bids.length).toBe(2);
      expect(bids[0].amount).toBe(150);
      expect(bids[1].amount).toBe(100);
    });
  });

  describe('singleton behavior', () => {
    it('returns the same state across multiple calls', () => {
      const store1 = useAuctionStore();
      const store2 = useAuctionStore();

      store1.currentBids.value.set('auction-1', [createBid()]);
      expect(store2.getBidsForAuction('auction-1').length).toBe(1);
    });
  });
});
