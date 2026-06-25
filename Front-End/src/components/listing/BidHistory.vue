<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useAuctionStore } from '@/stores/auction';
import { formatCurrency, formatDateTime } from '@/utils/formatters';
import { PAGE_SIZE } from '@/utils/constants';

const props = defineProps<{
  auctionId: string;
}>();

const auctionStore = useAuctionStore();

const currentPage = ref(1);
const isLoadingMore = ref(false);
const hasMore = ref(true);

const bids = computed(() => auctionStore.getBidsForAuction(props.auctionId));

async function loadInitial() {
  try {
    await auctionStore.fetchBidHistory({
      auctionId: props.auctionId,
      page: 1,
      pageSize: PAGE_SIZE,
    });
    // If fewer than PAGE_SIZE results returned, no more pages
    const fetched = auctionStore.getBidsForAuction(props.auctionId);
    hasMore.value = fetched.length >= PAGE_SIZE;
  } catch {
    // Silently handle — bids array will be empty
  }
}

async function loadMore() {
  if (isLoadingMore.value || !hasMore.value) return;

  isLoadingMore.value = true;
  currentPage.value += 1;

  try {
    const prevLength = bids.value.length;
    await auctionStore.fetchBidHistory({
      auctionId: props.auctionId,
      page: currentPage.value,
      pageSize: PAGE_SIZE,
    });
    const newLength = bids.value.length;
    const fetched = newLength - prevLength;
    hasMore.value = fetched >= PAGE_SIZE;
  } catch {
    // Revert page on failure
    currentPage.value -= 1;
  } finally {
    isLoadingMore.value = false;
  }
}

onMounted(() => {
  loadInitial();
});
</script>

<template>
  <div class="bg-card rounded-lg border border-gray-200 p-4 md:p-6 shadow-sm">
    <h3 class="text-lg font-semibold text-text mb-4">Bid History</h3>

    <!-- Empty State -->
    <div v-if="bids.length === 0" class="text-center py-6">
      <p class="text-sm text-gray-500">No bids placed yet. Be the first to bid!</p>
    </div>

    <!-- Bid List -->
    <ul v-else class="space-y-3" role="list" aria-label="Bid history">
      <li
        v-for="bid in bids"
        :key="bid.id"
        class="flex items-center justify-between border-b border-gray-100 pb-3 last:border-b-0 last:pb-0"
      >
        <div class="flex flex-col">
          <span class="text-sm font-medium text-text">{{ bid.bidderAlias }}</span>
          <span class="text-xs text-gray-500">{{ formatDateTime(bid.timestamp) }}</span>
        </div>
        <span class="text-sm font-semibold text-primary">
          {{ formatCurrency(bid.amount) }}
        </span>
      </li>
    </ul>

    <!-- Load More Button -->
    <div v-if="bids.length > 0 && hasMore" class="mt-4 text-center">
      <button
        type="button"
        @click="loadMore"
        :disabled="isLoadingMore"
        class="min-h-touch px-4 py-2 text-sm font-medium text-primary border border-primary rounded-md hover:bg-primary/5 focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
      >
        <span v-if="isLoadingMore">Loading...</span>
        <span v-else>Load More</span>
      </button>
    </div>
  </div>
</template>
