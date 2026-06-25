<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useFurnitureStore } from '@/stores/furniture';
import { useInfiniteScroll } from '@/composables/useInfiniteScroll';
import CatalogFilters from '@/components/catalog/CatalogFilters.vue';
import CatalogSortDropdown from '@/components/catalog/CatalogSortDropdown.vue';
import CatalogGrid from '@/components/catalog/CatalogGrid.vue';
import LoadingSpinner from '@/components/common/LoadingSpinner.vue';
import ErrorState from '@/components/common/ErrorState.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import type { CatalogFilters as CatalogFiltersType, CatalogSortOption } from '@/types/furniture';

const store = useFurnitureStore();

const error = ref<string | null>(null);

const canLoadMore = computed(() => !store.isLoading.value && store.hasMore.value && !error.value);

const showEmpty = computed(
  () => !store.isLoading.value && !error.value && store.listings.value.length === 0
);

const { sentinelRef } = useInfiniteScroll({
  onLoadMore: loadMore,
  enabled: canLoadMore,
});

async function loadListings(reset = false): Promise<void> {
  error.value = null;
  try {
    await store.fetchListings(reset);
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to load listings. Please try again.';
  }
}

async function loadMore(): Promise<void> {
  if (store.isLoading.value || !store.hasMore.value) return;
  await loadListings(false);
}

async function handleRetry(): Promise<void> {
  await loadListings(store.listings.value.length === 0);
}

async function handleFiltersUpdate(newFilters: CatalogFiltersType): Promise<void> {
  error.value = null;
  try {
    await store.updateFilters(newFilters);
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to update filters. Please try again.';
  }
}

async function handleSortUpdate(newSort: CatalogSortOption): Promise<void> {
  error.value = null;
  try {
    await store.updateSort(newSort);
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to update sort. Please try again.';
  }
}

onMounted(() => {
  loadListings(true);
});
</script>

<template>
  <div class="min-h-screen bg-background">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
      <!-- Header with filters and sort -->
      <div class="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4 mb-6">
        <CatalogFilters @update:filters="handleFiltersUpdate" />
        <CatalogSortDropdown @update:sort="handleSortUpdate" />
      </div>

      <!-- Error state -->
      <ErrorState
        v-if="error"
        :message="error"
        @retry="handleRetry"
      />

      <!-- Empty state -->
      <EmptyState
        v-else-if="showEmpty"
        title="No results found"
        message="No listings match your current filters. Try adjusting your filter criteria to see more results."
      />

      <!-- Listings grid -->
      <template v-else>
        <CatalogGrid :listings="store.listings.value" />

        <!-- Loading spinner for subsequent pages -->
        <LoadingSpinner
          v-if="store.isLoading.value"
          size="md"
          message="Loading more listings..."
        />

        <!-- Scroll sentinel for infinite scroll -->
        <div
          ref="sentinelRef"
          class="h-1 w-full"
          aria-hidden="true"
        />
      </template>
    </div>
  </div>
</template>
