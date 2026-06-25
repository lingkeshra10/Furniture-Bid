import { ref, computed } from 'vue';

export interface UseImageGalleryReturn {
  currentIndex: ReturnType<typeof ref<number>>;
  currentImage: ReturnType<typeof computed<string>>;
  totalImages: ReturnType<typeof computed<number>>;
  next: () => void;
  previous: () => void;
  goTo: (index: number) => void;
}

/**
 * Composable for image gallery navigation state.
 * Provides current index, image accessor, and navigation controls.
 * Does not wrap around — buttons should be disabled at boundaries.
 */
export function useImageGallery(images: string[]): UseImageGalleryReturn {
  const currentIndex = ref(0);

  const totalImages = computed(() => images.length);

  const currentImage = computed(() => images[currentIndex.value] ?? '');

  function next() {
    if (currentIndex.value < images.length - 1) {
      currentIndex.value++;
    }
  }

  function previous() {
    if (currentIndex.value > 0) {
      currentIndex.value--;
    }
  }

  function goTo(index: number) {
    if (index >= 0 && index < images.length) {
      currentIndex.value = index;
    }
  }

  return {
    currentIndex,
    currentImage,
    totalImages,
    next,
    previous,
    goTo,
  };
}
