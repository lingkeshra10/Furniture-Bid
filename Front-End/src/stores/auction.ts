import { ref } from 'vue';
import type { Bid, PlaceBidRequest, PlaceBidResponse, AutoBidConfig, AutoBidRequest, BidHistoryQuery } from '@/types/auction';
import type { BidUpdateEvent } from '@/types/common';
import { auctionService } from '@/services/api/auctionService';

// Module-level state (singleton — shared across all consumers)
const currentBids = ref<Map<string, Bid[]>>(new Map());
const autoBidConfigs = ref<Map<string, AutoBidConfig>>(new Map());
const bidSubmitting = ref(false);

// Getters
function getBidsForAuction(auctionId: string): Bid[] {
  return currentBids.value.get(auctionId) ?? [];
}

function getAutoBidConfig(auctionId: string): AutoBidConfig | undefined {
  return autoBidConfigs.value.get(auctionId);
}

function hasActiveBids(auctionId: string): boolean {
  const bids = currentBids.value.get(auctionId);
  return bids !== undefined && bids.length > 0;
}

// Actions
async function placeBid(request: PlaceBidRequest): Promise<PlaceBidResponse> {
  bidSubmitting.value = true;
  try {
    const response = await auctionService.placeBid(request);
    if (response.success && response.bid) {
      const existing = currentBids.value.get(request.auctionId) ?? [];
      currentBids.value.set(request.auctionId, [response.bid, ...existing]);
      // Trigger reactivity by reassigning the Map
      currentBids.value = new Map(currentBids.value);
    }
    return response;
  } finally {
    bidSubmitting.value = false;
  }
}

async function fetchBidHistory(query: BidHistoryQuery): Promise<void> {
  const response = await auctionService.getBidHistory(query);
  if (query.page === 1) {
    currentBids.value.set(query.auctionId, response.data);
  } else {
    const existing = currentBids.value.get(query.auctionId) ?? [];
    currentBids.value.set(query.auctionId, [...existing, ...response.data]);
  }
  // Trigger reactivity
  currentBids.value = new Map(currentBids.value);
}

async function activateAutoBid(request: AutoBidRequest): Promise<void> {
  await auctionService.activateAutoBid(request);
  autoBidConfigs.value.set(request.auctionId, {
    auctionId: request.auctionId,
    maxAmount: request.maxAmount,
    isActive: true,
  });
  // Trigger reactivity
  autoBidConfigs.value = new Map(autoBidConfigs.value);
}

async function deactivateAutoBid(auctionId: string): Promise<void> {
  await auctionService.deactivateAutoBid(auctionId);
  const existing = autoBidConfigs.value.get(auctionId);
  if (existing) {
    autoBidConfigs.value.set(auctionId, { ...existing, isActive: false });
  }
  // Trigger reactivity
  autoBidConfigs.value = new Map(autoBidConfigs.value);
}

function handleBidUpdate(event: BidUpdateEvent): void {
  const bid: Bid = {
    id: `bid-${event.timestamp}`,
    auctionId: event.auctionId,
    bidderId: '',
    bidderAlias: event.bidderAlias,
    amount: event.currentBid,
    timestamp: event.timestamp,
  };
  const existing = currentBids.value.get(event.auctionId) ?? [];
  currentBids.value.set(event.auctionId, [bid, ...existing]);
  // Trigger reactivity
  currentBids.value = new Map(currentBids.value);
}

/**
 * Reset auction store to initial defaults.
 * Called on 401 unauthorized to clear sensitive session data.
 * Requirement 18.3
 */
function $reset(): void {
  currentBids.value = new Map();
  autoBidConfigs.value = new Map();
  bidSubmitting.value = false;
}

export function useAuctionStore() {
  return {
    // State
    currentBids,
    autoBidConfigs,
    bidSubmitting,
    // Getters
    getBidsForAuction,
    getAutoBidConfig,
    hasActiveBids,
    // Actions
    placeBid,
    fetchBidHistory,
    activateAutoBid,
    deactivateAutoBid,
    handleBidUpdate,
    $reset,
  };
}
