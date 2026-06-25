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
const { autoBidConfig, toggleAutoBid, isAutoBidActive, minimumBid } = useAuction(props.auctionId);

const maxAmount = ref<string>(minimumBid.value.toFixed(2));
const isProcessing = ref(false);

// Update max amount input when minimum bid changes
watch(minimumBid, (newMin) => {
  if (!isAutoBidActive.value) {
    const current = parseFloat(maxAmount.value);
    if (isNaN(current) || current < newMin) {
      maxAmount.value = newMin.toFixed(2);
    }
  }
});

const parsedMaxAmount = computed(() => parseFloat(maxAmount.value));

const isValidMaxAmount = computed(() => {
  const val = parsedMaxAmount.value;
  if (isNaN(val)) return false;
  if (val > 999999999.99) return false;
  const parts = maxAmount.value.split('.');
  if (parts.length === 2 && parts[1].length > 2) return false;
  return val >= minimumBid.value;
});

const isDisabled = computed(() => {
  return props.isAuctionEnded || !authStore.isAuthenticated.value;
});

const isToggleDisabled = computed(() => {
  if (isDisabled.value || isProcessing.value) return true;
  // When activating, need valid amount
  if (!isAutoBidActive.value && !isValidMaxAmount.value) return true;
  return false;
});

function handleInput(event: Event) {
  const target = event.target as HTMLInputElement;
  const value = target.value;
  const regex = /^\d*\.?\d{0,2}$/;
  if (regex.test(value) || value === '') {
    maxAmount.value = value;
  } else {
    target.value = maxAmount.value;
  }
}

async function handleToggle() {
  if (isToggleDisabled.value) return;

  isProcessing.value = true;
  try {
    await toggleAutoBid(parsedMaxAmount.value);
  } finally {
    isProcessing.value = false;
  }
}
</script>

<template>
  <div class="bg-card rounded-lg border border-gray-200 p-4 md:p-6 shadow-sm">
    <h3 class="text-lg font-semibold text-text mb-3">Auto-Bid</h3>

    <!-- Status Display -->
    <div
      v-if="isAutoBidActive"
      class="mb-4 rounded-md bg-green-50 border border-green-200 px-4 py-3"
      role="status"
    >
      <div class="flex items-center gap-2">
        <span class="inline-block w-2 h-2 rounded-full bg-success"></span>
        <span class="text-sm font-medium text-green-800">Auto-Bid Active</span>
      </div>
      <p class="text-xs text-green-700 mt-1">
        Max amount: {{ formatCurrency(autoBidConfig?.maxAmount ?? 0) }}
      </p>
    </div>

    <div v-else-if="!isAuctionEnded && authStore.isAuthenticated.value" class="mb-4">
      <p class="text-sm text-gray-500">
        Set a maximum amount and let the system bid automatically for you.
      </p>
    </div>

    <!-- Disabled state messages -->
    <div v-if="isAuctionEnded" class="text-center py-2">
      <p class="text-sm text-gray-500">Auto-bid unavailable — auction has ended.</p>
    </div>

    <div v-else-if="!authStore.isAuthenticated.value" class="text-center py-2">
      <p class="text-sm text-gray-500">Login to use auto-bid.</p>
    </div>

    <!-- Auto-Bid Form -->
    <div v-else class="space-y-3">
      <div>
        <label for="auto-bid-max" class="block text-sm font-medium text-text mb-1">
          Maximum Amount (min {{ formatCurrency(minimumBid) }})
        </label>
        <div class="relative">
          <span class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500 text-sm">$</span>
          <input
            id="auto-bid-max"
            type="text"
            inputmode="decimal"
            :value="maxAmount"
            @input="handleInput"
            :disabled="isAutoBidActive || isProcessing"
            class="w-full min-h-touch pl-7 pr-3 py-3 border border-gray-300 rounded-md text-text focus:outline-none focus:ring-2 focus:ring-primary focus:border-primary disabled:bg-gray-100 disabled:cursor-not-allowed"
            placeholder="0.00"
          />
        </div>
      </div>

      <button
        type="button"
        @click="handleToggle"
        :disabled="isToggleDisabled"
        class="w-full min-h-touch px-4 py-3 font-semibold rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        :class="
          isAutoBidActive
            ? 'bg-red-600 text-white hover:bg-red-700 focus:ring-red-600'
            : 'bg-accent text-white hover:bg-accent/90 focus:ring-accent'
        "
      >
        <span v-if="isProcessing">Processing...</span>
        <span v-else-if="isAutoBidActive">Deactivate Auto-Bid</span>
        <span v-else>Activate Auto-Bid</span>
      </button>
    </div>
  </div>
</template>
