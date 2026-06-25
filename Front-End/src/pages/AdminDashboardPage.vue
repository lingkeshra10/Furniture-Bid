<script setup lang="ts">
import { ref } from 'vue';
import UserManagementTable from '@/components/admin/UserManagementTable.vue';
import ListingManagementTable from '@/components/admin/ListingManagementTable.vue';
import ReportedListings from '@/components/admin/ReportedListings.vue';
import AnalyticsSummaryCards from '@/components/admin/AnalyticsSummaryCards.vue';
import AuctionLineChart from '@/components/admin/AuctionLineChart.vue';
import CategoryBarChart from '@/components/admin/CategoryBarChart.vue';
import TopSellersTable from '@/components/admin/TopSellersTable.vue';
import DateRangePicker from '@/components/admin/DateRangePicker.vue';
import ErrorState from '@/components/common/ErrorState.vue';

type AdminTab = 'analytics' | 'users' | 'listings';

const activeTab = ref<AdminTab>('analytics');

// Date range state for analytics
const dateRange = ref<{ startDate: string; endDate: string }>({
  startDate: '',
  endDate: '',
});

// Error states for each section
const analyticsError = ref(false);
const usersError = ref(false);
const listingsError = ref(false);

// Retry key counters to force re-render of child components
const analyticsRetryKey = ref(0);
const usersRetryKey = ref(0);
const listingsRetryKey = ref(0);

function handleDateRangeChange(range: { startDate: string; endDate: string }) {
  dateRange.value = range;
  analyticsError.value = false;
  analyticsRetryKey.value++;
}

function handleAnalyticsError() {
  analyticsError.value = true;
}

function handleUsersError() {
  usersError.value = true;
}

function handleListingsError() {
  listingsError.value = true;
}

function retryAnalytics() {
  analyticsError.value = false;
  analyticsRetryKey.value++;
}

function retryUsers() {
  usersError.value = false;
  usersRetryKey.value++;
}

function retryListings() {
  listingsError.value = false;
  listingsRetryKey.value++;
}

const tabs: { id: AdminTab; label: string }[] = [
  { id: 'analytics', label: 'Analytics' },
  { id: 'users', label: 'Users' },
  { id: 'listings', label: 'Listings' },
];
</script>

<template>
  <div class="min-h-screen bg-background">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
      <!-- Page Header -->
      <div class="mb-6">
        <h1 class="text-2xl font-bold text-text">Admin Dashboard</h1>
        <p class="mt-1 text-sm text-gray-500">
          Manage users, listings, and view platform analytics.
        </p>
      </div>

      <!-- Tab Navigation -->
      <div class="mb-8 border-b border-gray-200">
        <nav class="flex gap-1 -mb-px" aria-label="Admin dashboard tabs">
          <button
            v-for="tab in tabs"
            :key="tab.id"
            class="px-4 py-3 text-sm font-medium transition-colors border-b-2 min-w-touch min-h-touch flex items-center justify-center"
            :class="
              activeTab === tab.id
                ? 'border-primary text-primary'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            "
            :aria-selected="activeTab === tab.id"
            role="tab"
            @click="activeTab = tab.id"
          >
            {{ tab.label }}
          </button>
        </nav>
      </div>

      <!-- Analytics Tab -->
      <div v-show="activeTab === 'analytics'" role="tabpanel" aria-label="Analytics section">
        <!-- Error state for analytics -->
        <ErrorState
          v-if="analyticsError"
          message="Failed to load analytics data. Please try again."
          @retry="retryAnalytics"
        />

        <template v-else>
          <!-- Date Range Picker -->
          <div class="mb-6">
            <DateRangePicker
              :key="'date-picker-' + analyticsRetryKey"
              @change="handleDateRangeChange"
              @error="handleAnalyticsError"
            />
          </div>

          <!-- Summary Cards -->
          <div class="mb-8">
            <AnalyticsSummaryCards
              :key="'summary-' + analyticsRetryKey"
              :start-date="dateRange.startDate"
              :end-date="dateRange.endDate"
              @error="handleAnalyticsError"
            />
          </div>

          <!-- Charts Section -->
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
            <div class="bg-card rounded-lg border border-gray-200 p-4 sm:p-6">
              <h3 class="text-base font-semibold text-text mb-4">Auction Activity</h3>
              <AuctionLineChart
                :key="'line-chart-' + analyticsRetryKey"
                :start-date="dateRange.startDate"
                :end-date="dateRange.endDate"
                @error="handleAnalyticsError"
              />
            </div>

            <div class="bg-card rounded-lg border border-gray-200 p-4 sm:p-6">
              <h3 class="text-base font-semibold text-text mb-4">Listings by Category</h3>
              <CategoryBarChart
                :key="'bar-chart-' + analyticsRetryKey"
                :start-date="dateRange.startDate"
                :end-date="dateRange.endDate"
                @error="handleAnalyticsError"
              />
            </div>
          </div>

          <!-- Top Sellers Table -->
          <div class="bg-card rounded-lg border border-gray-200 p-4 sm:p-6">
            <h3 class="text-base font-semibold text-text mb-4">Top Sellers</h3>
            <TopSellersTable
              :key="'top-sellers-' + analyticsRetryKey"
              :start-date="dateRange.startDate"
              :end-date="dateRange.endDate"
              @error="handleAnalyticsError"
            />
          </div>
        </template>
      </div>

      <!-- Users Tab -->
      <div v-show="activeTab === 'users'" role="tabpanel" aria-label="User management section">
        <!-- Error state for users -->
        <ErrorState
          v-if="usersError"
          message="Failed to load user data. Please try again."
          @retry="retryUsers"
        />

        <template v-else>
          <UserManagementTable
            :key="'users-' + usersRetryKey"
            @error="handleUsersError"
          />
        </template>
      </div>

      <!-- Listings Tab -->
      <div v-show="activeTab === 'listings'" role="tabpanel" aria-label="Listing management section">
        <!-- Error state for listings -->
        <ErrorState
          v-if="listingsError"
          message="Failed to load listing data. Please try again."
          @retry="retryListings"
        />

        <template v-else>
          <!-- Listing Management Table -->
          <div class="mb-10">
            <ListingManagementTable
              :key="'listings-' + listingsRetryKey"
              @error="handleListingsError"
            />
          </div>

          <!-- Reported Listings -->
          <div>
            <ReportedListings
              :key="'reported-' + listingsRetryKey"
              @error="handleListingsError"
            />
          </div>
        </template>
      </div>
    </div>
  </div>
</template>
