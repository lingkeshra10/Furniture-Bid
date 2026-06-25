<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useFurnitureStore } from '@/stores/furniture';
import { useAuctionStore } from '@/stores/auction';
import { useWatchlistStore } from '@/stores/watchlist';
import { useAuthStore } from '@/stores/auth';
import { socketService } from '@/services/websocket/socketClient';
import type { BidUpdateEvent } from '@/types/common';

import LoadingSpinner from '@/components/common/LoadingSpinner.vue';
import ErrorState from '@/components/common/ErrorState.vue';
import ImageGallery from '@/components/listing/ImageGallery.vue';
import ListingDetail from '@/components/listing/ListingDetail.vue';
import BidPanel from '@/components/listing/BidPanel.vue';
import BidHistory from '@/components/listing/BidHistory.vue';
import AutoBidToggle from '@/components/listing/AutoBidToggle.vue';
import AuctionCountdown from '@/components/listing/AuctionCountdown.vue';
import SellerInfo from '@/components/listing/SellerInfo.vue';

const route = useRoute();
const furnitureStore = useFurnitureStore();
const auctionStore = useAuctionStore();
const watchlistStore = useWatchlistStore();
const authStore = useAuthStore();

const listingId = computed(() => route.params.id as string);
const error = ref<string | null>(null);
const auctionEndedByCountdown = ref(false);

const listing = computed(() => furnitureStore.currentListing.value);
const isLoading = computed(() => furnitureStore.isLoading.value);

const isAuctionEnded = computed(() => {
  if (!listing.value) return false;
  // Check status first
  if (listing.value.status === 'ended') return true;
  // Check if countdown reached zero
  if (auctionEndedByCountdown.value) return true;
  // Fallback: check endDate directly
  return new Date(listing.value.auctionEndDate).getTime() <= Date.now();
});

const isWatched = computed(() => {
  if (!listing.value) return false;
  return watchlistStore.isWatched(listing.value.id);
});

// Detect auction end from the AuctionCountdown component's internal countdown
// We poll the endDate to track when auction time expires
let auctionEndCheckInterval: ReturnType<typeof setInterval> | null = null;

function startAuctionEndCheck() {
  stopAuctionEndCheck();
  if (!listing.value) return;

  const endTime = new Date(listing.value.auctionEndDate).getTime();
  if (endTime <= Date.now()) {
    auctionEndedByCountdown.value = true;
    return;
  }

  auctionEndCheckInterval = setInterval(() => {
    if (listing.value) {
      const endTime = new Date(listing.value.auctionEndDate).getTime();
      if (endTime <= Date.now()) {
        auctionEndedByCountdown.value = true;
        stopAuctionEndCheck();
      }
    }
  }, 1000);
}

function stopAuctionEndCheck() {
  if (auctionEndCheckInterval) {
    clearInterval(auctionEndCheckInterval);
    auctionEndCheckInterval = null;
  }
}

// Fetch listing data
async function fetchListing() {
  error.value = null;
  auctionEndedByCountdown.value = false;
  try {
    await furnitureStore.fetchListingById(listingId.value);
    // Start tracking auction end
    startAuctionEndCheck();
  } catch (err: unknown) {
    const message = err instanceof Error ? err.message : 'Failed to load listing. Please try again.';
    error.value = message;
  }
}

// Handle real-time bid updates from WebSocket
function handleBidUpdate(event: BidUpdateEvent) {
  auctionStore.handleBidUpdate(event);
  furnitureStore.updateCurrentBid(event.auctionId, event.currentBid, event.bidCount);
}

// Watchlist toggle
async function toggleWatchlist() {
  if (!listing.value || !authStore.isAuthenticated.value) return;

  try {
    if (isWatched.value) {
      await watchlistStore.removeFromWatchlist(listing.value.id);
    } else {
      await watchlistStore.addToWatchlist(listing.value.id);
    }
  } catch {
    // Handled by store (optimistic rollback)
  }
}

// Lifecycle: mount
onMounted(async () => {
  await fetchListing();

  // Subscribe to WebSocket auction room
  if (listingId.value) {
    socketService.joinAuctionRoom(listingId.value);
    socketService.onBidUpdate(listingId.value, handleBidUpdate);
  }
});

