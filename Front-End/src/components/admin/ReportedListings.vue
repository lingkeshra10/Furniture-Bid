<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import type { ListingReport } from '@/types/user';
import { adminService } from '@/services/api/adminService';
import { useToast } from '@/composables/useToast';
import { formatDateTime } from '@/utils/formatters';
import { PAGE_SIZE } from '@/utils/constants';
import PaginationControls from '@/components/common/PaginationControls.vue';

const { showToast } = useToast();

// State
const reports = ref<ListingReport[]>([]);
const currentPage = ref(1);
const totalReports = ref(0);
const isLoading = ref(false);

// Computed
const totalPages = computed(() => {
  return Math.max(1, Math.ceil(totalReports.value / PAGE_SIZE));
});

const sortedReports = computed(() => {
  return [...reports.value].sort(
    (a, b) => new Date(b.reportDate).getTime() - new Date(a.reportDate).getTime()
  );
});

// Methods
async function fetchReports() {
  isLoading.value = true;
  try {
    const response = await adminService.getReportedListings(currentPage.value, PAGE_SIZE);
    reports.value = response.data;
    totalReports.value = response.total;
  } catch (error) {
    showToast('Failed to load reported listings. Please try again.', 'error');
  } finally {
    isLoading.value = false;
  }
}

function handlePageChange(page: number) {
  currentPage.value = page;
  fetchReports();
}

onMounted(() => {
  fetchReports();
});
</script>

<template>
  <div class="space-y-4">
    <!-- Header -->
    <h2 class="text-lg font-semibold text-text">Reported Listings</h2>

    <!-- Loading State -->
    <div v-if="isLoading" class="flex items-center justify-center py-12">
      <svg class="animate-spin h-8 w-8 text-primary" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
      </svg>
    </div>

    <!-- Content when not loading -->
    <template v-else-if="sortedReports.length > 0">
      <!-- Desktop Table View (>= 768px) -->
      <div class="hidden md:block overflow-x-auto">
        <table class="w-full text-sm text-left" aria-label="Reported listings table">
          <thead class="bg-gray-50 border-b border-gray-200">
            <tr>
              <th scope="col" class="px-4 py-3 font-medium text-gray-600">Report Reason</th>
              <th scope="col" class="px-4 py-3 font-medium text-gray-600">Reporter</th>
              <th scope="col" class="px-4 py-3 font-medium text-gray-600">Date</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr
              v-for="report in sortedReports"
              :key="report.id"
              class="hover:bg-gray-50 transition-colors"
            >
              <td class="px-4 py-3 text-text max-w-[300px]">
                <p class="line-clamp-2">{{ report.reason }}</p>
              </td>
              <td class="px-4 py-3 text-gray-600">{{ report.reporterDisplayName }}</td>
              <td class="px-4 py-3 text-gray-600 whitespace-nowrap">{{ formatDateTime(report.reportDate) }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Mobile Card View (< 768px) - Stacked Cards -->
      <div class="md:hidden space-y-3">
        <div
          v-for="report in sortedReports"
          :key="'mobile-' + report.id"
          class="bg-card border border-gray-200 rounded-lg p-4 space-y-2"
        >
          <p class="text-sm font-medium text-text">{{ report.reason }}</p>
          <div class="flex items-center justify-between text-xs text-gray-500">
            <span>Reported by: <span class="font-medium text-gray-700">{{ report.reporterDisplayName }}</span></span>
            <span>{{ formatDateTime(report.reportDate) }}</span>
          </div>
        </div>
      </div>
    </template>

    <!-- Empty State -->
    <div v-else-if="!isLoading" class="text-center py-12">
      <svg class="mx-auto h-12 w-12 text-gray-300" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
        <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z" />
      </svg>
      <p class="mt-3 text-sm text-gray-500">No reported listings found.</p>
    </div>

    <!-- Pagination -->
    <div v-if="!isLoading && totalPages > 1" class="pt-4">
      <PaginationControls
        :current-page="currentPage"
        :total-pages="totalPages"
        @page-change="handlePageChange"
      />
    </div>
  </div>
</template>
