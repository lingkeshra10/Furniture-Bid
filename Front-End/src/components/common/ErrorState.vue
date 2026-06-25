<script setup lang="ts">
interface Props {
  message: string;
  retryLabel?: string;
}

withDefaults(defineProps<Props>(), {
  retryLabel: 'Try Again',
});

const emit = defineEmits<{
  retry: [];
}>();
</script>

<template>
  <div class="flex flex-col items-center justify-center gap-4 py-12 px-4 text-center">
    <!-- Error icon -->
    <div class="flex items-center justify-center w-16 h-16 rounded-full bg-red-50">
      <svg
        class="h-8 w-8 text-red-500"
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
        stroke-width="2"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
        />
      </svg>
    </div>

    <!-- Error message -->
    <p class="text-sm text-gray-600 max-w-sm">{{ message }}</p>

    <!-- Retry button -->
    <button
      class="px-6 py-2 rounded-md text-sm font-medium text-white bg-primary hover:bg-primary/90 transition-colors focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 min-w-touch min-h-touch flex items-center justify-center"
      @click="emit('retry')"
    >
      {{ retryLabel }}
    </button>
  </div>
</template>
