<script setup lang="ts">
import { computed } from 'vue';
import { Bar } from 'vue-chartjs';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';
import type { CategoryDistribution } from '@/types/user';

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

const props = defineProps<{
  distribution: CategoryDistribution[];
  isLoading: boolean;
}>();

function formatCategoryLabel(category: string): string {
  return category
    .split('-')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
}

const chartData = computed(() => {
  const labels = props.distribution.map((d) => formatCategoryLabel(d.category));
  const data = props.distribution.map((d) => d.count);

  return {
    labels,
    datasets: [
      {
        label: 'Listings',
        data,
        backgroundColor: [
          '#8B5E3C',
          '#C19A6B',
          '#D97706',
          '#16A34A',
          '#2563EB',
          '#7C3AED',
          '#DC2626',
          '#0891B2',
        ],
        borderRadius: 4,
        barThickness: 32,
      },
    ],
  };
});

const chartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
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
      ticks: { font: { size: 11 }, maxRotation: 45, minRotation: 0 },
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
    <h3 class="text-sm font-semibold text-text mb-4">Listings by Category</h3>

    <!-- Loading state -->
    <div v-if="isLoading" class="flex items-center justify-center h-64">
      <svg class="animate-spin h-8 w-8 text-primary" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
      </svg>
    </div>

    <!-- Empty state -->
    <div v-else-if="distribution.length === 0" class="flex items-center justify-center h-64 text-gray-400 text-sm">
      No category data available for the selected period.
    </div>

    <!-- Chart -->
    <div v-else class="h-64">
      <Bar :data="chartData" :options="chartOptions" />
    </div>
  </div>
</template>
