<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { auctionService } from '@/services/api/auctionService';
import { socketService } from '@/services/websocket/socketClient';
import LoadingSpinner from '@/components/common/LoadingSpinner.vue';
import ErrorState from '@/components/common/ErrorState.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import PaginationControls from '@/components/common/PaginationControls.vue';
import SellerListingCard from './SellerListingCard.vue';
import type { SellerActiveListing } from '@/types/auction';
import type { BidUpdateEvent } from '@/types/common';

const emit = defineEmits<{
  'listing-click': [id: string];
}>();

const listings = ref<SellerActiveListing[]>([]);
const isLoading = ref(false);
const error = ref<string | null>(null);
const currentPage = ref(1);
const totalPages = ref(1);
const pageSize = 20;

const showEmpty = computed(
  () => !isLoading.value && !error.value && listings.value.length === 0
);

async function fetchListings(page = 1): Promise<void> {
  isLoading.value = true;
  error.value = null;
  try {
    const response = await auctionService.getSellerActiveListings(page, pageSize);
    listings.value = response.data;
    currentPage.value = response.page;
    totalPages.value = Math.ceil(response.total / response.pageSize) || 1;
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to load active listings. Please try again.';
  } finally {
    isLoading.value = false;
  }
}

function handlePageChange(page: number): void {
  fetchListings(page);
}

function handleRetry(): void {
  fetchListings(currentPage.value);
}

function handleListingClick(id: string): void {
  emit('listing-click', id);
}

// Real-time bid update handler
function handleBidUpdate(event: BidUpdateEvent): void {
  const listing = listings.value.find((l) => l.id === event.auctionId);
  if (listing) {
    listing.currentBid = event.currentBid;
    listing.bidCount = event.bidCount;
  }
}

// Subscribe to bid updates for all active listings
function subscribeToUpdates(): void {
  for (const listing of listings.value) {
    socketService.onBidUpdate(listing.id, handleBidUpdate);
  }
}

// Re-subscribe to WebSocket updates when listings change
watch(listings, () => {
  subscribeToUpdates();
});

onMounted(() => {
  fetchListings(1);
});
</script>

<template>
  <section aria-labelledby="active-listings-heading">
    <h2 id="active-listings-heading" class="text-lg font-semibold text-text mb-4">
      Active Listings
    </h2>

    <!-- Loading state -->
    <LoadingSpinner
      v-if="isLoading && listings.length === 0"
      size="md"
      message="Loading active listings..."
    />

    <!-- Error state -->
    <ErrorState
      v-else-if="error"
      :message="error"
      @retry="handleRetry"
    />

    <!-- Empty state -->
    <EmptyState
      v-else-if="showEmpty"
      title="No active listings"
      message="You don't have any active listings. Create a new listing to start receiving bids."
    />

    <!-- Listings -->
    <template v-else>
      <div class="flex flex-col gap-3">
        <SellerListingCard
          v-for="listing in listings"
          :key="listing.id"
          :listing="listing"
          type="active"
          @click="handleListingClick"
        />
      </div>

      <!-- Pagination -->
      <div class="mt-4">
        <PaginationControls
          :current-page="currentPage"
          :total-pages="totalPages"
          :disabled="isLoading"
          @page-change="handlePageChange"
        />
      </div>
    </template>
  </section>
</template>
