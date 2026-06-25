<script setup lang="ts">
import { computed } from 'vue'
import WatchlistCard from './WatchlistCard.vue'
import type { WatchlistItem } from './WatchlistCard.vue'

const props = defineProps<{
  items: WatchlistItem[]
}>()

/**
 * Sort watched items by auction ending soonest first (ascending timeRemaining).
 * Items with timeRemaining <= 0 (ended) are placed at the end.
 * Requirement 6.3: Display all watched items sorted by auction ending soonest first.
 */
const sortedItems = computed(() => {
  return [...props.items].sort((a, b) => {
    // Ended auctions go to the end
    const aEnded = a.timeRemaining <= 0
    const bEnded = b.timeRemaining <= 0

    if (aEnded && !bEnded) return 1
    if (!aEnded && bEnded) return -1
    if (aEnded && bEnded) return 0

    return a.timeRemaining - b.timeRemaining
  })
})
</script>

<template>
  <div
    class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4"
    role="list"
    aria-label="Watchlist items"
  >
    <div
      v-for="item in sortedItems"
      :key="item.id"
      role="listitem"
    >
      <WatchlistCard :item="item" />
    </div>
  </div>
</template>
