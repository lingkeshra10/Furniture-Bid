<script setup lang="ts">
import { useToast } from '@/composables/useToast';

const { toasts, dismissToast } = useToast();

function getTypeClasses(type: string): string {
  switch (type) {
    case 'success':
      return 'bg-green-50 border-green-400 text-green-800';
    case 'warning':
      return 'bg-amber-50 border-amber-400 text-amber-800';
    case 'error':
      return 'bg-red-50 border-red-400 text-red-800';
    case 'info':
      return 'bg-blue-50 border-blue-400 text-blue-800';
    default:
      return 'bg-gray-50 border-gray-400 text-gray-800';
  }
}

function getIconPath(type: string): string {
  switch (type) {
    case 'success':
      return 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z';
    case 'warning':
      return 'M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z';
    case 'error':
      return 'M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z';
    case 'info':
      return 'M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z';
    default:
      return 'M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z';
  }
}
</script>

<template>
  <div
    class="fixed top-4 right-4 z-[9999] flex flex-col gap-3 pointer-events-none max-w-sm w-full"
    aria-live="polite"
    aria-atomic="false"
  >
    <TransitionGroup
      enter-active-class="transition ease-out duration-300"
      enter-from-class="opacity-0 translate-x-4"
      enter-to-class="opacity-100 translate-x-0"
      leave-active-class="transition ease-in duration-200"
      leave-from-class="opacity-100 translate-x-0"
      leave-to-class="opacity-0 translate-x-4"
    >
      <div
        v-for="toast in toasts"
        :key="toast.id"
        class="pointer-events-auto flex items-start gap-3 p-4 rounded-lg border shadow-lg"
        :class="getTypeClasses(toast.type)"
        role="alert"
      >
        <!-- Icon -->
        <svg
          class="h-5 w-5 flex-shrink-0 mt-0.5"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          stroke-width="2"
        >
          <path stroke-linecap="round" stroke-linejoin="round" :d="getIconPath(toast.type)" />
        </svg>

        <!-- Message -->
        <p class="flex-1 text-sm font-medium">{{ toast.message }}</p>

        <!-- Dismiss button -->
        <button
          class="flex-shrink-0 p-1 rounded-md hover:bg-black/10 transition-colors min-w-touch min-h-touch flex items-center justify-center -m-1"
          :aria-label="`Dismiss ${toast.type} notification`"
          @click="dismissToast(toast.id)"
        >
          <svg class="h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>
