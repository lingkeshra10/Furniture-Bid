<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import type { AdminListingRow } from '@/types/user';
import type { ListingStatus } from '@/types/furniture';
import { adminService } from '@/services/api/adminService';
import { useToast } from '@/composables/useToast';
import { formatCurrency } from '@/utils/formatters';
import { PAGE_SIZE } from '@/utils/constants';
import PaginationControls from '@/components/common/PaginationControls.vue';
import AppModal from '@/components/common/AppModal.vue';

const { showToast } = useToast();

// State
const listings = ref<AdminListingRow[]>([]);
const searchQuery = ref('');
const currentPage = ref(1);
const totalListings = ref(0);
const isLoading = ref(false);
const actionLoading = ref<string | null>(null);

// Remove confirmation modal
const showRemoveModal = ref(false);
const listingToRemove = ref<AdminListingRow | null>(null);

// Computed
const filteredListings = computed(() => {
  if (!searchQuery.value.trim()) return listings.value;
  const query = searchQuery.value.toLowerCase().trim();
  return listings.value.filter(
    (listing) =>
      listing.title.toLowerCase().includes(query) ||
      listing.sellerDisplayName.toLowerCase().includes(query)
  );
});

const totalPages = computed(() => {
  const total = searchQuery.value.trim() ? filteredListings.value.length : totalListings.value;
  return Math.max(1, Math.ceil(total / PAGE_SIZE));
});

const paginatedListings = computed(() => {
  if (searchQuery.value.trim()) {
    const start = (currentPage.value - 1) * PAGE_SIZE;
    return filteredListings.value.slice(start, start + PAGE_SIZE);
  }
  return listings.value;
});

// Methods
async function fetchListings() {
  isLoading.value = true;
  try {
    const response = await adminService.getListings(currentPage.value, PAGE_SIZE);
    listings.value = response.data;
    totalListings.value = response.total;
  } catch (error) {
    showToast('Failed to load listings. Please try again.', 'error');
  } finally {
    isLoading.value = false;
  }
}

function confirmRemove(listing: AdminListingRow) {
  listingToRemove.value = listing;
  showRemoveModal.value = true;
}

async function handleRemoveConfirm() {
  if (!listingToRemove.value) return;

  const listing = listingToRemove.value;
  showRemoveModal.value = false;
  actionLoading.value = listing.id;

  try {
    await adminService.removeListing(listing.id);
    listings.value = listings.value.filter((l) => l.id !== listing.id);
    totalListings.value -= 1;
    showToast(`"${listing.title}" has been removed.`, 'success');
  } catch (error) {
    const message = error instanceof Error ? error.message : 'Failed to remove listing.';
    showToast(message, 'error');
  } finally {
    actionLoading.value = null;
    listingToRemove.value = null;
  }
}

function handleRemoveCancel() {
  showRemoveModal.value = false;
  listingToRemove.value = null;
}

async function flagListing(listing: AdminListingRow) {
  actionLoading.value = listing.id;
  try {
    await adminService.flagListing(listing.id);
    listing.status = 'flagged';
    showToast(`"${listing.title}" has been flagged for review.`, 'success');
  } catch (error) {
    const message = error instanceof Error ? error.message : 'Failed to flag listing.';
    showToast(message, 'error');
  } finally {
    actionLoading.value = null;
  }
}

function handlePageChange(page: number) {
  currentPage.value = page;
}

