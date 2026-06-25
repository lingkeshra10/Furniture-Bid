import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useNotificationStore } from '@/stores/notification';
import type { Notification } from '@/types/notification';

/**
 * Composable for notification management.
 * Provides receive, mark-read, count formatting, and navigation helpers.
 *
 * Requirements:
 * - 7.1: Bell icon shows unread count, "99+" when count exceeds 99
 * - 7.2: Dropdown shows 20 most recent notifications, load-more capability
 * - 7.3: Real-time notifications received via WebSocket
 * - 7.5: Click notification → navigate to listing and mark as read
 * - 7.6: "Mark all as read" resets unread count to zero
 */
export function useNotification() {
  const router = useRouter();
  const store = useNotificationStore();

  const isDropdownOpen = ref(false);
  const currentPage = ref(1);
  const hasMore = ref(true);
  const pageSize = 20;

  /**
   * Toggle the notification dropdown visibility.
   * On open, fetches the first page of notifications.
   */
  async function toggleDropdown() {
    isDropdownOpen.value = !isDropdownOpen.value;
    if (isDropdownOpen.value) {
      currentPage.value = 1;
      hasMore.value = true;
      await store.fetchNotifications({ page: 1, pageSize });
    }
  }

  /** Close the dropdown */
  function closeDropdown() {
    isDropdownOpen.value = false;
  }

  /**
   * Load more (older) notifications.
   * Appends to the existing list via the store.
   */
  async function loadMore() {
    currentPage.value++;
    await store.fetchNotifications({ page: currentPage.value, pageSize });
    // If fewer than pageSize items returned, no more pages
    if (store.notifications.value.length < currentPage.value * pageSize) {
      hasMore.value = false;
    }
  }

  /**
   * Handle receiving a new real-time notification.
   * Adds it to the store (which increments unread count).
   */
  function receiveNotification(notification: Notification) {
    store.addNotification(notification);
  }

  /**
   * Handle clicking on a notification.
   * Marks it as read and navigates to the relevant listing page (Req 7.5).
   */
  async function handleNotificationClick(notification: Notification) {
    if (!notification.isRead) {
      await store.markAsRead(notification.id);
    }
    closeDropdown();
    router.push(`/listing/${notification.auctionId}`);
  }

  /**
   * Mark all notifications as read (Req 7.6).
   */
  async function markAllAsRead() {
    await store.markAllAsRead();
  }

  /**
   * Format the unread count for display (Req 7.1).
   * Returns numeric string for counts ≤ 99, "99+" otherwise.
   */
  function formatCount(count: number): string {
    return count > 99 ? '99+' : String(count);
  }

  return {
    // State
    isDropdownOpen,
    currentPage,
    hasMore,

    // Store state (reactive)
    notifications: store.notifications,
    unreadCount: store.unreadCount,
    displayCount: store.displayCount,
    recentNotifications: store.recentNotifications,
    isLoading: store.isLoading,

    // Actions
    toggleDropdown,
    closeDropdown,
    loadMore,
    receiveNotification,
    handleNotificationClick,
    markAllAsRead,
    formatCount,
  };
}
