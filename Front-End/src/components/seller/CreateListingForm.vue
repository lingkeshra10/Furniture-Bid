<script setup lang="ts">
import { ref, reactive } from 'vue';
import { createListingSchema } from '@/utils/validators';
import { useFurnitureStore } from '@/stores/furniture';
import { useToast } from '@/composables/useToast';
import ImageUploader from './ImageUploader.vue';
import type { FurnitureCategory, FurnitureCondition } from '@/types/furniture';

const emit = defineEmits<{
  success: [listingId: string];
}>();

const furnitureStore = useFurnitureStore();
const { showToast } = useToast();

const categories: { value: FurnitureCategory; label: string }[] = [
  { value: 'sofa', label: 'Sofa' },
  { value: 'dining-table', label: 'Dining Table' },
  { value: 'office-chair', label: 'Office Chair' },
  { value: 'wardrobe', label: 'Wardrobe' },
  { value: 'bed-frame', label: 'Bed Frame' },
  { value: 'coffee-table', label: 'Coffee Table' },
  { value: 'cabinet', label: 'Cabinet' },
  { value: 'bookshelf', label: 'Bookshelf' },
];

const conditions: { value: FurnitureCondition; label: string }[] = [
  { value: 'new', label: 'New' },
  { value: 'like-new', label: 'Like New' },
  { value: 'good', label: 'Good' },
  { value: 'fair', label: 'Fair' },
  { value: 'poor', label: 'Poor' },
];

const form = reactive({
  title: '',
  description: '',
  category: '' as FurnitureCategory | '',
  condition: '' as FurnitureCondition | '',
  brand: '',
  material: '',
  dimensions: {
    width: null as number | null,
    height: null as number | null,
    length: null as number | null,
  },
  weight: null as number | null,
  location: '',
  startingPrice: null as number | null,
  reservePrice: null as number | null,
  auctionEndDate: '',
});

const images = ref<File[]>([]);
const errors = reactive<Record<string, string>>({});
const serverError = ref('');
const isSubmitting = ref(false);

function clearErrors(): void {
  Object.keys(errors).forEach((key) => {
    errors[key] = '';
  });
}

function validateAll(): boolean {
  clearErrors();

  // Validate images separately
  if (images.value.length === 0) {
    errors.images = 'At least one image is required';
  } else if (images.value.length > 10) {
    errors.images = 'Maximum 10 images allowed';
  }

  // Build the data object for Zod validation
  const data = {
    title: form.title,
    description: form.description,
    category: form.category || undefined,
    condition: form.condition || undefined,
    dimensions: {
      width: form.dimensions.width ?? undefined,
      height: form.dimensions.height ?? undefined,
      length: form.dimensions.length ?? undefined,
    },
    startingPrice: form.startingPrice ?? undefined,
    reservePrice: form.reservePrice ?? undefined,
    auctionEndDate: form.auctionEndDate,
    brand: form.brand || undefined,
    material: form.material || undefined,
    weight: form.weight ?? undefined,
    location: form.location || undefined,
  };

  const result = createListingSchema.safeParse(data);

  if (!result.success) {
    for (const issue of result.error.issues) {
      const path = issue.path.join('.');
      if (!errors[path]) {
        errors[path] = issue.message;
      }
    }
  }

  // Check for any errors (including image errors)
  return !Object.values(errors).some((e) => e);
}

function validateField(field: string): void {
  // Clear current error for this field
  errors[field] = '';

  // Build value based on field path
  let value: unknown;
  if (field.startsWith('dimensions.')) {
    const subField = field.split('.')[1] as 'width' | 'height' | 'length';
    value = form.dimensions[subField];
  } else if (field === 'weight') {
    value = form.weight;
  } else if (field === 'startingPrice') {
    value = form.startingPrice;
  } else if (field === 'reservePrice') {
    value = form.reservePrice;
  } else {
    value = (form as Record<string, unknown>)[field];
  }

  // For nested fields and refinements, do a full validation and pick the relevant error
  const data = {
    title: form.title,
    description: form.description,
    category: form.category || undefined,
    condition: form.condition || undefined,
    dimensions: {
      width: form.dimensions.width ?? undefined,
      height: form.dimensions.height ?? undefined,
      length: form.dimensions.length ?? undefined,
    },
    startingPrice: form.startingPrice ?? undefined,
    reservePrice: form.reservePrice ?? undefined,
    auctionEndDate: form.auctionEndDate,
    brand: form.brand || undefined,
    material: form.material || undefined,
    weight: form.weight ?? undefined,
    location: form.location || undefined,
  };

  const result = createListingSchema.safeParse(data);
  if (!result.success) {
    for (const issue of result.error.issues) {
      const path = issue.path.join('.');
      if (path === field && !errors[field]) {
        errors[field] = issue.message;
        break;
      }
    }
  }
}

