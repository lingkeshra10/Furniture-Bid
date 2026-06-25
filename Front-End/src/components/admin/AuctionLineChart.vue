<script setup lang="ts">
import { computed } from 'vue';
import { Line } from 'vue-chartjs';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from 'chart.js';
import type { AuctionTrend } from '@/types/user';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend, Filler);

const props = defineProps<{
  trends: AuctionTrend[];
  isLoading: boolean;
}>();

const chartData = computed(() => {
  const labels = props.trends.map((t) => {
    const date = new Date(t.date);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  });

  return {
    labels,
    datasets: [
      {
        label: 'Auctions Created',
        data: props.trends.map((t) => t.auctionsCreated),
        borderColor: '#8B5E3C',
        backgroundColor: 'rgba(139, 94, 60, 0.1)',
        fill: true,
        tension: 0.3,
        pointRadius: 3,
        pointHoverRadius: 5,
      },
      {
        label: 'Auctions Completed',
        data: props.trends.map((t) => t.auctionsCompleted),
        borderColor: '#D97706',
        backgroundColor: 'rgba(217, 119, 6, 0.1)',
        fill: true,
        tension: 0.3,
        pointRadius: 3,
        pointHoverRadius: 5,
      },
    ],
  };
});

const chartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  interaction: {
    mode: 'index' as const,
    intersect: false,
  },
  plugins: {
    legend: {
      position: 'top' as const,
      labels: {
        usePointStyle: true,
        padding: 16,
        font: { size: 12 },
      },
    },
    tooltip: {
      backgroundColor: '#1F2937',
      titleFont: { size: 13 },
      bodyFont: { size: 12 },
      padding: 10,
      cornerRadius: 6,
    },
  },
  scales: {
    x: {
      grid: { display: false },
      ticks: { font: { size: 11 }, maxRotation: 45 },
    },
    y: {
      beginAtZero: true,
      ticks: {
        font: { size: 11 },
        stepSize: 1,
        precision: 0,
      },
      grid: { color: 'rgba(0, 0, 0, 0.05)' },
    },
  },
}));
</script>

<template>
  <div class="bg-card border border-gray-200 rounded-lg p-5">
    <h3 class="text-sm font-semibold text-text mb-4">Auction Activity</h3>

    <!-- Loading state -->
    <div v-if="isLoading" class="flex items-center justify-center h-64">
      <svg class="animate-spin h-8 w-8 text-primary" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
      </svg>
    </div>

    <!-- Empty state -->
    <div v-else-if="trends.length === 0" class="flex items-center justify-center h-64 text-gray-400 text-sm">
      No trend data available for the selected period.
    </div>

    <!-- Chart -->
    <div v-else class="h-64">
      <Line :data="chartData" :options="chartOptions" />
    </div>
  </div>
</template>
