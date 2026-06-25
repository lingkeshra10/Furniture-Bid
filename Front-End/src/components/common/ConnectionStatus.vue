<script setup lang="ts">
import { socketService } from '@/services/websocket/socketClient';

const connectionStatus = socketService.getConnectionStatus();
</script>

<template>
  <div
    v-if="connectionStatus !== 'connected'"
    role="alert"
    aria-live="polite"
    class="w-full text-center text-sm font-medium py-2 px-4"
    :class="{
      'bg-amber-100 text-amber-800': connectionStatus === 'reconnecting',
      'bg-red-100 text-red-800': connectionStatus === 'disconnected',
    }"
  >
    <span v-if="connectionStatus === 'reconnecting'" class="inline-flex items-center gap-2">
      <svg class="animate-spin h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
      </svg>
      Reconnecting…
    </span>
    <span v-else class="inline-flex items-center gap-2">
      <svg class="h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M18.364 5.636a9 9 0 010 12.728M5.636 18.364a9 9 0 010-12.728M8.464 15.536a5 5 0 010-7.072M15.536 8.464a5 5 0 010 7.072M12 12h.01" />
      </svg>
      Connection lost. Please check your internet connection.
    </span>
  </div>
</template>
