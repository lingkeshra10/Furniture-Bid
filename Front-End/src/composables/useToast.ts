import { ref, type Ref } from 'vue';

export type ToastType = 'success' | 'warning' | 'error' | 'info';

export interface Toast {
  id: string;
  message: string;
  type: ToastType;
  duration: number;
}

const DEFAULT_DURATIONS: Record<ToastType, number> = {
  success: 3000,
  warning: 5000,
  error: 5000,
  info: 4000,
};

const MAX_VISIBLE = 3;

let idCounter = 0;

const toasts: Ref<Toast[]> = ref([]);

function generateId(): string {
  return `toast-${++idCounter}-${Date.now()}`;
}

function showToast(message: string, type: ToastType, duration?: number): void {
  const toast: Toast = {
    id: generateId(),
    message,
    type,
    duration: duration ?? DEFAULT_DURATIONS[type],
  };

  toasts.value.push(toast);

  // Enforce max visible limit — remove oldest when exceeded
  while (toasts.value.length > MAX_VISIBLE) {
    toasts.value.shift();
  }

  // Auto-dismiss after duration
  if (toast.duration > 0) {
    setTimeout(() => {
      dismissToast(toast.id);
    }, toast.duration);
  }
}

function dismissToast(id: string): void {
  const index = toasts.value.findIndex((t) => t.id === id);
  if (index !== -1) {
    toasts.value.splice(index, 1);
  }
}

export function useToast() {
  return {
    toasts,
    showToast,
    dismissToast,
  };
}
