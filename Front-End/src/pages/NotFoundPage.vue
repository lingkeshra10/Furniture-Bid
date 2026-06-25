<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goToCatalog(): void {
  router.push({ name: 'catalog' })
}
</script>

<template>
  <div class="flex flex-col items-center justify-center min-h-[60vh] px-4 text-center">
    <!-- 404 icon -->
    <div class="flex items-center justify-center w-24 h-24 rounded-full bg-gray-100 mb-6">
      <svg
        class="h-12 w-12 text-gray-400"
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
        stroke-width="1.5"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          d="M9.75 9.75l4.5 4.5m0-4.5l-4.5 4.5M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
        />
      </svg>
    </div>

    <!-- 404 text -->
    <h1 class="text-4xl font-bold text-text mb-2">404</h1>
    <h2 class="text-xl font-semibold text-gray-600 mb-4">Page Not Found</h2>
    <p class="text-sm text-gray-500 max-w-md mb-8">
      The page you're looking for doesn't exist or has been moved. Let's get you back to browsing furniture.
    </p>

    <!-- Back to catalog button -->
    <button
      class="px-6 py-3 text-sm font-medium text-white bg-primary hover:bg-primary/90 rounded-md transition-colors focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 min-w-touch min-h-touch flex items-center justify-center"
      @click="goToCatalog"
    >
      Browse Catalog
    </button>
  </div>
</template>
