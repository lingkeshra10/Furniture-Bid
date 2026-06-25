<script setup lang="ts">
import { computed } from 'vue';
import type { Notification, NotificationType } from '@/types/notification';

/**
 * NotificationItem renders an individual notification with:
 * - Type-specific icon (outbid, auction-ending, auction-won, auction-lost, auto-bid-placed, auto-bid-limit-reached)
 * - Title, message, timestamp
 * - Read/unread visual state
 *
 * Requirements:
 * - 7.2: Each notification displays type icon, title, message, timestamp
 * - 7.5: Click navigates to relevant listing (handled by parent)
 */

const props = defineProps<{
  notification: Notification;
}>();

defineEmits<{
  click: [];
}>();

/** Map notification type to icon and color */
const iconConfig = computed(() => {
  const configs: Record<NotificationType, { path: string; color: string; bgColor: string }> = {
    'outbid': {
      path: 'M13 7h8m0 0v8m0-8l-8 8-4-4-6 6',
      color: 'text-red-500',
      bgColor: 'bg-red-50',
    },
    'auction-ending': {
      path: 'M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z',
      color: 'text-amber-500',
      bgColor: 'bg-amber-50',
    },
    'auction-won': {
      path: 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z',
      color: 'text-green-500',
      bgColor: 'bg-green-50',
    },
    'auction-lost': {
      path: 'M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z',
      color: 'text-gray-500',
      bgColor: 'bg-gray-50',
    },
    'auto-bid-placed': {
      path: 'M13 10V3L4 14h7v7l9-11h-7z',
      color: 'text-blue-500',
      bgColor: 'bg-blue-50',
    },
    'auto-bid-limit-reached': {
      path: 'M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z',
      color: 'text-orange-500',
      bgColor: 'bg-orange-50',
    },
  };
  return configs[props.notification.type] || configs['outbid'];
});

/** Format the notification timestamp as relative time */
const formattedTime = computed(() => {
  const date = new Date(props.notification.createdAt);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffMinutes = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMs / 3600000);
  const diffDays = Math.floor(diffMs / 86400000);

  if (diffMinutes < 1) return 'Just now';
  if (diffMinutes < 60) return `${diffMinutes}m ago`;
  if (diffHours < 24) return `${diffHours}h ago`;
  if (diffDays < 7) return `${diffDays}d ago`;
  return date.toLocaleDateString();
});
</script>

<template>
  <button
    class="w-full flex items-start gap-3 px-4 py-3 text-left transition-colors hover:bg-background/70 cursor-pointer border-b border-gray-50 last:border-b-0"
    :class="{ 'bg-blue-50/30': !notification.isRead }"
    role="menuitem"
    @click="$emit('click')"
  >
    <!-- Type Icon -->
    <div
      class="flex-shrink-0 w-9 h-9 rounded-full flex items-center justify-center mt-0.5"
      :class="iconConfig.bgColor"
    >
      <svg
        class="h-4.5 w-4.5"
        :class="iconConfig.color"
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
        stroke-width="2"
      >
        <path stroke-linecap="round" stroke-linejoin="round" :d="iconConfig.path" />
      </svg>
    </div>

    <!-- Content -->
    <div class="flex-1 min-w-0">
      <div class="flex items-start justify-between gap-2">
        <p
          class="text-sm leading-tight truncate"
          :class="notification.isRead ? 'font-normal text-text/80' : 'font-semibold text-text'"
        >
          {{ notification.title }}
        </p>
        <!-- Unread dot indicator -->
        <span
          v-if="!notification.isRead"
          class="flex-shrink-0 w-2 h-2 bg-accent rounded-full mt-1.5"
          aria-label="Unread"
        />
      </div>
      <p class="text-xs text-gray-500 mt-0.5 line-clamp-2">{{ notification.message }}</p>
      <p class="text-xs text-gray-400 mt-1">{{ formattedTime }}</p>
    </div>
  </button>
</template>