async function handleSubmit(): Promise<void> {
  serverError.value = '';

  if (!validateAll()) return;

  isSubmitting.value = true;

  try {
    const listingId = await furnitureStore.createListing({
      title: form.title,
      description: form.description,
      category: form.category as FurnitureCategory,
      condition: form.condition as FurnitureCondition,
      dimensions: {
        width: form.dimensions.width!,
        height: form.dimensions.height!,
        length: form.dimensions.length!,
      },
      startingPrice: form.startingPrice!,
      reservePrice: form.reservePrice!,
      auctionEndDate: form.auctionEndDate,
      images: images.value,
      brand: form.brand || undefined,
      material: form.material || undefined,
      weight: form.weight ?? undefined,
      location: form.location || undefined,
    });

    showToast('Listing created successfully!', 'success');
    emit('success', listingId);
  } catch (error: unknown) {
    if (error && typeof error === 'object' && 'fieldErrors' in error) {
      const fieldErrors = (error as { fieldErrors: Record<string, string> }).fieldErrors;
      for (const [field, message] of Object.entries(fieldErrors)) {
        errors[field] = message;
      }
    } else {
      const message = error instanceof Error ? error.message : 'Failed to create listing. Please try again.';
      serverError.value = message;
    }
  } finally {
    isSubmitting.value = false;
  }
}

function updateImages(files: File[]): void {
  images.value = files;
  if (files.length > 0) {
    errors.images = '';
  }
}
</script>

