import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { ref, nextTick, defineComponent, h } from 'vue';
import { mount } from '@vue/test-utils';
import { useInfiniteScroll } from './useInfiniteScroll';

// Mock IntersectionObserver as a proper class
let mockObserverCallback: IntersectionObserverCallback;
let mockObserverInstance: {
  observe: ReturnType<typeof vi.fn>;
  unobserve: ReturnType<typeof vi.fn>;
  disconnect: ReturnType<typeof vi.fn>;
};

class MockIntersectionObserver {
  constructor(callback: IntersectionObserverCallback, public options?: IntersectionObserverInit) {
    mockObserverCallback = callback;
    mockObserverInstance = {
      observe: vi.fn(),
      unobserve: vi.fn(),
      disconnect: vi.fn(),
    };
    Object.assign(this, mockObserverInstance);
  }
  observe = vi.fn();
  unobserve = vi.fn();
  disconnect = vi.fn();
}

describe('useInfiniteScroll', () => {
  let originalIntersectionObserver: typeof IntersectionObserver;

  beforeEach(() => {
    originalIntersectionObserver = globalThis.IntersectionObserver;
    globalThis.IntersectionObserver = MockIntersectionObserver as unknown as typeof IntersectionObserver;
  });

  afterEach(() => {
    globalThis.IntersectionObserver = originalIntersectionObserver;
  });

  function mountComposable(options: { enabled: ReturnType<typeof ref<boolean>>; onLoadMore: () => void; rootMargin?: string }) {
    let result: ReturnType<typeof useInfiniteScroll>;
    const TestComponent = defineComponent({
      setup() {
        result = useInfiniteScroll({
          onLoadMore: options.onLoadMore,
          enabled: options.enabled,
          rootMargin: options.rootMargin,
        });
        return () => h('div');
      },
    });
    const wrapper = mount(TestComponent);
    return { result: result!, wrapper };
  }

  it('should return a sentinelRef', () => {
    const enabled = ref(true);
    const onLoadMore = vi.fn();

    const { result } = mountComposable({ enabled, onLoadMore });

    expect(result.sentinelRef).toBeDefined();
    expect(result.sentinelRef.value).toBeNull();
  });

  it('should create an IntersectionObserver when sentinelRef is set', async () => {
    const enabled = ref(true);
    const onLoadMore = vi.fn();

    const { result } = mountComposable({ enabled, onLoadMore });

    const mockElement = document.createElement('div');
    result.sentinelRef.value = mockElement;
    await nextTick();

    expect(mockObserverInstance.observe).toHaveBeenCalledWith(mockElement);
  });

  it('should call onLoadMore when sentinel intersects and enabled is true', async () => {
    const enabled = ref(true);
    const onLoadMore = vi.fn();

    const { result } = mountComposable({ enabled, onLoadMore });

    const mockElement = document.createElement('div');
    result.sentinelRef.value = mockElement;
    await nextTick();

    // Simulate intersection
    mockObserverCallback(
      [{ isIntersecting: true } as IntersectionObserverEntry],
      {} as IntersectionObserver
    );

    expect(onLoadMore).toHaveBeenCalledTimes(1);
  });

  it('should not call onLoadMore when enabled is false', async () => {
    const enabled = ref(false);
    const onLoadMore = vi.fn();

    const { result } = mountComposable({ enabled, onLoadMore });

    const mockElement = document.createElement('div');
    result.sentinelRef.value = mockElement;
    await nextTick();

    // Simulate intersection
    mockObserverCallback(
      [{ isIntersecting: true } as IntersectionObserverEntry],
      {} as IntersectionObserver
    );

    expect(onLoadMore).not.toHaveBeenCalled();
  });

  it('should not call onLoadMore when sentinel is not intersecting', async () => {
    const enabled = ref(true);
    const onLoadMore = vi.fn();

    const { result } = mountComposable({ enabled, onLoadMore });

    const mockElement = document.createElement('div');
    result.sentinelRef.value = mockElement;
    await nextTick();

    // Simulate non-intersection
    mockObserverCallback(
      [{ isIntersecting: false } as IntersectionObserverEntry],
      {} as IntersectionObserver
    );

    expect(onLoadMore).not.toHaveBeenCalled();
  });

  it('should disconnect observer when component unmounts', async () => {
    const enabled = ref(true);
    const onLoadMore = vi.fn();

    const { result, wrapper } = mountComposable({ enabled, onLoadMore });

    const mockElement = document.createElement('div');
    result.sentinelRef.value = mockElement;
    await nextTick();

    wrapper.unmount();

    expect(mockObserverInstance.disconnect).toHaveBeenCalled();
  });
});
