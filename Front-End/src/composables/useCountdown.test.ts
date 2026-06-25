import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { defineComponent, h } from 'vue';
import { mount } from '@vue/test-utils';
import { useCountdown } from './useCountdown';

describe('useCountdown', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  function mountComposable(endDate: string | Date) {
    let result: ReturnType<typeof useCountdown>;
    const TestComponent = defineComponent({
      setup() {
        result = useCountdown(endDate);
        return () => h('div');
      },
    });
    const wrapper = mount(TestComponent);
    return { result: result!, wrapper };
  }

  it('should compute remaining time correctly', () => {
    // Set 2 days, 3 hours, 15 minutes, 30 seconds in the future
    const now = Date.now();
    const futureMs = 2 * 86400000 + 3 * 3600000 + 15 * 60000 + 30 * 1000;
    const endDate = new Date(now + futureMs).toISOString();

    const { result } = mountComposable(endDate);

    expect(result.days.value).toBe(2);
    expect(result.hours.value).toBe(3);
    expect(result.minutes.value).toBe(15);
    expect(result.seconds.value).toBe(30);
    expect(result.isEnded.value).toBe(false);
  });

  it('should set isEnded to true when end date is in the past', () => {
    const pastDate = new Date(Date.now() - 1000).toISOString();

    const { result } = mountComposable(pastDate);

    expect(result.days.value).toBe(0);
    expect(result.hours.value).toBe(0);
    expect(result.minutes.value).toBe(0);
    expect(result.seconds.value).toBe(0);
    expect(result.isEnded.value).toBe(true);
  });

  it('should accept a Date object', () => {
    const futureDate = new Date(Date.now() + 60000); // 1 minute from now

    const { result } = mountComposable(futureDate);

    expect(result.isEnded.value).toBe(false);
    expect(result.minutes.value).toBe(1);
    expect(result.seconds.value).toBe(0);
  });

  it('should update countdown every second', () => {
    const endDate = new Date(Date.now() + 5000).toISOString(); // 5 seconds from now

    const { result } = mountComposable(endDate);

    expect(result.seconds.value).toBe(5);

    vi.advanceTimersByTime(1000);
    expect(result.seconds.value).toBe(4);

    vi.advanceTimersByTime(1000);
    expect(result.seconds.value).toBe(3);
  });

  it('should set isEnded to true when countdown reaches zero', () => {
    const endDate = new Date(Date.now() + 2000).toISOString(); // 2 seconds from now

    const { result } = mountComposable(endDate);

    expect(result.isEnded.value).toBe(false);

    vi.advanceTimersByTime(2000);
    expect(result.isEnded.value).toBe(true);
    expect(result.seconds.value).toBe(0);
  });

  it('should clear interval on unmount', () => {
    const clearIntervalSpy = vi.spyOn(globalThis, 'clearInterval');
    const endDate = new Date(Date.now() + 60000).toISOString();

    const { wrapper } = mountComposable(endDate);

    wrapper.unmount();

    expect(clearIntervalSpy).toHaveBeenCalled();
    clearIntervalSpy.mockRestore();
  });
});
