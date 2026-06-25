<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import CreateListingForm from '@/components/seller/CreateListingForm.vue';

const router = useRouter();

const createdListingId = ref<string | null>(null);

function handleSuccess(listingId: string): void {
  createdListingId.value = listingId;
}

function viewListing(): void {
  if (createdListingId.value) {
    router.push({ name: 'listing-detail', params: { id: createdListingId.value } });
  }
}

function createAnother(): void {
  createdListingId.value = null;
}
</script>

<template>
  <div class="max-w-3xl mx-auto px-4 py-8">
    <!-- Success state -->
    <div v-if="createdListingId" class="text-center space-y-6">
      <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-success/10">
        <svg class="w-8 h-8 text-success" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
          <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
        </svg>
      </div>
      <h1 class="text-2xl font-bold text-text">Listing Created Successfully!</h1>
      <p class="text-gray-600">Your furniture listing is now live and ready for bids.</p>
      <div class="flex flex-col sm:flex-row items-center justify-center gap-3">
        <button
          @click="viewListing"
          class="min-h-touch px-6 py-3 rounded-lg bg-primary text-white font-medium transition hover:bg-primary/90 focus:outline-none focus:ring-2 focus:ring-primary/50 focus:ring-offset-2"
        >
          View Listing
        </button>
        <button
          @click="createAnother"
          class="min-h-touch px-6 py-3 rounded-lg border border-gray-300 text-text font-medium transition hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-primary/50 focus:ring-offset-2"
        >
          Create Another Listing
        </button>
      </div>
    </div>

    <!-- Form state -->
    <div v-else>
      <div class="mb-6">
        <h1 class="text-2xl font-bold text-text">Create New Listing</h1>
        <p class="mt-1 text-sm text-gray-600">Fill in the details about your furniture item to start an auction.</p>
      </div>
      <div class="bg-card rounded-xl shadow-sm border border-gray-100 p-6">
        <CreateListingForm @success="handleSuccess" />
      </div>
    </div>
  </div>
</template>
