<script setup lang="ts">
import { computed } from 'vue';

interface Props {
  currentPage: number;
  totalPages: number;
  disabled?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
});

const emit = defineEmits<{
  'page-change': [pageNumber: number];
}>();

const canGoPrevious = computed(() => props.currentPage > 1 && !props.disabled);
const canGoNext = computed(() => props.currentPage < props.totalPages && !props.disabled);

function goToPrevious() {
  if (canGoPrevious.value) {
    emit('page-change', props.currentPage - 1);
  }
}

function goToNext() {
  if (canGoNext.value) {
    emit('page-change', props.currentPage + 1);
  }
}
</script>

<template>
  <nav class="flex items-center justify-center gap-2" aria-label="Pagination">
    <!-- Previous button -->
    <button
      class="px-4 py-2 rounded-md text-sm font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 min-w-touch min-h-touch flex items-center justify-center"
      :class="
        canGoPrevious
          ? 'text-text bg-gray-100 hover:bg-gray-200'
          : 'text-gray-300 bg-gray-50 cursor-not-allowed'
      "
      :disabled="!canGoPrevious"
      aria-label="Previous page"
      @click="goToPrevious"
    >
      <svg class="h-4 w-4 mr-1" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7" />
      </svg>
      Previous
    </button>

    <!-- Page indicator -->
    <span class="text-sm text-gray-600 px-3 py-2">
      Page {{ currentPage }} of {{ totalPages }}
    </span>

    <!-- Next button -->
    <button
      class="px-4 py-2 rounded-md text-sm font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 min-w-touch min-h-touch flex items-center justify-center"
      :class="
        canGoNext
          ? 'text-text bg-gray-100 hover:bg-gray-200'
          : 'text-gray-300 bg-gray-50 cursor-not-allowed'
      "
      :disabled="!canGoNext"
      aria-label="Next page"
      @click="goToNext"
    >
      Next
      <svg class="h-4 w-4 ml-1" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7" />
      </svg>
    </button>
  </nav>
</template>
