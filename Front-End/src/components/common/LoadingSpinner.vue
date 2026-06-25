<script setup lang="ts">
interface Props {
  size?: 'sm' | 'md' | 'lg';
  message?: string;
}

withDefaults(defineProps<Props>(), {
  size: 'md',
  message: undefined,
});

const sizeClasses = {
  sm: 'h-5 w-5',
  md: 'h-8 w-8',
  lg: 'h-12 w-12',
};
</script>

<template>
  <div class="flex flex-col items-center justify-center gap-3 py-8" role="status" aria-live="polite">
    <svg
      class="animate-spin text-primary"
      :class="sizeClasses[size]"
      xmlns="http://www.w3.org/2000/svg"
      fill="none"
      viewBox="0 0 24 24"
    >
      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
    </svg>
    <p v-if="message" class="text-sm text-gray-600 font-medium">{{ message }}</p>
    <span class="sr-only">Loading</span>
  </div>
</template>