<template>
  <form @submit.prevent="handleSubmit" class="space-y-6" novalidate aria-label="Create listing form">
    <!-- Server error -->
    <div
      v-if="serverError"
      class="rounded-md bg-red-50 border border-red-200 p-3 text-sm text-red-700"
      role="alert"
    >
      {{ serverError }}
    </div>

    <!-- Title -->
    <div>
      <label for="listing-title" class="block text-sm font-medium text-text mb-1">
        Title <span class="text-red-500">*</span>
      </label>
      <input
        id="listing-title"
        v-model="form.title"
        type="text"
        placeholder="e.g., Mid-Century Modern Walnut Coffee Table"
        class="w-full min-h-touch rounded-lg border px-4 py-3 text-text placeholder-gray-400 transition focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
        :class="errors.title ? 'border-red-400' : 'border-gray-300'"
        :aria-invalid="!!errors.title"
        :aria-describedby="errors.title ? 'listing-title-error' : undefined"
        @blur="validateField('title')"
      />
      <p v-if="errors.title" id="listing-title-error" class="mt-1 text-sm text-red-600" role="alert">
        {{ errors.title }}
      </p>
    </div>

    <!-- Description -->
    <div>
      <label for="listing-description" class="block text-sm font-medium text-text mb-1">
        Description <span class="text-red-500">*</span>
      </label>
      <textarea
        id="listing-description"
        v-model="form.description"
        rows="4"
        placeholder="Describe the item's condition, history, and any notable features..."
        class="w-full rounded-lg border px-4 py-3 text-text placeholder-gray-400 transition focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary resize-y"
        :class="errors.description ? 'border-red-400' : 'border-gray-300'"
        :aria-invalid="!!errors.description"
        :aria-describedby="errors.description ? 'listing-description-error' : undefined"
        @blur="validateField('description')"
      />
      <p v-if="errors.description" id="listing-description-error" class="mt-1 text-sm text-red-600" role="alert">
        {{ errors.description }}
      </p>
    </div>

    <!-- Category and Condition row -->
    <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
      <!-- Category -->
      <div>
        <label for="listing-category" class="block text-sm font-medium text-text mb-1">
          Category <span class="text-red-500">*</span>
        </label>
        <select
          id="listing-category"
          v-model="form.category"
          class="w-full min-h-touch rounded-lg border px-4 py-3 text-text transition focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
          :class="errors.category ? 'border-red-400' : 'border-gray-300'"
          :aria-invalid="!!errors.category"
          :aria-describedby="errors.category ? 'listing-category-error' : undefined"
          @blur="validateField('category')"
          @change="validateField('category')"
        >
          <option value="" disabled>Select a category</option>
          <option v-for="cat in categories" :key="cat.value" :value="cat.value">
            {{ cat.label }}
          </option>
        </select>
        <p v-if="errors.category" id="listing-category-error" class="mt-1 text-sm text-red-600" role="alert">
          {{ errors.category }}
        </p>
      </div>

      <!-- Condition -->
      <div>
        <label for="listing-condition" class="block text-sm font-medium text-text mb-1">
          Condition <span class="text-red-500">*</span>
        </label>
        <select
          id="listing-condition"
          v-model="form.condition"
          class="w-full min-h-touch rounded-lg border px-4 py-3 text-text transition focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
          :class="errors.condition ? 'border-red-400' : 'border-gray-300'"
          :aria-invalid="!!errors.condition"
          :aria-describedby="errors.condition ? 'listing-condition-error' : undefined"
          @blur="validateField('condition')"
          @change="validateField('condition')"
        >
          <option value="" disabled>Select condition</option>
          <option v-for="cond in conditions" :key="cond.value" :value="cond.value">
            {{ cond.label }}
          </option>
        </select>
        <p v-if="errors.condition" id="listing-condition-error" class="mt-1 text-sm text-red-600" role="alert">
          {{ errors.condition }}
        </p>
      </div>
    </div>

    <!-- Dimensions -->
    <fieldset>
      <legend class="block text-sm font-medium text-text mb-2">
        Dimensions (cm) <span class="text-red-500">*</span>
      </legend>
      <div class="grid grid-cols-3 gap-3">
        <div>
          <label for="listing-width" class="block text-xs text-gray-500 mb-1">Width</label>
          <input
            id="listing-width"
            v-model.number="form.dimensions.width"
            type="number"
            min="1"
            max="9999"
            placeholder="cm"
            class="w-full min-h-touch rounded-lg border px-3 py-2 text-text placeholder-gray-400 transition focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
            :class="errors['dimensions.width'] ? 'border-red-400' : 'border-gray-300'"
            :aria-invalid="!!errors['dimensions.width']"
            @blur="validateField('dimensions.width')"
          />
          <p v-if="errors['dimensions.width']" class="mt-1 text-xs text-red-600" role="alert">
            {{ errors['dimensions.width'] }}
          </p>
        </div>
        <div>
          <label for="listing-height" class="block text-xs text-gray-500 mb-1">Height</label>
          <input
            id="listing-height"
            v-model.number="form.dimensions.height"
            type="number"
            min="1"
            max="9999"
            placeholder="cm"
            class="w-full min-h-touch rounded-lg border px-3 py-2 text-text placeholder-gray-400 transition focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
            :class="errors['dimensions.height'] ? 'border-red-400' : 'border-gray-300'"
            :aria-invalid="!!errors['dimensions.height']"
            @blur="validateField('dimensions.height')"
          />
          <p v-if="errors['dimensions.height']" class="mt-1 text-xs text-red-600" role="alert">
            {{ errors['dimensions.height'] }}
          </p>
        </div>
        <div>
          <label for="listing-length" class="block text-xs text-gray-500 mb-1">Length</label>
          <input
            id="listing-length"
            v-model.number="form.dimensions.length"
            type="number"
            min="1"
            max="9999"
            placeholder="cm"
            class="w-full min-h-touch rounded-lg border px-3 py-2 text-text placeholder-gray-400 transition focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
            :class="errors['dimensions.length'] ? 'border-red-400' : 'border-gray-300'"
            :aria-invalid="!!errors['dimensions.length']"
            @blur="validateField('dimensions.length')"
          />
          <p v-if="errors['dimensions.length']" class="mt-1 text-xs text-red-600" role="alert">
            {{ errors['dimensions.length'] }}
          </p>
        </div>
      </div>
    </fieldset>

    <!-- Pricing row -->
    <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
      <!-- Starting Price -->
      <div>
        <label for="listing-starting-price" class="block text-sm font-medium text-text mb-1">
          Starting Price ($) <span class="text-red-500">*</span>
        </label>
        <input
          id="listing-starting-price"
          v-model.number="form.startingPrice"
          type="number"
          min="0.01"
          max="999999.99"
          step="0.01"
          placeholder="0.01"
          class="w-full min-h-touch rounded-lg border px-4 py-3 text-text placeholder-gray-400 transition focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
          :class="errors.startingPrice ? 'border-red-400' : 'border-gray-300'"
          :aria-invalid="!!errors.startingPrice"
          :aria-describedby="errors.startingPrice ? 'listing-starting-price-error' : undefined"
          @blur="validateField('startingPrice')"
        />
        <p v-if="errors.startingPrice" id="listing-starting-price-error" class="mt-1 text-sm text-red-600" role="alert">
          {{ errors.startingPrice }}
        </p>
      </div>

      <!-- Reserve Price -->
      <div>
        <label for="listing-reserve-price" class="block text-sm font-medium text-text mb-1">
          Reserve Price ($) <span class="text-red-500">*</span>
        </label>
        <input
          id="listing-reserve-price"
          v-model.number="form.reservePrice"
          type="number"
          min="0.01"
          max="999999.99"
          step="0.01"
          placeholder="Must be ≥ starting price"
          class="w-full min-h-touch rounded-lg border px-4 py-3 text-text placeholder-gray-400 transition focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
          :class="errors.reservePrice ? 'border-red-400' : 'border-gray-300'"
          :aria-invalid="!!errors.reservePrice"
          :aria-describedby="errors.reservePrice ? 'listing-reserve-price-error' : undefined"
          @blur="validateField('reservePrice')"
        />
        <p v-if="errors.reservePrice" id="listing-reserve-price-error" class="mt-1 text-sm text-red-600" role="alert">
          {{ errors.reservePrice }}
        </p>
      </div>
    </div>

    <!-- Auction End Date -->
    <div>
      <label for="listing-end-date" class="block text-sm font-medium text-text mb-1">
        Auction End Date <span class="text-red-500">*</span>
      </label>
      <input
        id="listing-end-date"
        v-model="form.auctionEndDate"
        type="datetime-local"
        class="w-full min-h-touch rounded-lg border px-4 py-3 text-text transition focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
        :class="errors.auctionEndDate ? 'border-red-400' : 'border-gray-300'"
        :aria-invalid="!!errors.auctionEndDate"
        :aria-describedby="errors.auctionEndDate ? 'listing-end-date-error' : 'listing-end-date-hint'"
        @blur="validateField('auctionEndDate')"
      />
      <p v-if="errors.auctionEndDate" id="listing-end-date-error" class="mt-1 text-sm text-red-600" role="alert">
        {{ errors.auctionEndDate }}
      </p>
      <p v-else id="listing-end-date-hint" class="mt-1 text-xs text-gray-500">
        Must be between 24 hours and 30 days from now
      </p>
    </div>

    <!-- Images -->
    <ImageUploader
      :files="images"
      :error="errors.images"
      @update:files="updateImages"
    />

    <!-- Optional fields section -->
    <details class="border border-gray-200 rounded-lg">
      <summary class="px-4 py-3 cursor-pointer text-sm font-medium text-text hover:text-primary transition">
        Optional Details (brand, material, weight, location)
      </summary>
      <div class="px-4 pb-4 pt-2 space-y-4">
        <!-- Brand -->
        <div>
          <label for="listing-brand" class="block text-sm font-medium text-text mb-1">Brand</label>
          <input
            id="listing-brand"
            v-model="form.brand"
            type="text"
            placeholder="e.g., IKEA, Herman Miller"
            class="w-full min-h-touch rounded-lg border border-gray-300 px-4 py-3 text-text placeholder-gray-400 transition focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
          />
        </div>

        <!-- Material -->
        <div>
          <label for="listing-material" class="block text-sm font-medium text-text mb-1">Material</label>
          <input
            id="listing-material"
            v-model="form.material"
            type="text"
            placeholder="e.g., Walnut, Oak, Metal"
            class="w-full min-h-touch rounded-lg border border-gray-300 px-4 py-3 text-text placeholder-gray-400 transition focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
          />
        </div>

        <!-- Weight -->
        <div>
          <label for="listing-weight" class="block text-sm font-medium text-text mb-1">Weight (kg)</label>
          <input
            id="listing-weight"
            v-model.number="form.weight"
            type="number"
            min="0.1"
            max="9999"
            step="0.1"
            placeholder="e.g., 15.5"
            class="w-full min-h-touch rounded-lg border px-4 py-3 text-text placeholder-gray-400 transition focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
            :class="errors.weight ? 'border-red-400' : 'border-gray-300'"
            :aria-invalid="!!errors.weight"
            @blur="validateField('weight')"
          />
          <p v-if="errors.weight" class="mt-1 text-sm text-red-600" role="alert">
            {{ errors.weight }}
          </p>
        </div>

        <!-- Location -->
        <div>
          <label for="listing-location" class="block text-sm font-medium text-text mb-1">Location</label>
          <input
            id="listing-location"
            v-model="form.location"
            type="text"
            placeholder="e.g., San Francisco, CA"
            class="w-full min-h-touch rounded-lg border border-gray-300 px-4 py-3 text-text placeholder-gray-400 transition focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
          />
        </div>
      </div>
    </details>

    <!-- Submit button -->
    <button
      type="submit"
      :disabled="isSubmitting"
      class="w-full min-h-touch flex items-center justify-center rounded-lg bg-primary px-6 py-3 text-white font-medium transition hover:bg-primary/90 focus:outline-none focus:ring-2 focus:ring-primary/50 focus:ring-offset-2 disabled:opacity-60 disabled:cursor-not-allowed"
    >
      <svg
        v-if="isSubmitting"
        class="animate-spin -ml-1 mr-2 h-5 w-5 text-white"
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 24 24"
        aria-hidden="true"
      >
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
      </svg>
      {{ isSubmitting ? 'Creating Listing...' : 'Create Listing' }}
    </button>
  </form>
</template>
