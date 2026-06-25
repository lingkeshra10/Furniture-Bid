<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { auctionService } from '@/services/api/auctionService';
import LoadingSpinner from '@/components/common/LoadingSpinner.vue';
import ErrorState from '@/components/common/ErrorState.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import PaginationControls from '@/components/common/PaginationControls.vue';
import SellerListingCard from './SellerListingCard.vue';
import type { SellerCompletedAuction } from '@/types/auction';

const emit = defineEmits<{
  'listing-click': [id: string];
}>();

const auctions = ref<SellerCompletedAuction[]>([]);
const isLoading = ref(false);
const error = ref<string | null>(null);
const currentPage = ref(1);
const totalPages = ref(1);
const pageSize = 20;

const showEmpty = computed(
  () => !isLoading.value && !error.value && auctions.value.length === 0
);

async function fetchAuctions(page = 1): Promise<void> {
  isLoading.value = true;
  error.value = null;
  try {
    const response = await auctionService.getSellerCompletedAuctions(page, pageSize);
    auctions.value = response.data;
    currentPage.value = response.page;
    totalPages.value = Math.ceil(response.total / response.pageSize) || 1;
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to load completed auctions. Please try again.';
  } finally {
    isLoading.value = false;
  }
}

function handlePageChange(page: number): void {
  fetchAuctions(page);
}

function handleRetry(): void {
  fetchAuctions(currentPage.value);
}

function handleListingClick(id: string): void {
  emit('listing-click', id);
}

onMounted(() => {
  fetchAuctions(1);
});
</script>

<template>
  <section aria-labelledby="completed-auctions-heading">
    <h2 id="completed-auctions-heading" class="text-lg font-semibold text-text mb-4">
      Completed Auctions
    </h2>

    <!-- Loading state -->
    <LoadingSpinner
      v-if="isLoading && auctions.length === 0"
      size="md"
      message="Loading completed auctions..."
    />

    <!-- Error state -->
    <ErrorState
      v-else-if="error"
      :message="error"
      @retry="handleRetry"
    />

    <!-- Empty state -->
    <EmptyState
      v-else-if="showEmpty"
      title="No completed auctions"
      message="You don't have any completed auctions yet. Once your active listings end, they'll appear here."
    />

    <!-- Auctions list -->
    <template v-else>
      <div class="flex flex-col gap-3">
        <SellerListingCard
          v-for="auction in auctions"
          :key="auction.id"
          :listing="auction"
          type="completed"
          @click="handleListingClick"
        />
      </div>

      <!-- Pagination -->
      <div class="mt-4">
        <PaginationControls
          :current-page="currentPage"
          :total-pages="totalPages"
          :disabled="isLoading"
          @page-change="handlePageChange"
        />
      </div>
    </template>
  </section>
</template>
