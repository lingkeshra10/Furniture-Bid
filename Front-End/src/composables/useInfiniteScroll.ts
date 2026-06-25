import { ref, watch, onUnmounted, type Ref } from 'vue';

export interface UseInfiniteScrollOptions {
  /** Callback triggered when the sentinel becomes visible */
  onLoadMore: () => void | Promise<void>;
  /** Reactive flag to enable/disable the observer */
  enabled: Ref<boolean>;
  /** Root margin for IntersectionObserver (default: '200px') */
  rootMargin?: string;
}

/**
 * Composable for infinite scroll using IntersectionObserver.
 * Returns a sentinel ref that should be placed at the bottom of the scrollable content.
 * When the sentinel enters the viewport (with rootMargin buffer), onLoadMore is called.
 */
export function useInfiniteScroll(options: UseInfiniteScrollOptions) {
  const { onLoadMore, enabled, rootMargin = '200px' } = options;
  const sentinelRef = ref<HTMLElement | null>(null);

  let observer: IntersectionObserver | null = null;

  function createObserver() {
    if (!sentinelRef.value) return;

    observer = new IntersectionObserver(
      (entries) => {
        const entry = entries[0];
        if (entry && entry.isIntersecting && enabled.value) {
          onLoadMore();
        }
      },
      {
        rootMargin,
        threshold: 0,
      }
    );

    observer.observe(sentinelRef.value);
  }

  function destroyObserver() {
    if (observer) {
      observer.disconnect();
      observer = null;
    }
  }

  // Watch for sentinel element changes (when it mounts/unmounts)
  watch(sentinelRef, (newEl, oldEl) => {
    if (oldEl && observer) {
      observer.unobserve(oldEl);
    }
    if (newEl) {
      destroyObserver();
      createObserver();
    }
  });

  // Watch for enabled changes — reconnect observer when re-enabled
  watch(enabled, (isEnabled) => {
    if (isEnabled && sentinelRef.value && !observer) {
      createObserver();
    }
  });

  onUnmounted(() => {
    destroyObserver();
  });

  return {
    sentinelRef,
  };
}
