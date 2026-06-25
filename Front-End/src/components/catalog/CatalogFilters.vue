<script setup lang="ts">
import { reactive, watch } from 'vue'
import type { CatalogFilters, FurnitureCategory, FurnitureCondition } from '@/types/furniture'

const props = defineProps<{
  modelValue?: CatalogFilters
}>()

const emit = defineEmits<{
  'update:filters': [filters: CatalogFilters]
}>()

const categories: { value: FurnitureCategory; label: string }[] = [
  { value: 'sofa', label: 'Sofa' },
  { value: 'dining-table', label: 'Dining Table' },
  { value: 'office-chair', label: 'Office Chair' },
  { value: 'wardrobe', label: 'Wardrobe' },
  { value: 'bed-frame', label: 'Bed Frame' },
  { value: 'coffee-table', label: 'Coffee Table' },
  { value: 'cabinet', label: 'Cabinet' },
  { value: 'bookshelf', label: 'Bookshelf' },
]

const conditions: { value: FurnitureCondition; label: string }[] = [
  { value: 'new', label: 'New' },
  { value: 'like-new', label: 'Like New' },
  { value: 'good', label: 'Good' },
  { value: 'fair', label: 'Fair' },
  { value: 'poor', label: 'Poor' },
]

const state = reactive<{
  selectedCategories: FurnitureCategory[]
  selectedConditions: FurnitureCondition[]
  priceMin: string
  priceMax: string
  location: string
}>({
  selectedCategories: props.modelValue?.category ?? [],
  selectedConditions: props.modelValue?.condition ?? [],
  priceMin: props.modelValue?.priceMin?.toString() ?? '',
  priceMax: props.modelValue?.priceMax?.toString() ?? '',
  location: props.modelValue?.location ?? '',
})

function emitFilters() {
  const filters: CatalogFilters = {}

  if (state.selectedCategories.length > 0) {
    filters.category = [...state.selectedCategories]
  }
  if (state.selectedConditions.length > 0) {
    filters.condition = [...state.selectedConditions]
  }

  const min = parseFloat(state.priceMin)
  if (!isNaN(min) && min >= 0) {
    filters.priceMin = Math.min(min, 999999)
  }

  const max = parseFloat(state.priceMax)
  if (!isNaN(max) && max >= 0) {
    filters.priceMax = Math.min(max, 999999)
  }

  if (state.location.trim()) {
    filters.location = state.location.trim()
  }

  emit('update:filters', filters)
}

function clampPrice(field: 'priceMin' | 'priceMax') {
  const val = parseFloat(state[field])
  if (isNaN(val) || val < 0) {
    state[field] = ''
  } else if (val > 999999) {
    state[field] = '999999'
  }
}

watch(
  () => [state.selectedCategories, state.selectedConditions, state.priceMin, state.priceMax, state.location],
  () => emitFilters(),
  { deep: true }
)
</script>

<template>
  <div class="space-y-5">
    <!-- Category Multi-Select -->
    <fieldset>
      <legend class="text-sm font-medium text-text mb-2">Category</legend>
      <div class="space-y-1">
        <label
          v-for="cat in categories"
          :key="cat.value"
          class="flex items-center gap-2 min-h-touch cursor-pointer px-1"
        >
          <input
            type="checkbox"
            :value="cat.value"
            v-model="state.selectedCategories"
            class="w-4 h-4 rounded border-gray-300 text-primary focus:ring-primary"
          />
          <span class="text-sm text-text">{{ cat.label }}</span>
        </label>
      </div>
    </fieldset>

    <!-- Condition Multi-Select -->
    <fieldset>
      <legend class="text-sm font-medium text-text mb-2">Condition</legend>
      <div class="space-y-1">
        <label
          v-for="cond in conditions"
          :key="cond.value"
          class="flex items-center gap-2 min-h-touch cursor-pointer px-1"
        >
          <input
            type="checkbox"
            :value="cond.value"
            v-model="state.selectedConditions"
            class="w-4 h-4 rounded border-gray-300 text-primary focus:ring-primary"
          />
          <span class="text-sm text-text">{{ cond.label }}</span>
        </label>
      </div>
    </fieldset>

    <!-- Price Range -->
    <fieldset>
      <legend class="text-sm font-medium text-text mb-2">Price Range</legend>
      <div class="flex items-center gap-2">
        <div class="flex-1">
          <label for="price-min" class="sr-only">Minimum price</label>
          <input
            id="price-min"
            v-model="state.priceMin"
            type="number"
            min="0"
            max="999999"
            step="1"
            placeholder="Min"
            class="w-full min-h-touch rounded border border-gray-300 px-3 py-2 text-sm focus:border-primary focus:ring-1 focus:ring-primary"
            @blur="clampPrice('priceMin')"
          />
        </div>
        <span class="text-gray-400 text-sm">–</span>
        <div class="flex-1">
          <label for="price-max" class="sr-only">Maximum price</label>
          <input
            id="price-max"
            v-model="state.priceMax"
            type="number"
            min="0"
            max="999999"
            step="1"
            placeholder="Max"
            class="w-full min-h-touch rounded border border-gray-300 px-3 py-2 text-sm focus:border-primary focus:ring-1 focus:ring-primary"
            @blur="clampPrice('priceMax')"
          />
        </div>
      </div>
    </fieldset>

    <!-- Location -->
    <div>
      <label for="filter-location" class="text-sm font-medium text-text mb-2 block">
        Location
      </label>
      <input
        id="filter-location"
        v-model="state.location"
        type="text"
        placeholder="e.g., New York"
        class="w-full min-h-touch rounded border border-gray-300 px-3 py-2 text-sm focus:border-primary focus:ring-1 focus:ring-primary"
      />
    </div>
  </div>
</template>
