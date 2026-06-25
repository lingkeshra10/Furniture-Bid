import { ref, computed } from 'vue';
import { furnitureService } from '@/services/api/furnitureService';
import { PAGE_SIZE } from '@/utils/constants';
import type {
  FurnitureListing,
  FurnitureListingSummary,
  CatalogFilters,
  CatalogSortOption,
  CreateListingRequest,
} from '@/types/furniture';

// Module-level state (shared singleton)
const listings = ref<FurnitureListingSummary[]>([]);
const currentListing = ref<FurnitureListing | null>(null);
const filters = ref<CatalogFilters>({});
const sort = ref<CatalogSortOption>('ending-soonest');
const page = ref(1);
const hasMore = ref(true);
const isLoading = ref(false);

// Getter
const activeFiltersCount = computed(() => {
  let count = 0;
  if (filters.value.category && filters.value.category.length > 0) count++;
  if (filters.value.condition && filters.value.condition.length > 0) count++;
  if (filters.value.priceMin != null) count++;
  if (filters.value.priceMax != null) count++;
  if (filters.value.location) count++;
  return count;
});

// Actions
async function fetchListings(reset = false): Promise<void> {
  if (reset) {
    listings.value = [];
    page.value = 1;
    hasMore.value = true;
  }

  isLoading.value = true;
  try {
    const response = await furnitureService.getListings({
      filters: filters.value,
      sort: sort.value,
      page: page.value,
      pageSize: PAGE_SIZE,
    });

    if (reset) {
      listings.value = response.data;
    } else {
      listings.value = [...listings.value, ...response.data];
    }

    hasMore.value = response.hasMore;
    page.value = response.page + 1;
  } finally {
    isLoading.value = false;
  }
}

async function fetchListingById(id: string): Promise<void> {
  isLoading.value = true;
  try {
    currentListing.value = await furnitureService.getListingById(id);
  } finally {
    isLoading.value = false;
  }
}

async function createListing(data: CreateListingRequest): Promise<string> {
  isLoading.value = true;
  try {
    const newListing = await furnitureService.createListing(data);
    return newListing.id;
  } finally {
    isLoading.value = false;
  }
}

async function updateFilters(newFilters: CatalogFilters): Promise<void> {
  filters.value = newFilters;
  await fetchListings(true);
}

async function updateSort(newSort: CatalogSortOption): Promise<void> {
  sort.value = newSort;
  await fetchListings(true);
}

function updateCurrentBid(auctionId: string, bid: number, count: number): void {
  if (currentListing.value && currentListing.value.id === auctionId) {
    currentListing.value = {
      ...currentListing.value,
      currentBid: bid,
      bidCount: count,
    };
  }
}

/**
 * Reset furniture store to initial defaults.
 * Called on 401 unauthorized to clear sensitive session data.
 * Requirement 18.3
 */
function $reset(): void {
  listings.value = [];
  currentListing.value = null;
  filters.value = {};
  sort.value = 'ending-soonest';
  page.value = 1;
  hasMore.value = true;
  isLoading.value = false;
}

export function useFurnitureStore() {
  return {
    // State
    listings,
    currentListing,
    filters,
    sort,
    page,
    hasMore,
    isLoading,

    // Getter
    activeFiltersCount,

    // Actions
    fetchListings,
    fetchListingById,
    createListing,
    updateFilters,
    updateSort,
    updateCurrentBid,
    $reset,
  };
}
