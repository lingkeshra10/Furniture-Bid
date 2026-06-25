import type { BidUpdateEvent, AuctionEndEvent } from '@/types/common';
import type { Notification } from '@/types/notification';

/**
 * Socket.IO event name constants used for typed subscriptions.
 */
export const SocketEvents = {
  // Server → Client events
  BID_UPDATE: 'bid:update',
  OUTBID: 'outbid',
  AUCTION_ENDING: 'auction:ending',
  AUCTION_WON: 'auction:won',
  AUCTION_LOST: 'auction:lost',
  NOTIFICATION: 'notification',

  // Client → Server events (room management)
  JOIN_AUCTION: 'join:auction',
  LEAVE_AUCTION: 'leave:auction',
  SUBSCRIBE_NOTIFICATIONS: 'subscribe:notifications',
} as const;

/**
 * Payload types for each server-to-client event.
 */
export interface OutbidPayload {
  auctionId: string;
  currentBid: number;
}

export interface AuctionEndingPayload {
  auctionId: string;
  minutesRemaining: number;
}

/**
 * Map of event names to their payload types for type-safe subscriptions.
 */
export interface ServerToClientEvents {
  [SocketEvents.BID_UPDATE]: (event: BidUpdateEvent) => void;
  [SocketEvents.OUTBID]: (event: OutbidPayload) => void;
  [SocketEvents.AUCTION_ENDING]: (event: AuctionEndingPayload) => void;
  [SocketEvents.AUCTION_WON]: (event: AuctionEndEvent) => void;
  [SocketEvents.AUCTION_LOST]: (event: AuctionEndEvent) => void;
  [SocketEvents.NOTIFICATION]: (notification: Notification) => void;
}

export interface ClientToServerEvents {
  [SocketEvents.JOIN_AUCTION]: (payload: { auctionId: string }) => void;
  [SocketEvents.LEAVE_AUCTION]: (payload: { auctionId: string }) => void;
  [SocketEvents.SUBSCRIBE_NOTIFICATIONS]: (payload: { userId: string }) => void;
}
