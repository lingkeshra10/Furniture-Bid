import { ref, computed } from 'vue';
import type { Notification, NotificationListQuery } from '@/types/notification';
import { notificationService } from '@/services/api/notificationService';

/**
 * Notification store implemented as a singleton composable using Vue's reactivity system.
 * State is declared at module scope so it's shared across all component usages.
 *
 * Requirements:
 * - 7.1: Display bell icon showing unread count, displaying "99+" when count exceeds 99
 * - 7.4: When notification received, increment unread count
 * - 7.6: "Mark all as read" action resets unread count to zero
 * - 18.1: Stores organized by domain
 */

// --- Singleton reactive state (shared across all usages) ---
const notifications = ref<Notification[]>([]);
const unreadCount = ref(0);
const isLoading = ref(false);

// --- Getters ---

/** Display-friendly unread count: returns '99+' when count exceeds 99 (Req 7.1) */
const displayCount = computed(() =>
  unreadCount.value > 99 ? '99+' : String(unreadCount.value)
);

/** Most recent 20 notifications for quick-access display */
const recentNotifications = computed(() => notifications.value.slice(0, 20));

// --- Actions ---

/**
 * Fetch notifications from the API.
 * On first page (page === 1), replaces notifications and sets unreadCount.
 * On subsequent pages, appends to the existing list.
 */
async function fetchNotifications(query: NotificationListQuery): Promise<void> {
  isLoading.value = true;
  try {
    const response = await notificationService.getNotifications(query);
    if (query.page === 1) {
      notifications.value = response.data;
      unreadCount.value = response.data.filter((n) => !n.isRead).length;
    } else {
      notifications.value = [...notifications.value, ...response.data];
    }
  } finally {
    isLoading.value = false;
  }
}

/**
 * Add a new notification (e.g. from a WebSocket event).
 * Prepends to notifications array and increments unreadCount if unread (Req 7.4).
 */
function addNotification(notification: Notification): void {
  notifications.value = [notification, ...notifications.value];
  if (!notification.isRead) {
    unreadCount.value++;
  }
}

/**
 * Mark a single notification as read.
 * Calls the API, then updates local state.
 */
async function markAsRead(notificationId: string): Promise<void> {
  await notificationService.markAsRead(notificationId);
  const notification = notifications.value.find((n) => n.id === notificationId);
  if (notification && !notification.isRead) {
    notification.isRead = true;
    unreadCount.value = Math.max(0, unreadCount.value - 1);
  }
}

/**
 * Mark all notifications as read.
 * Calls the API, then resets all local read states (Req 7.6).
 */
async function markAllAsRead(): Promise<void> {
  await notificationService.markAllAsRead();
  notifications.value.forEach((n) => {
    n.isRead = true;
  });
  unreadCount.value = 0;
}

// --- Composable export ---

/**
 * Singleton composable for notification state management.
 * All component usages share the same reactive state.
 */
/**
 * Reset notification store to initial defaults.
 * Called on 401 unauthorized to clear sensitive session data.
 * Requirement 18.3
 */
function $reset(): void {
  notifications.value = [];
  unreadCount.value = 0;
  isLoading.value = false;
}

export function useNotificationStore() {
  return {
    // State
    notifications,
    unreadCount,
    isLoading,

    // Getters
    displayCount,
    recentNotifications,

    // Actions
    fetchNotifications,
    addNotification,
    markAsRead,
    markAllAsRead,
    $reset,
  };
}
