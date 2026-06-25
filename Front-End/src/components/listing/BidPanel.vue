<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { useAuction } from '@/composables/useAuction';
import { useAuthStore } from '@/stores/auth';
import { formatCurrency } from '@/utils/formatters';
import { BID_INCREMENT } from '@/utils/constants';

const props = defineProps<{
  auctionId: string;
  currentBid: number;
  isAuctionEnded: boolean;
}>();

const authStore = useAuthStore();
const { minimumBid, submitBid, isSubmitting, bidError, bidSuccess } = useAuction(props.auctionId);

const bidAmount = ref<string>(minimumBid.value.toFixed(2));

// Update bid input when minimum bid changes (e.g., due to WebSocket update)
watch(minimumBid, (newMin) => {
  const currentInput = parseFloat(bidAmount.value);
  if (isNaN(currentInput) || currentInput < newMin) {
    bidAmount.value = newMin.toFixed(2);
  }
});

const parsedAmount = computed(() => parseFloat(bidAmount.value));

const isValidAmount = computed(() => {
  const val = parsedAmount.value;
  if (isNaN(val)) return false;
  if (val < 0.01 || val > 999999999.99) return false;
  // Check up to 2 decimal places
  const parts = bidAmount.value.split('.');
  if (parts.length === 2 && parts[1].length > 2) return false;
  return val >= minimumBid.value;
});

const isSubmitDisabled = computed(() => {
  return (
    props.isAuctionEnded ||
    !authStore.isAuthenticated.value ||
    isSubmitting.value ||
    !isValidAmount.value
  );
});

function handleInput(event: Event) {
  const target = event.target as HTMLInputElement;
  // Allow only numeric values with up to 2 decimal places
  const value = target.value;
  const regex = /^\d*\.?\d{0,2}$/;
  if (regex.test(value) || value === '') {
    bidAmount.value = value;
  } else {
    target.value = bidAmount.value;
  }
}

async function handleSubmit() {
  const amount = parsedAmount.value;
  if (isNaN(amount)) return;

  const success = await submitBid(amount);
  if (success) {
    // Update the input to the next minimum bid
    bidAmount.value = minimumBid.value.toFixed(2);
  }
  // On failure, keep user's amount in the input
}
</script>

<template>
  <div class="bg-card rounded-lg border border-gray-200 p-4 md:p-6 shadow-sm">
    <!-- Auction Ended Banner -->
    <div
      v-if="isAuctionEnded"
      class="mb-4 rounded-md bg-gray-100 border border-gray-300 px-4 py-3 text-center"
      role="alert"
    >
      <span class="text-sm font-semibold text-gray-600">Auction Ended</span>
    </div>

    <!-- Current Highest Bid -->
    <div class="mb-4 text-center">
      <p class="text-sm text-gray-500 font-medium">Current Highest Bid</p>
      <p class="text-2xl md:text-3xl font-bold text-primary mt-1">
        {{ formatCurrency(currentBid) }}
      </p>
    </div>

    <!-- Login Prompt -->
    <div
      v-if="!authStore.isAuthenticated.value && !isAuctionEnded"
      class="mb-4 rounded-md bg-amber-50 border border-amber-200 px-4 py-3 text-center"
      role="status"
    >
      <span class="text-sm text-amber-700">Login to bid</span>
    </div>

    <!-- Bid Form -->
    <div v-if="!isAuctionEnded" class="space-y-3">
      <div>
        <label for="bid-amount" class="block text-sm font-medium text-text mb-1">
          Your Bid (min {{ formatCurrency(minimumBid) }})
        </label>
        <div class="relative">
          <span class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500 text-sm">$</span>
          <input
            id="bid-amount"
            type="text"
            inputmode="decimal"
            :value="bidAmount"
            @input="handleInput"
            :disabled="!authStore.isAuthenticated.value || isSubmitting"
            class="w-full min-h-touch pl-7 pr-3 py-3 border border-gray-300 rounded-md text-text focus:outline-none focus:ring-2 focus:ring-primary focus:border-primary disabled:bg-gray-100 disabled:cursor-not-allowed"
            placeholder="0.00"
            aria-describedby="bid-feedback"
          />
        </div>
      </div>

      <button
        type="button"
        @click="handleSubmit"
        :disabled="isSubmitDisabled"
        class="w-full min-h-touch px-4 py-3 bg-primary text-white font-semibold rounded-md hover:bg-primary/90 focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
      >
        <span v-if="isSubmitting">Placing Bid...</span>
        <span v-else>Place Bid</span>
      </button>

      <!-- Success/Error Messages -->
      <div id="bid-feedback" aria-live="polite">
        <p v-if="bidSuccess" class="text-sm text-success font-medium mt-2">
          {{ bidSuccess }}
        </p>
        <p v-if="bidError" class="text-sm text-red-600 font-medium mt-2">
          {{ bidError }}
        </p>
      </div>
    </div>
  </div>
</template>
