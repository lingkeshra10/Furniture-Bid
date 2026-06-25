import { ref, computed } from 'vue';
import type { FurnitureListingSummary } from '@/types/furniture';
import { userProfileService } from '@/services/api/userProfileService';

/**
 * Watchlist store implemented as a singleton composable using Vue's reactivity system.
 * State is declared at module scope so it's shared across all component usages.
 *
 * Requirements:
 * - 6.1: "Add to Watchlist" / "Remove from Watchlist" toggle
 * - 6.2: Add/remove from watchlist and persist via User Profile Service
 * - 6.3: Display all watched items sorted by auction ending soonest, with empty state
 * - 18.1: Stores organized by domain
 */

// --- Singleton reactive state (shared across all usages) ---
const watchedItems = ref<FurnitureListingSummary[]>([]);
const watchedIds = ref<Set<string>>(new Set());
const isLoading = ref(false);

// --- Getters ---
const watchlistCount = computed(() => watchedIds.value.size);

function isWatched(listingId: string): boolean {
  return watchedIds.value.has(listingId);
}

// --- Actions ---

/**
 * Fetch the user's watchlist from the API.
 * Sorts items by timeRemaining ascending (ending soonest first).
 * Populates watchedIds from the fetched items.
 */
async function fetchWatchlist(): Promise<void> {
  isLoading.value = true;
  try {
    const response = await userProfileService.getWatchlist();
    const sorted = [...response.data].sort((a, b) => a.timeRemaining - b.timeRemaining);
    watchedItems.value = sorted;
    watchedIds.value = new Set(sorted.map((item) => item.id));
  } finally {
    isLoading.value = false;
  }
}

/**
 * Optimistically add a listing to the watchlist.
 * Adds the ID to watchedIds immediately, then persists via API.
 * On error, rolls back the optimistic addition and rethrows.
 *
 * Requirement 6.1, 6.2
 */
async function addToWatchlist(listingId: string): Promise<void> {
  // Optimistic update
  watchedIds.value = new Set([...watchedIds.value, listingId]);

  try {
    await userProfileService.addToWatchlist(listingId);
  } catch (error) {
    // Rollback on failure
    const rolled = new Set(watchedIds.value);
    rolled.delete(listingId);
    watchedIds.value = rolled;
    throw error;
  }
}

/**
 * Optimistically remove a listing from the watchlist.
 * Removes from watchedIds and watchedItems immediately, then persists via API.
 * On error, rolls back to previous state and rethrows.
 *
 * Requirement 6.1, 6.2
 */
async function removeFromWatchlist(listingId: string): Promise<void> {
  // Capture state for rollback
  const previousIds = watchedIds.value;
  const previousItems = watchedItems.value;

  // Optimistic update
  const newIds = new Set(watchedIds.value);
  newIds.delete(listingId);
  watchedIds.value = newIds;
  watchedItems.value = watchedItems.value.filter((item) => item.id !== listingId);

  try {
    await userProfileService.removeFromWatchlist(listingId);
  } catch (error) {
    // Rollback on failure
    watchedIds.value = previousIds;
    watchedItems.value = previousItems;
    throw error;
  }
}

/**
 * Update the current bid for a watched item when a real-time bid event arrives.
 * Finds the item by auctionId (matches item.id) and updates its currentBid.
 */
function updateWatchedItemBid(auctionId: string, bid: number): void {
  const index = watchedItems.value.findIndex((item) => item.id === auctionId);
  if (index !== -1) {
    watchedItems.value[index] = {
      ...watchedItems.value[index],
      currentBid: bid,
    };
  }
}

// --- Composable export ---

/**
 * Singleton composable for watchlist state management.
 * All component usages share the same reactive state.
 */
/**
 * Reset watchlist store to initial defaults.
 * Called on 401 unauthorized to clear sensitive session data.
 * Requirement 18.3
 */
function $reset(): void {
  watchedItems.value = [];
  watchedIds.value = new Set();
  isLoading.value = false;
}

export function useWatchlistStore() {
  return {
    // State
    watchedItems,
    watchedIds,
    isLoading,

    // Getters
    watchlistCount,
    isWatched,

    // Actions
    fetchWatchlist,
    addToWatchlist,
    removeFromWatchlist,
    updateWatchedItemBid,
    $reset,
  };
}
