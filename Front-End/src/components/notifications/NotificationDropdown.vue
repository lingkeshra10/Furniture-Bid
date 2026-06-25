<script setup lang="ts">
import { useNotification } from '@/composables/useNotification';
import NotificationItem from './NotificationItem.vue';

/**
 * NotificationDropdown displays the 20 most recent notifications sorted by most recent first,
 * with load-more capability and a "Mark all as read" action.
 *
 * Requirements:
 * - 7.2: Display dropdown list of 20 most recent notifications sorted by most recent first,
 *         with ability to load older notifications
 * - 7.6: "Mark all as read" action resets unread count to zero
 */
const {
  recentNotifications,
  notifications,
  isLoading,
  hasMore,
  unreadCount,
  handleNotificationClick,
  markAllAsRead,
  loadMore,
} = useNotification();
</script>

<template>
  <div
    class="absolute right-0 mt-2 w-80 sm:w-96 bg-card rounded-lg shadow-lg border border-gray-200 z-50 overflow-hidden"
    role="menu"
    aria-label="Notifications"
  >
    <!-- Header -->
    <div class="flex items-center justify-between px-4 py-3 border-b border-gray-100 bg-background/50">
      <h3 class="text-sm font-semibold text-text">Notifications</h3>
      <button
        v-if="unreadCount > 0"
        class="text-xs font-medium text-primary hover:text-primary/80 transition-colors"
        @click="markAllAsRead"
      >
        Mark all as read
      </button>
    </div>

    <!-- Notification List -->
    <div class="max-h-96 overflow-y-auto">
      <!-- Loading State -->
      <div v-if="isLoading && notifications.length === 0" class="flex items-center justify-center py-8">
        <svg
          class="animate-spin h-5 w-5 text-primary"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
        >
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
        </svg>
      </div>

      <!-- Empty State -->
      <div
        v-else-if="!isLoading && notifications.length === 0"
        class="flex flex-col items-center justify-center py-8 px-4"
      >
        <svg
          class="h-10 w-10 text-gray-300 mb-2"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          stroke-width="1.5"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
          />
        </svg>
        <p class="text-sm text-gray-500">No notifications yet</p>
      </div>

      <!-- Notification Items -->
      <template v-else>
        <NotificationItem
          v-for="notification in notifications"
          :key="notification.id"
          :notification="notification"
          @click="handleNotificationClick(notification)"
        />
      </template>
    </div>

    <!-- Load More -->
    <div v-if="notifications.length > 0 && hasMore" class="border-t border-gray-100">
      <button
        class="w-full px-4 py-2.5 text-sm font-medium text-primary hover:bg-background transition-colors text-center"
        :disabled="isLoading"
        @click="loadMore"
      >
        <span v-if="isLoading">Loading...</span>
        <span v-else>Load more</span>
      </button>
    </div>
  </div>
</template>
