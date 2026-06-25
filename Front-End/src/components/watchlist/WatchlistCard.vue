<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import type { FurnitureListingSummary } from '@/types/furniture'
import type { AuctionResult } from '@/types/auction'
import { formatCurrency, formatCountdown } from '@/utils/formatters'

export interface WatchlistItem extends FurnitureListingSummary {
  auctionResult?: AuctionResult;
  isOutbid?: boolean;
}

const props = defineProps<{
  item: WatchlistItem
}>()

const formattedBid = computed(() => formatCurrency(props.item.currentBid))
const formattedTime = computed(() => formatCountdown(props.item.timeRemaining))
const isEnded = computed(() => props.item.timeRemaining <= 0)

/**
 * Determine the status badge to display.
 * Priority:
 * - Won: user placed the highest bid (auction ended)
 * - Lost: user was outbid and reserve was met (auction ended)
 * - Reserve Not Met: reserve price was not reached (auction ended)
 * - Outbid: active auction where user has been outbid
 */
const statusBadge = computed(() => {
  const result = props.item.auctionResult

  if (result === 'won') {
    return { label: 'Won', classes: 'bg-success text-white' }
  }
  if (result === 'lost') {
    return { label: 'Lost', classes: 'bg-red-100 text-red-800' }
  }
  if (result === 'reserve-not-met') {
    return { label: 'Reserve Not Met', classes: 'bg-yellow-100 text-yellow-800' }
  }
  if (props.item.isOutbid && !isEnded.value) {
    return { label: 'Outbid', classes: 'bg-accent/10 text-accent' }
  }

  return null
})
</script>

<template>
  <RouterLink
    :to="{ name: 'listing-detail', params: { id: item.id } }"
    class="block rounded-lg bg-card shadow-sm hover:shadow-md transition-shadow duration-200 overflow-hidden min-h-touch"
    :aria-label="`View watched listing: ${item.title}`"
  >
    <!-- Thumbnail with status badge -->
    <div class="relative aspect-[4/3] overflow-hidden">
      <img
        :src="item.thumbnailUrl"
        :alt="item.title"
        class="w-full h-full object-cover"
        loading="lazy"
      />
      <span
        v-if="statusBadge"
        :class="[statusBadge.classes, 'absolute top-2 left-2 px-2 py-0.5 rounded text-xs font-semibold']"
      >
        {{ statusBadge.label }}
      </span>
    </div>

    <!-- Card content -->
    <div class="p-3">
      <!-- Title -->
      <h3 class="text-text font-medium text-sm truncate">
        {{ item.title }}
      </h3>

      <!-- Current bid and time remaining -->
      <div class="mt-2 flex items-center justify-between">
        <span class="text-primary font-semibold text-base">
          {{ formattedBid }}
        </span>
        <span
          :class="[
            'text-xs',
            isEnded ? 'text-red-500 font-medium' : 'text-gray-500'
          ]"
        >
          {{ formattedTime }}
        </span>
      </div>
    </div>
  </RouterLink>
</template>
