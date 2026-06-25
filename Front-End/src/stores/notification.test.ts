import { describe, it, expect, vi, beforeEach } from 'vitest';
import type { Notification } from '@/types/notification';
import type { PaginatedResponse } from '@/types/common';

// Mock the notification service
vi.mock('@/services/api/notificationService', () => ({
  notificationService: {
    getNotifications: vi.fn(),
    markAsRead: vi.fn(),
    markAllAsRead: vi.fn(),
  },
}));

import { notificationService } from '@/services/api/notificationService';
import { useNotificationStore } from './notification';

function createNotification(overrides: Partial<Notification> = {}): Notification {
  return {
    id: 'notif-1',
    type: 'outbid',
    title: 'You have been outbid',
    message: 'Someone placed a higher bid',
    auctionId: 'auction-1',
    isRead: false,
    createdAt: '2024-01-01T00:00:00Z',
    ...overrides,
  };
}

describe('useNotificationStore', () => {
  let store: ReturnType<typeof useNotificationStore>;

  beforeEach(async () => {
    vi.clearAllMocks();
    // Reset module state by re-importing
    // Since state is module-level, we need to reset it manually
    store = useNotificationStore();
    // Reset state to clean slate
    store.notifications.value = [];
    store.unreadCount.value = 0;
    store.isLoading.value = false;
  });

  describe('initial state', () => {
    it('has empty notifications array', () => {
      expect(store.notifications.value).toEqual([]);
    });

    it('has zero unread count', () => {
      expect(store.unreadCount.value).toBe(0);
    });

    it('is not loading', () => {
      expect(store.isLoading.value).toBe(false);
    });
  });

  describe('displayCount getter', () => {
    it('returns "0" when unread count is zero', () => {
      store.unreadCount.value = 0;
      expect(store.displayCount.value).toBe('0');
    });

    it('returns string count when <= 99', () => {
      store.unreadCount.value = 5;
      expect(store.displayCount.value).toBe('5');
    });

    it('returns "99" when count is exactly 99', () => {
      store.unreadCount.value = 99;
      expect(store.displayCount.value).toBe('99');
    });

    it('returns "99+" when count exceeds 99', () => {
      store.unreadCount.value = 100;
      expect(store.displayCount.value).toBe('99+');
    });

    it('returns "99+" for large counts', () => {
      store.unreadCount.value = 500;
      expect(store.displayCount.value).toBe('99+');
    });
  });

  describe('recentNotifications getter', () => {
    it('returns empty array when no notifications', () => {
      expect(store.recentNotifications.value).toEqual([]);
    });

    it('returns all notifications when fewer than 20', () => {
      const notifs = Array.from({ length: 5 }, (_, i) =>
        createNotification({ id: `notif-${i}` })
      );
      store.notifications.value = notifs;
      expect(store.recentNotifications.value).toHaveLength(5);
    });

    it('returns only first 20 notifications when more exist', () => {
      const notifs = Array.from({ length: 30 }, (_, i) =>
        createNotification({ id: `notif-${i}` })
      );
      store.notifications.value = notifs;
      expect(store.recentNotifications.value).toHaveLength(20);
      expect(store.recentNotifications.value[0].id).toBe('notif-0');
      expect(store.recentNotifications.value[19].id).toBe('notif-19');
    });
  });

  describe('fetchNotifications', () => {
    it('sets notifications and unreadCount on first page fetch', async () => {
      const mockResponse: PaginatedResponse<Notification> = {
        data: [
          createNotification({ id: '1', isRead: false }),
          createNotification({ id: '2', isRead: true }),
          createNotification({ id: '3', isRead: false }),
        ],
        total: 3,
        page: 1,
        pageSize: 20,
        hasMore: false,
      };
      vi.mocked(notificationService.getNotifications).mockResolvedValue(mockResponse);

      await store.fetchNotifications({ page: 1, pageSize: 20 });

      expect(store.notifications.value).toHaveLength(3);
      expect(store.unreadCount.value).toBe(2);
      expect(store.isLoading.value).toBe(false);
    });

    it('appends notifications on subsequent pages', async () => {
      store.notifications.value = [createNotification({ id: 'existing' })];
      store.unreadCount.value = 1;

      const mockResponse: PaginatedResponse<Notification> = {
        data: [createNotification({ id: 'new-1' }), createNotification({ id: 'new-2' })],
        total: 3,
        page: 2,
        pageSize: 20,
        hasMore: false,
      };
      vi.mocked(notificationService.getNotifications).mockResolvedValue(mockResponse);

      await store.fetchNotifications({ page: 2, pageSize: 20 });

      expect(store.notifications.value).toHaveLength(3);
      expect(store.unreadCount.value).toBe(1); // unchanged on page > 1
    });

    it('sets isLoading during fetch', async () => {
      let resolvePromise!: (value: PaginatedResponse<Notification>) => void;
      const pendingPromise = new Promise<PaginatedResponse<Notification>>((resolve) => {
        resolvePromise = resolve;
      });
      vi.mocked(notificationService.getNotifications).mockReturnValue(pendingPromise);

      const fetchPromise = store.fetchNotifications({ page: 1, pageSize: 20 });
      expect(store.isLoading.value).toBe(true);

      resolvePromise({ data: [], total: 0, page: 1, pageSize: 20, hasMore: false });
      await fetchPromise;
      expect(store.isLoading.value).toBe(false);
    });

    it('resets isLoading on error', async () => {
      vi.mocked(notificationService.getNotifications).mockRejectedValue(new Error('Network error'));

      await expect(store.fetchNotifications({ page: 1, pageSize: 20 })).rejects.toThrow('Network error');
      expect(store.isLoading.value).toBe(false);
    });
  });

  describe('addNotification', () => {
    it('prepends notification to list', () => {
      store.notifications.value = [createNotification({ id: 'existing' })];

      store.addNotification(createNotification({ id: 'new' }));

      expect(store.notifications.value[0].id).toBe('new');
      expect(store.notifications.value).toHaveLength(2);
    });

    it('increments unreadCount for unread notification', () => {
      store.unreadCount.value = 3;
      store.addNotification(createNotification({ isRead: false }));
      expect(store.unreadCount.value).toBe(4);
    });

    it('does not increment unreadCount for already-read notification', () => {
      store.unreadCount.value = 3;
      store.addNotification(createNotification({ isRead: true }));
      expect(store.unreadCount.value).toBe(3);
    });
  });

  describe('markAsRead', () => {
    it('calls service and updates local notification state', async () => {
      vi.mocked(notificationService.markAsRead).mockResolvedValue(undefined);
      store.notifications.value = [
        createNotification({ id: 'notif-1', isRead: false }),
      ];
      store.unreadCount.value = 1;

      await store.markAsRead('notif-1');

      expect(notificationService.markAsRead).toHaveBeenCalledWith('notif-1');
      expect(store.notifications.value[0].isRead).toBe(true);
      expect(store.unreadCount.value).toBe(0);
    });

    it('does not decrement unreadCount if notification was already read', async () => {
      vi.mocked(notificationService.markAsRead).mockResolvedValue(undefined);
      store.notifications.value = [
        createNotification({ id: 'notif-1', isRead: true }),
      ];
      store.unreadCount.value = 0;

      await store.markAsRead('notif-1');

      expect(store.unreadCount.value).toBe(0);
    });

    it('does not decrement below zero', async () => {
      vi.mocked(notificationService.markAsRead).mockResolvedValue(undefined);
      store.notifications.value = [
        createNotification({ id: 'notif-1', isRead: false }),
      ];
      store.unreadCount.value = 0;

      await store.markAsRead('notif-1');

      expect(store.unreadCount.value).toBe(0);
    });
  });

  describe('markAllAsRead', () => {
    it('calls service and marks all notifications as read', async () => {
      vi.mocked(notificationService.markAllAsRead).mockResolvedValue(undefined);
      store.notifications.value = [
        createNotification({ id: '1', isRead: false }),
        createNotification({ id: '2', isRead: false }),
        createNotification({ id: '3', isRead: true }),
      ];
      store.unreadCount.value = 2;

      await store.markAllAsRead();

      expect(notificationService.markAllAsRead).toHaveBeenCalled();
      expect(store.notifications.value.every((n) => n.isRead)).toBe(true);
      expect(store.unreadCount.value).toBe(0);
    });
  });
});
