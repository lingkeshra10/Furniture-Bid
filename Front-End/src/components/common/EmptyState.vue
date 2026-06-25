<script setup lang="ts">
interface Props {
  title: string;
  message: string;
}

defineProps<Props>();
</script>

<template>
  <div class="flex flex-col items-center justify-center gap-4 py-12 px-4 text-center">
    <!-- Empty state icon -->
    <div class="flex items-center justify-center w-16 h-16 rounded-full bg-gray-100">
      <svg
        class="h-8 w-8 text-gray-400"
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
        stroke-width="1.5"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-2.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"
        />
      </svg>
    </div>

    <!-- Title -->
    <h3 class="text-lg font-semibold text-text">{{ title }}</h3>

    <!-- Guidance message -->
    <p class="text-sm text-gray-500 max-w-sm">{{ message }}</p>
  </div>
</template>
