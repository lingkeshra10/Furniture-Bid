<script setup lang="ts">
import type { FurnitureListingSummary } from '@/types/furniture'
import ListingCard from './ListingCard.vue'

defineProps<{
  listings: FurnitureListingSummary[]
}>()
</script>

<template>
  <div
    class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4"
  >
    <ListingCard
      v-for="listing in listings"
      :key="listing.id"
      :listing="listing"
    />
  </div>
</template>
