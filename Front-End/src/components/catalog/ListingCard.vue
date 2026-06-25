<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import type { FurnitureListingSummary } from '@/types/furniture'
import { formatCurrency, formatCountdown } from '@/utils/formatters'

const props = defineProps<{
  listing: FurnitureListingSummary
}>()

const formattedBid = computed(() => formatCurrency(props.listing.currentBid))
const formattedTime = computed(() => formatCountdown(props.listing.timeRemaining))

const conditionLabel = computed(() => {
  const labels: Record<string, string> = {
    'new': 'New',
    'like-new': 'Like New',
    'good': 'Good',
    'fair': 'Fair',
    'poor': 'Poor',
  }
  return labels[props.listing.condition] || props.listing.condition
})

const conditionColor = computed(() => {
  const colors: Record<string, string> = {
    'new': 'bg-success text-white',
    'like-new': 'bg-green-100 text-green-800',
    'good': 'bg-blue-100 text-blue-800',
    'fair': 'bg-yellow-100 text-yellow-800',
    'poor': 'bg-red-100 text-red-800',
  }
  return colors[props.listing.condition] || 'bg-gray-100 text-gray-800'
})

const isEnded = computed(() => props.listing.timeRemaining <= 0)
</script>

<template>
  <RouterLink
    :to="{ name: 'listing-detail', params: { id: listing.id } }"
    class="block rounded-lg bg-card shadow-sm hover:shadow-md transition-shadow duration-200 overflow-hidden min-h-touch"
    :aria-label="`View listing: ${listing.title}`"
  >
    <div class="relative aspect-[4/3] overflow-hidden">
      <img
        :src="listing.thumbnailUrl"
        :alt="listing.title"
        class="w-full h-full object-cover"
        loading="lazy"
      />
      <span
        :class="[conditionColor, 'absolute top-2 right-2 px-2 py-0.5 rounded text-xs font-medium']"
      >
        {{ conditionLabel }}
      </span>
    </div>

    <div class="p-3">
      <h3 class="text-text font-medium text-sm truncate">
        {{ listing.title }}
      </h3>

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
