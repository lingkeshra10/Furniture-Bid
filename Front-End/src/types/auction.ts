export type AuctionResult = 'won' | 'lost' | 'reserve-not-met' | 'active';

export interface Bid {
  id: string;
  auctionId: string;
  bidderId: string;
  bidderAlias: string;       // anonymized identifier
  amount: number;
  timestamp: string;         // ISO 8601
}

export interface PlaceBidRequest {
  auctionId: string;
  amount: number;
}

export interface PlaceBidResponse {
  success: boolean;
  bid?: Bid;
  error?: string;
}

export interface AutoBidConfig {
  auctionId: string;
  maxAmount: number;
  isActive: boolean;
}

export interface AutoBidRequest {
  auctionId: string;
  maxAmount: number;
}

export interface BidHistoryQuery {
  auctionId: string;
  page: number;
  pageSize: number;          // default 20
}

export type UserBidStatus = 'winning' | 'outbid' | 'won' | 'lost';

export interface UserBidHistoryItem {
  id: string;
  auctionId: string;
  auctionTitle: string;
  amount: number;
  timestamp: string;         // ISO 8601
  status: UserBidStatus;
}

export interface SellerActiveListing {
  id: string;
  title: string;
  currentBid: number;
  bidCount: number;
  timeRemaining: number;     // milliseconds
}

export interface SellerCompletedAuction {
  id: string;
  title: string;
  winningBid: number;
  winnerDisplayName: string;
  reserveMet: boolean;
  endedAt: string;
}
