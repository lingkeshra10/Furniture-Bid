<script setup lang="ts">
import { computed } from 'vue';
import type { AnalyticsSummary } from '@/types/user';
import { formatCurrency } from '@/utils/formatters';

const props = defineProps<{
  summary: AnalyticsSummary | null;
  isLoading: boolean;
}>();

const cards = computed(() => {
  if (!props.summary) return [];
  return [
    {
      label: 'Total Users',
      value: props.summary.totalUsers.toLocaleString(),
      icon: 'users',
      color: 'text-primary',
      bgColor: 'bg-primary/10',
    },
    {
      label: 'Active Auctions',
      value: props.summary.activeAuctions.toLocaleString(),
      icon: 'auction',
      color: 'text-accent',
      bgColor: 'bg-accent/10',
    },
    {
      label: 'Completed Auctions',
      value: props.summary.completedAuctions.toLocaleString(),
      icon: 'check',
      color: 'text-success',
      bgColor: 'bg-success/10',
    },
    {
      label: 'Total Revenue',
      value: formatCurrency(props.summary.totalRevenue),
      icon: 'revenue',
      color: 'text-secondary',
      bgColor: 'bg-secondary/10',
    },
  ];
});
</script>

<template>
  <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
    <!-- Loading skeleton -->
    <template v-if="isLoading">
      <div
        v-for="i in 4"
        :key="'skeleton-' + i"
        class="bg-card border border-gray-200 rounded-lg p-5 animate-pulse"
      >
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 bg-gray-200 rounded-lg"></div>
          <div class="flex-1">
            <div class="h-3 bg-gray-200 rounded w-20 mb-2"></div>
            <div class="h-6 bg-gray-200 rounded w-16"></div>
          </div>
        </div>
      </div>
    </template>

    <!-- Cards -->
    <template v-else>
      <div
        v-for="card in cards"
        :key="card.label"
        class="bg-card border border-gray-200 rounded-lg p-5 transition-shadow hover:shadow-md"
      >
        <div class="flex items-center gap-3">
          <div class="flex-shrink-0 w-10 h-10 rounded-lg flex items-center justify-center" :class="card.bgColor">
            <!-- Users icon -->
            <svg v-if="card.icon === 'users'" class="w-5 h-5" :class="card.color" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M15 19.128a9.38 9.38 0 002.625.372 9.337 9.337 0 004.121-.952 4.125 4.125 0 00-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 018.624 21c-2.331 0-4.512-.645-6.374-1.766l-.001-.109a6.375 6.375 0 0111.964-3.07M12 6.375a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0zm8.25 2.25a2.625 2.625 0 11-5.25 0 2.625 2.625 0 015.25 0z" />
            </svg>
            <!-- Auction icon -->
            <svg v-else-if="card.icon === 'auction'" class="w-5 h-5" :class="card.color" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <!-- Check icon -->
            <svg v-else-if="card.icon === 'check'" class="w-5 h-5" :class="card.color" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <!-- Revenue icon -->
            <svg v-else-if="card.icon === 'revenue'" class="w-5 h-5" :class="card.color" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v12m-3-2.818l.879.659c1.171.879 3.07.879 4.242 0 1.172-.879 1.172-2.303 0-3.182C13.536 12.219 12.768 12 12 12c-.725 0-1.45-.22-2.003-.659-1.106-.879-1.106-2.303 0-3.182s2.9-.879 4.006 0l.415.33M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-xs text-gray-500 font-medium uppercase tracking-wide">{{ card.label }}</p>
            <p class="text-lg font-bold text-text truncate" :title="card.value">{{ card.value }}</p>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
