import { ref, onScopeDispose } from 'vue';

export interface UseCountdownReturn {
  days: ReturnType<typeof ref<number>>;
  hours: ReturnType<typeof ref<number>>;
  minutes: ReturnType<typeof ref<number>>;
  seconds: ReturnType<typeof ref<number>>;
  isEnded: ReturnType<typeof ref<boolean>>;
}

/**
 * Composable for reactive countdown timer.
 * Accepts an end date (ISO string or Date) and returns reactive refs
 * for days, hours, minutes, seconds remaining, and isEnded flag.
 * Automatically updates every second and cleans up on scope disposal.
 */
export function useCountdown(endDate: string | Date): UseCountdownReturn {
  const targetTime = typeof endDate === 'string' ? new Date(endDate).getTime() : endDate.getTime();

  const days = ref(0);
  const hours = ref(0);
  const minutes = ref(0);
  const seconds = ref(0);
  const isEnded = ref(false);

  function update() {
    const now = Date.now();
    const remaining = targetTime - now;

    if (remaining <= 0) {
      days.value = 0;
      hours.value = 0;
      minutes.value = 0;
      seconds.value = 0;
      isEnded.value = true;
      return;
    }

    const totalSeconds = Math.floor(remaining / 1000);
    days.value = Math.floor(totalSeconds / 86400);
    hours.value = Math.floor((totalSeconds % 86400) / 3600);
    minutes.value = Math.floor((totalSeconds % 3600) / 60);
    seconds.value = totalSeconds % 60;
    isEnded.value = false;
  }

  // Initial update
  update();

  // Set up interval for 1-second updates
  const intervalId = setInterval(() => {
    update();
    if (isEnded.value) {
      clearInterval(intervalId);
    }
  }, 1000);

  // Clean up interval on scope disposal
  onScopeDispose(() => {
    clearInterval(intervalId);
  });

  return {
    days,
    hours,
    minutes,
    seconds,
    isEnded,
  };
}
