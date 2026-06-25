<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import ErrorState from '@/components/common/ErrorState.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import PaginationControls from '@/components/common/PaginationControls.vue'
import { auctionService } from '@/services/api/auctionService'
import { formatCurrency, formatDateTime } from '@/utils/formatters'
import type { UserBidHistoryItem } from '@/types/auction'

const router = useRouter()

const bids = ref<UserBidHistoryItem[]>([])
const isLoading = ref(false)
const error = ref<string | null>(null)
const currentPage = ref(1)
const totalPages = ref(1)
const pageSize = 20

async function loadBidHistory(page = 1): Promise<void> {
  isLoading.value = true
  error.value = null
  try {
    const response = await auctionService.getUserBidHistory(page, pageSize)
    bids.value = response.data
    currentPage.value = response.page
    totalPages.value = Math.ceil(response.total / response.pageSize) || 1
  } catch (e) {
    error.value = 'Failed to load your bidding history. Please try again.'
  } finally {
    isLoading.value = false
  }
}

function handlePageChange(page: number): void {
  loadBidHistory(page)
}

function navigateToListing(auctionId: string): void {
  router.push({ name: 'listing-detail', params: { id: auctionId } })
}

function getStatusColor(status: string): string {
  switch (status) {
    case 'winning':
      return 'bg-green-100 text-green-800'
    case 'won':
      return 'bg-success/10 text-success'
    case 'outbid':
      return 'bg-yellow-100 text-yellow-800'
    case 'lost':
      return 'bg-red-100 text-red-800'
    default:
      return 'bg-gray-100 text-gray-800'
  }
}

function getStatusLabel(status: string): string {
  switch (status) {
    case 'winning':
      return 'Winning'
    case 'won':
      return 'Won'
    case 'outbid':
      return 'Outbid'
    case 'lost':
      return 'Lost'
    default:
      return status
  }
}

onMounted(() => {
  loadBidHistory()
})
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 py-6 sm:px-6 lg:px-8">
    <h1 class="text-2xl font-bold text-text mb-6">Bidding History</h1>

    <!-- Loading state -->
    <LoadingSpinner
      v-if="isLoading"
      size="lg"
      message="Loading your bidding history..."
    />

    <!-- Error state with retry -->
    <ErrorState
      v-else-if="error"
      :message="error"
      retry-label="Retry"
      @retry="loadBidHistory(currentPage)"
    />

    <!-- Empty state -->
    <EmptyState
      v-else-if="bids.length === 0"
      title="No bids placed yet"
      message="Browse the catalog and place bids on furniture you're interested in."
    />

    <!-- Bid history list -->
    <div v-else>
      <!-- Desktop table view -->
      <div class="hidden sm:block overflow-x-auto">
        <table class="w-full border-collapse">
          <thead>
            <tr class="border-b border-gray-200">
              <th class="text-left py-3 px-4 text-sm font-semibold text-gray-600">Auction</th>
              <th class="text-left py-3 px-4 text-sm font-semibold text-gray-600">Bid Amount</th>
              <th class="text-left py-3 px-4 text-sm font-semibold text-gray-600">Date</th>
              <th class="text-left py-3 px-4 text-sm font-semibold text-gray-600">Status</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="bid in bids"
              :key="bid.id"
              class="border-b border-gray-100 hover:bg-gray-50 cursor-pointer transition-colors"
              tabindex="0"
              role="button"
              :aria-label="`View auction: ${bid.auctionTitle}`"
              @click="navigateToListing(bid.auctionId)"
              @keydown.enter="navigateToListing(bid.auctionId)"
            >
              <td class="py-3 px-4 text-sm text-text font-medium">{{ bid.auctionTitle }}</td>
              <td class="py-3 px-4 text-sm text-text">{{ formatCurrency(bid.amount) }}</td>
              <td class="py-3 px-4 text-sm text-gray-500">{{ formatDateTime(bid.timestamp) }}</td>
              <td class="py-3 px-4">
                <span
                  class="inline-block px-2 py-1 rounded-full text-xs font-medium"
                  :class="getStatusColor(bid.status)"
                >
                  {{ getStatusLabel(bid.status) }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Mobile card view -->
      <div class="sm:hidden space-y-3">
        <div
          v-for="bid in bids"
          :key="bid.id"
          class="bg-card rounded-lg border border-gray-200 p-4 cursor-pointer hover:shadow-md transition-shadow"
          tabindex="0"
          role="button"
          :aria-label="`View auction: ${bid.auctionTitle}`"
          @click="navigateToListing(bid.auctionId)"
          @keydown.enter="navigateToListing(bid.auctionId)"
        >
          <div class="flex items-start justify-between mb-2">
            <h3 class="text-sm font-medium text-text flex-1 mr-2">{{ bid.auctionTitle }}</h3>
            <span
              class="inline-block px-2 py-1 rounded-full text-xs font-medium shrink-0"
              :class="getStatusColor(bid.status)"
            >
              {{ getStatusLabel(bid.status) }}
            </span>
          </div>
          <div class="flex items-center justify-between text-sm">
            <span class="font-semibold text-primary">{{ formatCurrency(bid.amount) }}</span>
            <span class="text-gray-500">{{ formatDateTime(bid.timestamp) }}</span>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div class="mt-6">
        <PaginationControls
          :current-page="currentPage"
          :total-pages="totalPages"
          @page-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>