// Lifecycle: unmount
onUnmounted(() => {
  if (listingId.value) {
    socketService.leaveAuctionRoom(listingId.value);
  }
  stopAuctionEndCheck();
});

// Watch for route changes (same page, different listing)
watch(listingId, async (newId, oldId) => {
  if (newId !== oldId) {
    // Leave old room
    if (oldId) {
      socketService.leaveAuctionRoom(oldId);
    }
    stopAuctionEndCheck();
    // Fetch new listing and join new room
    await fetchListing();
    if (newId) {
      socketService.joinAuctionRoom(newId);
      socketService.onBidUpdate(newId, handleBidUpdate);
    }
  }
});
</script>

<template>
  <div class="min-h-screen bg-background">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
      <!-- Loading State -->
      <LoadingSpinner
        v-if="isLoading && !listing"
        size="lg"
        message="Loading listing..."
      />

      <!-- Error State -->
      <ErrorState
        v-else-if="error"
        :message="error"
        @retry="fetchListing"
      />

      <!-- Listing Content -->
      <div v-else-if="listing" class="space-y-6">
        <!-- Header with title and watchlist button -->
        <div class="flex items-start justify-between gap-4">
          <h1 class="text-2xl md:text-3xl font-bold text-text">{{ listing.title }}</h1>

          <!-- Watchlist Toggle Button (authenticated users only) -->
          <button
            v-if="authStore.isAuthenticated.value"
            @click="toggleWatchlist"
            class="flex-shrink-0 min-w-[44px] min-h-[44px] flex items-center justify-center rounded-full border transition-colors focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2"
            :class="isWatched
              ? 'bg-red-50 border-red-200 text-red-500 hover:bg-red-100'
              : 'bg-white border-gray-200 text-gray-400 hover:bg-gray-50 hover:text-red-400'"
            :aria-label="isWatched ? 'Remove from watchlist' : 'Add to watchlist'"
            :aria-pressed="isWatched"
          >
            <!-- Heart icon - filled when watched -->
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-6 w-6"
              :fill="isWatched ? 'currentColor' : 'none'"
              viewBox="0 0 24 24"
              stroke="currentColor"
              stroke-width="2"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"
              />
            </svg>
          </button>
        </div>

        <!-- Two-column layout -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <!-- Left Column: Images + Details + Seller Info -->
          <div class="lg:col-span-2 space-y-6">
            <!-- Image Gallery -->
            <ImageGallery :images="listing.images" />

            <!-- Listing Details -->
            <div class="bg-card rounded-lg border border-gray-200 p-4 md:p-6 shadow-sm">
              <ListingDetail :listing="listing" />
            </div>

            <!-- Seller Info -->
            <SellerInfo
              :seller-name="listing.sellerDisplayName"
              :seller-rating="listing.sellerRating"
            />
          </div>

          <!-- Right Column: Countdown + Bid Panel + Auto-Bid + Bid History -->
          <div class="space-y-6">
            <!-- Auction Countdown -->
            <div class="bg-card rounded-lg border border-gray-200 p-4 shadow-sm">
              <h3 class="text-sm font-medium text-gray-500 text-center mb-3">Time Remaining</h3>
              <AuctionCountdown :end-date="listing.auctionEndDate" />
            </div>

            <!-- Auction Result Banner (if ended) -->
            <div
              v-if="isAuctionEnded"
              class="bg-gray-50 rounded-lg border border-gray-200 p-4 text-center"
              role="status"
              aria-live="polite"
            >
              <p class="text-sm font-semibold text-gray-600">This auction has ended.</p>
              <p v-if="listing.currentBid > 0" class="text-sm text-gray-500 mt-1">
                Final bid: <span class="font-semibold text-primary">${{ listing.currentBid.toFixed(2) }}</span>
              </p>
            </div>

            <!-- Bid Panel -->
            <BidPanel
              :auction-id="listing.id"
              :current-bid="listing.currentBid"
              :is-auction-ended="isAuctionEnded"
            />

            <!-- Auto-Bid Toggle -->
            <AutoBidToggle
              :auction-id="listing.id"
              :current-bid="listing.currentBid"
              :is-auction-ended="isAuctionEnded"
            />

            <!-- Bid History -->
            <BidHistory :auction-id="listing.id" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
