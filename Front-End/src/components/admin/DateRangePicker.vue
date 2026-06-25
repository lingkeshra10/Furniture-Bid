<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import type { AnalyticsQuery } from '@/types/user';

const emit = defineEmits<{
  'date-change': [query: AnalyticsQuery];
}>();

// Calculate default dates (last 30 days)
function getDefaultStartDate(): string {
  const date = new Date();
  date.setDate(date.getDate() - 30);
  return formatDateForInput(date);
}

function getDefaultEndDate(): string {
  return formatDateForInput(new Date());
}

function formatDateForInput(date: Date): string {
  return date.toISOString().split('T')[0];
}

const startDate = ref(getDefaultStartDate());
const endDate = ref(getDefaultEndDate());
const errorMessage = ref('');

// Max range is 12 months
const MAX_RANGE_DAYS = 365;

const isValidRange = computed(() => {
  if (!startDate.value || !endDate.value) return false;

  const start = new Date(startDate.value);
  const end = new Date(endDate.value);

  if (start > end) return false;

  const diffMs = end.getTime() - start.getTime();
  const diffDays = diffMs / (1000 * 60 * 60 * 24);

  return diffDays <= MAX_RANGE_DAYS;
});

const maxStartDate = computed(() => endDate.value);

const minEndDate = computed(() => startDate.value);

const maxEndDate = computed(() => formatDateForInput(new Date()));

function validateAndEmit() {
  errorMessage.value = '';

  if (!startDate.value || !endDate.value) {
    errorMessage.value = 'Both dates are required.';
    return;
  }

  const start = new Date(startDate.value);
  const end = new Date(endDate.value);

  if (start > end) {
    errorMessage.value = 'Start date must be before end date.';
    return;
  }

  const diffMs = end.getTime() - start.getTime();
  const diffDays = diffMs / (1000 * 60 * 60 * 24);

  if (diffDays > MAX_RANGE_DAYS) {
    errorMessage.value = 'Date range cannot exceed 12 months.';
    return;
  }

  emit('date-change', {
    startDate: startDate.value,
    endDate: endDate.value,
  });
}

// Watch for changes and validate
watch([startDate, endDate], () => {
  validateAndEmit();
});

// Emit initial values on mount
onMounted(() => {
  validateAndEmit();
});
</script>

<template>
  <div class="flex flex-col sm:flex-row sm:items-end gap-3">
    <div class="flex-1">
      <label for="analytics-start-date" class="block text-xs font-medium text-gray-600 mb-1">
        Start Date
      </label>
      <input
        id="analytics-start-date"
        v-model="startDate"
        type="date"
        :max="maxStartDate"
        class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary focus:border-primary"
        aria-label="Analytics start date"
      />
    </div>

    <div class="flex-1">
      <label for="analytics-end-date" class="block text-xs font-medium text-gray-600 mb-1">
        End Date
      </label>
      <input
        id="analytics-end-date"
        v-model="endDate"
        type="date"
        :min="minEndDate"
        :max="maxEndDate"
        class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary focus:border-primary"
        aria-label="Analytics end date"
      />
    </div>

    <!-- Error message -->
    <p
      v-if="errorMessage"
      class="text-xs text-red-600 sm:self-center"
      role="alert"
    >
      {{ errorMessage }}
    </p>

    <!-- Valid indicator -->
    <div
      v-else-if="isValidRange"
      class="hidden sm:flex items-center text-xs text-success gap-1 self-center"
    >
      <svg class="w-3.5 h-3.5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M4.5 12.75l6 6 9-13.5" />
      </svg>
      <span>Valid range</span>
    </div>
  </div>
</template>
