import { getApiClient } from './client';
import type { Notification, NotificationListQuery } from '@/types/notification';
import type { PaginatedResponse } from '@/types/common';

const client = getApiClient();

export const notificationService = {
  async getNotifications(query: NotificationListQuery): Promise<PaginatedResponse<Notification>> {
    const { data } = await client.get<PaginatedResponse<Notification>>('/notifications', {
      params: { page: query.page, pageSize: query.pageSize },
    });
    return data;
  },

  async markAsRead(id: string): Promise<void> {
    await client.put(`/notifications/${id}/read`);
  },

  async markAllAsRead(): Promise<void> {
    await client.put('/notifications/read-all');
  },
};