function getStatusBadgeClasses(status: ListingStatus): string {
  switch (status) {
    case 'active':
      return 'bg-green-100 text-green-800';
    case 'flagged':
      return 'bg-amber-100 text-amber-800';
    case 'removed':
      return 'bg-red-100 text-red-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
}

// Reset to page 1 when search changes
watch(searchQuery, () => {
  currentPage.value = 1;
});

// Fetch when page changes (only for non-search mode)
watch(currentPage, () => {
  if (!searchQuery.value.trim()) {
    fetchListings();
  }
});

onMounted(() => {
  fetchListings();
});
</script>

<template>
  <div class="space-y-4">
    <!-- Header and Search -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
      <h2 class="text-lg font-semibold text-text">Listing Management</h2>
      <div class="relative w-full sm:w-72">
        <svg
          class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          stroke-width="2"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
          />
        </svg>
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Search by title or seller..."
          class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary focus:border-primary"
          aria-label="Search listings by title or seller name"
        />
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="isLoading" class="flex items-center justify-center py-12">
      <svg class="animate-spin h-8 w-8 text-primary" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
      </svg>
    </div>

    <!-- Content when not loading -->
    <template v-else-if="paginatedListings.length > 0">
      <!-- Desktop Table View (>= 768px) -->
      <div class="hidden md:block overflow-x-auto">
        <table class="w-full text-sm text-left" aria-label="Listing management table">
          <thead class="bg-gray-50 border-b border-gray-200">
            <tr>
              <th scope="col" class="px-4 py-3 font-medium text-gray-600">Title</th>
              <th scope="col" class="px-4 py-3 font-medium text-gray-600">Seller</th>
              <th scope="col" class="px-4 py-3 font-medium text-gray-600">Status</th>
              <th scope="col" class="px-4 py-3 font-medium text-gray-600">Current Bid</th>
              <th scope="col" class="px-4 py-3 font-medium text-gray-600">Reports</th>
              <th scope="col" class="px-4 py-3 font-medium text-gray-600">Actions</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr
              v-for="listing in paginatedListings"
              :key="listing.id"
              class="hover:bg-gray-50 transition-colors"
            >
              <td class="px-4 py-3 font-medium text-text max-w-[200px] truncate">{{ listing.title }}</td>
              <td class="px-4 py-3 text-gray-600">{{ listing.sellerDisplayName }}</td>
              <td class="px-4 py-3">
                <span
                  class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium capitalize"
                  :class="getStatusBadgeClasses(listing.status)"
                >
                  {{ listing.status }}
                </span>
              </td>
              <td class="px-4 py-3 text-gray-600">{{ formatCurrency(listing.currentBid) }}</td>
              <td class="px-4 py-3">
                <span
                  class="inline-flex items-center justify-center min-w-[24px] px-1.5 py-0.5 rounded-full text-xs font-medium"
                  :class="listing.reportCount > 0 ? 'bg-red-100 text-red-700' : 'bg-gray-100 text-gray-600'"
                >
                  {{ listing.reportCount }}
                </span>
              </td>
              <td class="px-4 py-3">
                <div class="flex items-center gap-2">
                  <button
                    v-if="listing.status !== 'flagged'"
                    class="px-3 py-1.5 text-xs font-medium text-amber-700 bg-amber-50 hover:bg-amber-100 rounded transition-colors min-w-touch min-h-touch flex items-center justify-center disabled:opacity-50 disabled:cursor-not-allowed"
                    :disabled="actionLoading === listing.id"
                    :aria-label="`Flag ${listing.title} for review`"
                    @click="flagListing(listing)"
                  >
                    Flag
                  </button>
                  <button
                    v-if="listing.status !== 'removed'"
                    class="px-3 py-1.5 text-xs font-medium text-red-700 bg-red-50 hover:bg-red-100 rounded transition-colors min-w-touch min-h-touch flex items-center justify-center disabled:opacity-50 disabled:cursor-not-allowed"
                    :disabled="actionLoading === listing.id"
                    :aria-label="`Remove ${listing.title}`"
                    @click="confirmRemove(listing)"
                  >
                    Remove
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Mobile Card View (< 768px) - Stacked Cards -->
      <div class="md:hidden space-y-3">
        <div
          v-for="listing in paginatedListings"
          :key="'mobile-' + listing.id"
          class="bg-card border border-gray-200 rounded-lg p-4 space-y-3"
        >
          <div class="flex items-start justify-between gap-2">
            <div class="min-w-0">
              <p class="font-medium text-text truncate">{{ listing.title }}</p>
              <p class="text-sm text-gray-500">{{ listing.sellerDisplayName }}</p>
            </div>
            <span
              class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium capitalize shrink-0"
              :class="getStatusBadgeClasses(listing.status)"
            >
              {{ listing.status }}
            </span>
          </div>

          <div class="flex items-center gap-4 text-sm text-gray-600">
            <span class="font-medium">{{ formatCurrency(listing.currentBid) }}</span>
            <span class="flex items-center gap-1">
              <svg class="h-3.5 w-3.5 text-gray-400" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126z" />
              </svg>
              <span :class="listing.reportCount > 0 ? 'text-red-600 font-medium' : ''">
                {{ listing.reportCount }} {{ listing.reportCount === 1 ? 'report' : 'reports' }}
              </span>
            </span>
          </div>

          <div class="flex items-center gap-2 pt-2 border-t border-gray-100">
            <button
              v-if="listing.status !== 'flagged'"
              class="flex-1 px-3 py-2 text-xs font-medium text-amber-700 bg-amber-50 hover:bg-amber-100 rounded transition-colors min-h-touch flex items-center justify-center disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="actionLoading === listing.id"
              :aria-label="`Flag ${listing.title} for review`"
              @click="flagListing(listing)"
            >
              Flag
            </button>
            <button
              v-if="listing.status !== 'removed'"
              class="flex-1 px-3 py-2 text-xs font-medium text-red-700 bg-red-50 hover:bg-red-100 rounded transition-colors min-h-touch flex items-center justify-center disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="actionLoading === listing.id"
              :aria-label="`Remove ${listing.title}`"
              @click="confirmRemove(listing)"
            >
              Remove
            </button>
          </div>
        </div>
      </div>
    </template>

    <!-- Empty State -->
    <div v-else-if="!isLoading" class="text-center py-12">
      <svg class="mx-auto h-12 w-12 text-gray-300" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
        <path stroke-linecap="round" stroke-linejoin="round" d="M20.25 7.5l-.625 10.632a2.25 2.25 0 01-2.247 2.118H6.622a2.25 2.25 0 01-2.247-2.118L3.75 7.5M10 11.25h4M3.375 7.5h17.25c.621 0 1.125-.504 1.125-1.125v-1.5c0-.621-.504-1.125-1.125-1.125H3.375c-.621 0-1.125.504-1.125 1.125v1.5c0 .621.504 1.125 1.125 1.125z" />
      </svg>
      <p class="mt-3 text-sm text-gray-500">
        {{ searchQuery.trim() ? 'No listings match your search.' : 'No listings found.' }}
      </p>
    </div>

    <!-- Pagination -->
    <div v-if="!isLoading && totalPages > 1" class="pt-4">
      <PaginationControls
        :current-page="currentPage"
        :total-pages="totalPages"
        @page-change="handlePageChange"
      />
    </div>

    <!-- Remove Confirmation Modal -->
    <AppModal
      :show="showRemoveModal"
      title="Remove Listing"
      :message="`Are you sure you want to remove &quot;${listingToRemove?.title ?? 'this listing'}&quot;? This action will hide the listing from all users.`"
      confirm-label="Remove"
      cancel-label="Cancel"
      confirm-variant="danger"
      @confirm="handleRemoveConfirm"
      @cancel="handleRemoveCancel"
    />
  </div>
</template>
