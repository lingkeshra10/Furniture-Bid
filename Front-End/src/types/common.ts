import type { AuctionResult } from './auction';

export interface PaginatedResponse<T> {
  data: T[];
  total: number;
  page: number;
  pageSize: number;
  hasMore: boolean;
}

export interface ApiError {
  statusCode: number;
  errorCode: string;
  message: string;
  fieldErrors?: Record<string, string>;
}

export type ConnectionStatus = 'connected' | 'reconnecting' | 'disconnected';

export interface WebSocketEvent<T = unknown> {
  event: string;
  payload: T;
  timestamp: string;
}

export interface BidUpdateEvent {
  auctionId: string;
  currentBid: number;
  bidCount: number;
  bidderAlias: string;
  timestamp: string;
}

export interface AuctionEndEvent {
  auctionId: string;
  result: AuctionResult;
  winningBid?: number;
  winnerId?: string;
}
