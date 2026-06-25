<script setup lang="ts">
import type { TopSeller } from '@/types/user';
import { formatCurrency } from '@/utils/formatters';

defineProps<{
  sellers: TopSeller[];
  isLoading: boolean;
}>();
</script>

<template>
  <div class="bg-card border border-gray-200 rounded-lg p-5">
    <h3 class="text-sm font-semibold text-text mb-4">Top 10 Sellers</h3>

    <!-- Loading state -->
    <div v-if="isLoading" class="space-y-3">
      <div v-for="i in 5" :key="'skeleton-' + i" class="flex items-center gap-3 animate-pulse">
        <div class="w-8 h-8 bg-gray-200 rounded-full"></div>
        <div class="flex-1">
          <div class="h-3 bg-gray-200 rounded w-28 mb-1"></div>
          <div class="h-2 bg-gray-200 rounded w-20"></div>
        </div>
        <div class="h-4 bg-gray-200 rounded w-16"></div>
      </div>
    </div>

    <!-- Empty state -->
    <div v-else-if="sellers.length === 0" class="text-center py-8 text-gray-400 text-sm">
      No seller data available for the selected period.
    </div>

    <!-- Desktop Table (>= 768px) -->
    <div v-else class="hidden md:block overflow-x-auto">
      <table class="w-full text-sm text-left" aria-label="Top sellers table">
        <thead class="bg-gray-50 border-b border-gray-200">
          <tr>
            <th scope="col" class="px-4 py-2.5 font-medium text-gray-600">#</th>
            <th scope="col" class="px-4 py-2.5 font-medium text-gray-600">Seller</th>
            <th scope="col" class="px-4 py-2.5 font-medium text-gray-600 text-right">Completed Auctions</th>
            <th scope="col" class="px-4 py-2.5 font-medium text-gray-600 text-right">Total Revenue</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr
            v-for="(seller, index) in sellers"
            :key="seller.displayName"
            class="hover:bg-gray-50 transition-colors"
          >
            <td class="px-4 py-2.5 text-gray-500 font-medium">{{ index + 1 }}</td>
            <td class="px-4 py-2.5 font-medium text-text">{{ seller.displayName }}</td>
            <td class="px-4 py-2.5 text-gray-600 text-right">{{ seller.completedAuctions }}</td>
            <td class="px-4 py-2.5 text-gray-600 text-right font-medium">{{ formatCurrency(seller.totalRevenue) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Mobile Card View (< 768px) -->
    <div v-if="sellers.length > 0" class="md:hidden space-y-2">
      <div
        v-for="(seller, index) in sellers"
        :key="'mobile-' + seller.displayName"
        class="flex items-center gap-3 py-2.5 border-b border-gray-100 last:border-b-0"
      >
        <div
          class="flex-shrink-0 w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold"
          :class="index < 3 ? 'bg-accent/10 text-accent' : 'bg-gray-100 text-gray-500'"
        >
          {{ index + 1 }}
        </div>
        <div class="flex-1 min-w-0">
          <p class="text-sm font-medium text-text truncate">{{ seller.displayName }}</p>
          <p class="text-xs text-gray-500">{{ seller.completedAuctions }} auctions</p>
        </div>
        <p class="text-sm font-medium text-text">{{ formatCurrency(seller.totalRevenue) }}</p>
      </div>
    </div>
  </div>
</template>
