<script setup lang="ts">
import { computed } from 'vue'
import { useImageGallery } from '@/composables/useImageGallery'

const props = defineProps<{
  images: string[]
}>()

const { currentIndex, currentImage, totalImages, next, previous } = useImageGallery(props.images)

const isFirst = computed(() => currentIndex.value === 0)
const isLast = computed(() => currentIndex.value === totalImages.value - 1)
</script>

<template>
  <div class="relative w-full overflow-hidden rounded-lg bg-gray-100">
    <!-- Main image -->
    <div class="relative aspect-[4/3] w-full">
      <img
        :src="currentImage"
        :alt="`Image ${currentIndex + 1} of ${totalImages}`"
        class="w-full h-full object-contain"
      />
    </div>

    <!-- Previous button -->
    <button
      v-if="totalImages > 1"
      :disabled="isFirst"
      class="absolute left-2 top-1/2 -translate-y-1/2 min-w-[44px] min-h-[44px] flex items-center justify-center rounded-full bg-white/80 shadow-md text-text hover:bg-white disabled:opacity-30 disabled:cursor-not-allowed transition-opacity"
      aria-label="Previous image"
      @click="previous"
    >
      <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
        <path fill-rule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clip-rule="evenodd" />
      </svg>
    </button>

    <!-- Next button -->
    <button
      v-if="totalImages > 1"
      :disabled="isLast"
      class="absolute right-2 top-1/2 -translate-y-1/2 min-w-[44px] min-h-[44px] flex items-center justify-center rounded-full bg-white/80 shadow-md text-text hover:bg-white disabled:opacity-30 disabled:cursor-not-allowed transition-opacity"
      aria-label="Next image"
      @click="next"
    >
      <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
        <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd" />
      </svg>
    </button>

    <!-- Position indicator -->
    <div
      v-if="totalImages > 1"
      class="absolute bottom-3 left-1/2 -translate-x-1/2 bg-black/60 text-white text-xs px-3 py-1 rounded-full"
    >
      {{ currentIndex + 1 }} / {{ totalImages }}
    </div>
  </div>
</template>
