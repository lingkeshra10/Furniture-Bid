<script setup lang="ts">
import { ref } from 'vue'
import type { CatalogSortOption } from '@/types/furniture'

const props = defineProps<{
  modelValue?: CatalogSortOption
}>()

const emit = defineEmits<{
  'update:sort': [sort: CatalogSortOption]
}>()

const options: { value: CatalogSortOption; label: string }[] = [
  { value: 'ending-soonest', label: 'Ending Soonest' },
  { value: 'price-low-high', label: 'Price: Low to High' },
  { value: 'price-high-low', label: 'Price: High to Low' },
  { value: 'newest', label: 'Newest' },
]

const selected = ref<CatalogSortOption>(props.modelValue ?? 'ending-soonest')

function onSortChange(event: Event) {
  const target = event.target as HTMLSelectElement
  selected.value = target.value as CatalogSortOption
  emit('update:sort', selected.value)
}
</script>

<template>
  <div class="flex items-center gap-2">
    <label for="sort-select" class="text-sm text-text whitespace-nowrap">
      Sort by
    </label>
    <select
      id="sort-select"
      :value="selected"
      @change="onSortChange"
      class="min-h-touch rounded border border-gray-300 px-3 py-2 text-sm bg-card text-text focus:border-primary focus:ring-1 focus:ring-primary cursor-pointer"
    >
      <option
        v-for="option in options"
        :key="option.value"
        :value="option.value"
      >
        {{ option.label }}
      </option>
    </select>
  </div>
</template>
