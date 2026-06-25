export type NotificationType =
  | 'outbid'
  | 'auction-ending'
  | 'auction-won'
  | 'auction-lost'
  | 'auto-bid-placed'
  | 'auto-bid-limit-reached';

export interface Notification {
  id: string;
  type: NotificationType;
  title: string;
  message: string;
  auctionId: string;
  isRead: boolean;
  createdAt: string;
}

export interface NotificationListQuery {
  page: number;
  pageSize: number;          // default 20
}
