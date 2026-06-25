<script setup lang="ts">
import { computed } from 'vue'
import type { FurnitureListing } from '@/types/furniture'
import { formatCurrency } from '@/utils/formatters'

const props = defineProps<{
  listing: FurnitureListing
}>()

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

const categoryLabel = computed(() => {
  const labels: Record<string, string> = {
    'sofa': 'Sofa',
    'dining-table': 'Dining Table',
    'office-chair': 'Office Chair',
    'wardrobe': 'Wardrobe',
    'bed-frame': 'Bed Frame',
    'coffee-table': 'Coffee Table',
    'cabinet': 'Cabinet',
    'bookshelf': 'Bookshelf',
  }
  return labels[props.listing.category] || props.listing.category
})

const formattedDimensions = computed(() => {
  const { width, height, length } = props.listing.dimensions
  return `${width} × ${height} × ${length} cm`
})

const formattedWeight = computed(() => {
  if (props.listing.weight == null) return null
  return `${props.listing.weight} kg`
})

const formattedStartingPrice = computed(() => formatCurrency(props.listing.startingPrice))
const formattedCurrentBid = computed(() => formatCurrency(props.listing.currentBid))
</script>

<template>
  <div class="space-y-5">
    <!-- Title -->
    <h1 class="text-2xl font-bold text-text">{{ listing.title }}</h1>

    <!-- Description -->
    <p class="text-sm text-gray-700 leading-relaxed whitespace-pre-line">{{ listing.description }}</p>

    <!-- Details grid -->
    <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
      <!-- Category -->
      <div class="flex flex-col">
        <span class="text-xs text-gray-500 uppercase tracking-wide">Category</span>
        <span class="text-sm font-medium text-text">{{ categoryLabel }}</span>
      </div>

      <!-- Condition -->
      <div class="flex flex-col">
        <span class="text-xs text-gray-500 uppercase tracking-wide">Condition</span>
        <span class="text-sm font-medium text-text">{{ conditionLabel }}</span>
      </div>

      <!-- Brand (optional) -->
      <div v-if="listing.brand" class="flex flex-col">
        <span class="text-xs text-gray-500 uppercase tracking-wide">Brand</span>
        <span class="text-sm font-medium text-text">{{ listing.brand }}</span>
      </div>

      <!-- Material (optional) -->
      <div v-if="listing.material" class="flex flex-col">
        <span class="text-xs text-gray-500 uppercase tracking-wide">Material</span>
        <span class="text-sm font-medium text-text">{{ listing.material }}</span>
      </div>

      <!-- Dimensions -->
      <div class="flex flex-col">
        <span class="text-xs text-gray-500 uppercase tracking-wide">Dimensions (W × H × L)</span>
        <span class="text-sm font-medium text-text">{{ formattedDimensions }}</span>
      </div>

      <!-- Weight (optional) -->
      <div v-if="formattedWeight" class="flex flex-col">
        <span class="text-xs text-gray-500 uppercase tracking-wide">Weight</span>
        <span class="text-sm font-medium text-text">{{ formattedWeight }}</span>
      </div>

      <!-- Location (optional) -->
      <div v-if="listing.location" class="flex flex-col">
        <span class="text-xs text-gray-500 uppercase tracking-wide">Location</span>
        <span class="text-sm font-medium text-text">{{ listing.location }}</span>
      </div>

      <!-- Starting Price -->
      <div class="flex flex-col">
        <span class="text-xs text-gray-500 uppercase tracking-wide">Starting Price</span>
        <span class="text-sm font-medium text-text">{{ formattedStartingPrice }}</span>
      </div>

      <!-- Current Bid -->
      <div class="flex flex-col">
        <span class="text-xs text-gray-500 uppercase tracking-wide">Current Bid</span>
        <span class="text-sm font-semibold text-primary">{{ formattedCurrentBid }}</span>
      </div>

      <!-- Bid Count -->
      <div class="flex flex-col">
        <span class="text-xs text-gray-500 uppercase tracking-wide">Total Bids</span>
        <span class="text-sm font-medium text-text">{{ listing.bidCount }}</span>
      </div>
    </div>
  </div>
</template>
