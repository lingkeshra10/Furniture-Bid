<script setup lang="ts">
import { onMounted, ref } from 'vue'
import WatchlistGrid from '@/components/watchlist/WatchlistGrid.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import ErrorState from '@/components/common/ErrorState.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { useWatchlistStore } from '@/stores/watchlist'

const watchlistStore = useWatchlistStore()
const error = ref<string | null>(null)

async function loadWatchlist(): Promise<void> {
  error.value = null
  try {
    await watchlistStore.fetchWatchlist()
  } catch (e) {
    error.value = 'Failed to load your watchlist. Please try again.'
  }
}

onMounted(() => {
  loadWatchlist()
})
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 py-6 sm:px-6 lg:px-8">
    <h1 class="text-2xl font-bold text-text mb-6">My Watchlist</h1>

    <!-- Loading state -->
    <LoadingSpinner
      v-if="watchlistStore.isLoading"
      size="lg"
      message="Loading your watchlist..."
    />

    <!-- Error state with retry -->
    <ErrorState
      v-else-if="error"
      :message="error"
      retry-label="Retry"
      @retry="loadWatchlist"
    />

    <!-- Empty state -->
    <EmptyState
      v-else-if="watchlistStore.watchedItems.length === 0"
      title="Your watchlist is empty"
      message="Browse the catalog and add items to your watchlist to track auctions you're interested in."
    />

    <!-- Watchlist grid -->
    <WatchlistGrid
      v-else
      :items="watchlistStore.watchedItems"
    />
  </div>
</template>
