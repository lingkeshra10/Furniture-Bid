<script setup lang="ts">
import { useRouter } from 'vue-router';
import ActiveListings from '@/components/seller/ActiveListings.vue';
import CompletedAuctions from '@/components/seller/CompletedAuctions.vue';

const router = useRouter();

function navigateToListing(id: string): void {
  router.push({ name: 'listing-detail', params: { id } });
}
</script>

<template>
  <div class="min-h-screen bg-background">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
      <!-- Page header -->
      <div class="mb-8">
        <h1 class="text-2xl font-bold text-text">Seller Dashboard</h1>
        <p class="mt-1 text-sm text-gray-500">
          Monitor your active listings and review completed auctions.
        </p>
      </div>

      <!-- Active Listings Section -->
      <div class="mb-10">
        <ActiveListings @listing-click="navigateToListing" />
      </div>

      <!-- Completed Auctions Section -->
      <div>
        <CompletedAuctions @listing-click="navigateToListing" />
      </div>
    </div>
  </div>
</template>
