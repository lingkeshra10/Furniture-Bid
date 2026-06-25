<script setup lang="ts">
import { computed } from 'vue';
import { formatCurrency, formatCountdown } from '@/utils/formatters';
import type { SellerActiveListing, SellerCompletedAuction } from '@/types/auction';

type ListingType = 'active' | 'completed';

interface Props {
  listing: SellerActiveListing | SellerCompletedAuction;
  type: ListingType;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  click: [id: string];
}>();

const isActive = computed(() => props.type === 'active');

const activeListing = computed(() => props.listing as SellerActiveListing);
const completedListing = computed(() => props.listing as SellerCompletedAuction);

const formattedBid = computed(() => {
  if (isActive.value) {
    return formatCurrency(activeListing.value.currentBid);
  }
  return formatCurrency(completedListing.value.winningBid);
});

const timeRemainingText = computed(() => {
  if (!isActive.value) return '';
  return formatCountdown(activeListing.value.timeRemaining);
});

function handleClick() {
  emit('click', props.listing.id);
}
</script>

<template>
  <div
    class="flex flex-col sm:flex-row sm:items-center gap-3 sm:gap-4 p-4 bg-card rounded-lg border border-gray-200 hover:border-primary/50 hover:shadow-sm transition-all cursor-pointer min-h-touch"
    role="button"
    tabindex="0"
    :aria-label="`View listing: ${listing.title}`"
    @click="handleClick"
    @keydown.enter="handleClick"
    @keydown.space.prevent="handleClick"
  >
    <!-- Title -->
    <div class="flex-1 min-w-0">
      <h3 class="text-sm font-medium text-text truncate">{{ listing.title }}</h3>
    </div>

    <!-- Active listing details -->
    <template v-if="isActive">
      <div class="flex flex-wrap items-center gap-3 sm:gap-6 text-sm">
        <!-- Current Bid -->
        <div class="flex flex-col">
          <span class="text-xs text-gray-500">Current Bid</span>
          <span class="font-semibold text-primary">{{ formattedBid }}</span>
        </div>

        <!-- Bid Count -->
        <div class="flex flex-col">
          <span class="text-xs text-gray-500">Bids</span>
          <span class="font-medium text-text">{{ activeListing.bidCount }}</span>
        </div>

        <!-- Time Remaining -->
        <div class="flex flex-col">
          <span class="text-xs text-gray-500">Time Left</span>
          <span class="font-medium text-accent">{{ timeRemainingText }}</span>
        </div>
      </div>
    </template>

    <!-- Completed listing details -->
    <template v-else>
      <div class="flex flex-wrap items-center gap-3 sm:gap-6 text-sm">
        <!-- Winning Bid -->
        <div class="flex flex-col">
          <span class="text-xs text-gray-500">Winning Bid</span>
          <span class="font-semibold text-primary">{{ formattedBid }}</span>
        </div>

        <!-- Winner Name -->
        <div class="flex flex-col">
          <span class="text-xs text-gray-500">Winner</span>
          <span class="font-medium text-text">{{ completedListing.winnerDisplayName }}</span>
        </div>

        <!-- Reserve Met Status -->
        <div class="flex flex-col">
          <span class="text-xs text-gray-500">Reserve</span>
          <span
            class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium"
            :class="completedListing.reserveMet
              ? 'bg-green-100 text-success'
              : 'bg-red-100 text-red-600'"
          >
            {{ completedListing.reserveMet ? 'Met' : 'Not Met' }}
          </span>
        </div>
      </div>
    </template>
  </div>
</template>
