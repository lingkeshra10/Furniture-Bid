/**
 * Mock WebSocket event handlers that simulate real-time Socket.IO events
 * at configurable intervals when VITE_USE_MOCKS=true.
 *
 * Provides start/stop control and emits realistic bid updates, outbid
 * notifications, auction ending warnings, auction won/lost events,
 * and general notifications.
 *
 * Requirement: 15.4
 */

import type { BidUpdateEvent, AuctionEndEvent } from '@/types/common';
import type { Notification, NotificationType } from '@/types/notification';
import type { AuctionResult } from '@/types/auction';
import { mockListings, mockUsers, generateId } from './mockData';

// ─── Configuration ──────────────────────────────────────────────────────────

export interface MockSocketConfig {
  /** Interval in ms between bid update events (default: 5000) */
  bidUpdateInterval: number;
  /** Interval in ms between outbid notification events (default: 8000) */
  outbidInterval: number;
  /** Interval in ms between auction ending warning events (default: 15000) */
  auctionEndingInterval: number;
  /** Interval in ms between auction won/lost events (default: 20000) */
  auctionResultInterval: number;
  /** Interval in ms between general notification events (default: 10000) */
  notificationInterval: number;
}

const DEFAULT_CONFIG: MockSocketConfig = {
  bidUpdateInterval: 5000,
  outbidInterval: 8000,
  auctionEndingInterval: 15000,
  auctionResultInterval: 20000,
  notificationInterval: 10000,
};

// ─── Event Callback Types ───────────────────────────────────────────────────

type BidUpdateCallback = (event: BidUpdateEvent) => void;
type OutbidCallback = (event: { auctionId: string; currentBid: number }) => void;
type AuctionEndingCallback = (event: { auctionId: string; minutesRemaining: number }) => void;
type AuctionWonCallback = (event: AuctionEndEvent) => void;
type AuctionLostCallback = (event: AuctionEndEvent) => void;
type NotificationCallback = (notification: Notification) => void;

// ─── Helpers ────────────────────────────────────────────────────────────────

function randomInt(min: number, max: number): number {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function pick<T>(arr: readonly T[]): T {
  return arr[Math.floor(Math.random() * arr.length)];
}

function getActiveListings() {
  return mockListings.filter((l) => l.status === 'active');
}

function getBuyers() {
  return mockUsers.filter((u) => u.role === 'buyer');
}

// ─── Mock Socket Handler Class ──────────────────────────────────────────────

export class MockSocketHandler {
  private config: MockSocketConfig;
  private intervals: ReturnType<typeof setInterval>[] = [];
  private running = false;

  // Registered callbacks (mirrors SocketService subscription methods)
  private bidUpdateCallbacks: Map<string, BidUpdateCallback[]> = new Map();
  private outbidCallbacks: OutbidCallback[] = [];
  private auctionEndingCallbacks: AuctionEndingCallback[] = [];
  private auctionWonCallbacks: AuctionWonCallback[] = [];
  private auctionLostCallbacks: AuctionLostCallback[] = [];
  private notificationCallbacks: NotificationCallback[] = [];

  constructor(config: Partial<MockSocketConfig> = {}) {
    this.config = { ...DEFAULT_CONFIG, ...config };
  }

  // ─── Subscription Methods ───────────────────────────────────────────────

  onBidUpdate(auctionId: string, callback: BidUpdateCallback): void {
    if (!this.bidUpdateCallbacks.has(auctionId)) {
      this.bidUpdateCallbacks.set(auctionId, []);
    }
    this.bidUpdateCallbacks.get(auctionId)!.push(callback);
  }

  onOutbid(callback: OutbidCallback): void {
    this.outbidCallbacks.push(callback);
  }

  onAuctionEnding(callback: AuctionEndingCallback): void {
    this.auctionEndingCallbacks.push(callback);
  }

  onAuctionWon(callback: AuctionWonCallback): void {
    this.auctionWonCallbacks.push(callback);
  }

  onAuctionLost(callback: AuctionLostCallback): void {
    this.auctionLostCallbacks.push(callback);
  }

  onNotification(callback: NotificationCallback): void {
    this.notificationCallbacks.push(callback);
  }

  // ─── Lifecycle Control ──────────────────────────────────────────────────

  /**
   * Start emitting mock WebSocket events at configured intervals.
   */
  start(): void {
    if (this.running) return;
    this.running = true;

    this.intervals.push(
      setInterval(() => this.emitBidUpdate(), this.config.bidUpdateInterval),
    );
    this.intervals.push(
      setInterval(() => this.emitOutbid(), this.config.outbidInterval),
    );
    this.intervals.push(
      setInterval(() => this.emitAuctionEnding(), this.config.auctionEndingInterval),
    );
    this.intervals.push(
      setInterval(() => this.emitAuctionResult(), this.config.auctionResultInterval),
    );
    this.intervals.push(
      setInterval(() => this.emitNotification(), this.config.notificationInterval),
    );

    console.info('[MockSocket] Mock WebSocket event emitters started');
  }

  /**
   * Stop all mock event emitters and clean up callbacks.
   */
  stop(): void {
    if (!this.running) return;
    this.running = false;

    for (const interval of this.intervals) {
      clearInterval(interval);
    }
    this.intervals = [];

    console.info('[MockSocket] Mock WebSocket event emitters stopped');
  }

  /**
   * Remove all registered callbacks (equivalent to removeAllListeners).
   */
  removeAllListeners(): void {
    this.bidUpdateCallbacks.clear();
    this.outbidCallbacks = [];
    this.auctionEndingCallbacks = [];
    this.auctionWonCallbacks = [];
    this.auctionLostCallbacks = [];
    this.notificationCallbacks = [];
  }

  /**
   * Returns whether the mock handler is currently running.
   */
  isRunning(): boolean {
    return this.running;
  }

  // ─── Event Emitters ─────────────────────────────────────────────────────

  private emitBidUpdate(): void {
    const listings = getActiveListings();
    if (listings.length === 0) return;

    const listing = pick(listings);
    const buyers = getBuyers();
    const bidder = pick(buyers);
    const increment = randomInt(5, 50);

    listing.currentBid += increment;
    listing.bidCount += 1;

    const event: BidUpdateEvent = {
      auctionId: listing.id,
      currentBid: listing.currentBid,
      bidCount: listing.bidCount,
      bidderAlias: `Bidder${bidder.id.slice(-1)}`,
      timestamp: new Date().toISOString(),
    };

    // Emit to auction-specific subscribers
    const callbacks = this.bidUpdateCallbacks.get(listing.id);
    if (callbacks) {
      for (const cb of callbacks) {
        cb(event);
      }
    }
  }

  private emitOutbid(): void {
    const listings = getActiveListings();
    if (listings.length === 0 || this.outbidCallbacks.length === 0) return;

    const listing = pick(listings);
    const event = {
      auctionId: listing.id,
      currentBid: listing.currentBid,
    };

    for (const cb of this.outbidCallbacks) {
      cb(event);
    }
  }

  private emitAuctionEnding(): void {
    const listings = getActiveListings();
    if (listings.length === 0 || this.auctionEndingCallbacks.length === 0) return;

    const listing = pick(listings);
    const minutesRemaining = pick([60, 30, 15, 10, 5, 1]);

    const event = {
      auctionId: listing.id,
      minutesRemaining,
    };

    for (const cb of this.auctionEndingCallbacks) {
      cb(event);
    }
  }

  private emitAuctionResult(): void {
    const listings = getActiveListings();
    if (listings.length === 0) return;

    const listing = pick(listings);
    const buyers = getBuyers();
    const winner = pick(buyers);

    const results: AuctionResult[] = ['won', 'lost', 'reserve-not-met'];
    const result = pick(results);

    const event: AuctionEndEvent = {
      auctionId: listing.id,
      result,
      winningBid: listing.currentBid,
      winnerId: winner.id,
    };

    if (result === 'won') {
      for (const cb of this.auctionWonCallbacks) {
        cb(event);
      }
    } else {
      for (const cb of this.auctionLostCallbacks) {
        cb(event);
      }
    }
  }

  private emitNotification(): void {
    if (this.notificationCallbacks.length === 0) return;

    const listings = getActiveListings();
    if (listings.length === 0) return;

    const listing = pick(listings);
    const notificationTypes: NotificationType[] = [
      'outbid',
      'auction-ending',
      'auction-won',
      'auction-lost',
      'auto-bid-placed',
      'auto-bid-limit-reached',
    ];
    const type = pick(notificationTypes);

    const notification: Notification = {
      id: generateId(),
      type,
      title: getNotificationTitle(type),
      message: getNotificationMessage(type, listing.title, listing.currentBid),
      auctionId: listing.id,
      isRead: false,
      createdAt: new Date().toISOString(),
    };

    for (const cb of this.notificationCallbacks) {
      cb(notification);
    }
  }
}

// ─── Notification Content Generators ────────────────────────────────────────

function getNotificationTitle(type: NotificationType): string {
  switch (type) {
    case 'outbid':
      return 'You have been outbid';
    case 'auction-ending':
      return 'Auction ending soon';
    case 'auction-won':
      return 'Congratulations! You won';
    case 'auction-lost':
      return 'Auction ended';
    case 'auto-bid-placed':
      return 'Auto-bid placed';
    case 'auto-bid-limit-reached':
      return 'Auto-bid limit reached';
  }
}

function getNotificationMessage(
  type: NotificationType,
  listingTitle: string,
  currentBid: number,
): string {
  switch (type) {
    case 'outbid':
      return `Someone placed a higher bid on "${listingTitle}". Current bid is now $${currentBid}.`;
    case 'auction-ending':
      return `The auction for "${listingTitle}" ends in ${pick([1, 5, 15, 30])} minutes.`;
    case 'auction-won':
      return `You won the auction for "${listingTitle}" with a bid of $${currentBid}.`;
    case 'auction-lost':
      return `The auction for "${listingTitle}" has ended. You were outbid.`;
    case 'auto-bid-placed':
      return `Your auto-bid placed $${currentBid} on "${listingTitle}".`;
    case 'auto-bid-limit-reached':
      return `Your maximum auto-bid amount has been reached on "${listingTitle}".`;
  }
}

// ─── Singleton Instance ─────────────────────────────────────────────────────

let mockSocketInstance: MockSocketHandler | null = null;

/**
 * Returns the singleton MockSocketHandler instance.
 * Creates one with default config if it doesn't exist.
 */
export function getMockSocketHandler(config?: Partial<MockSocketConfig>): MockSocketHandler {
  if (!mockSocketInstance) {
    mockSocketInstance = new MockSocketHandler(config);
  }
  return mockSocketInstance;
}

/**
 * Initializes and starts the mock socket handler.
 * Call this from the SocketService.initMockMode() method.
 */
export function initMockSocketHandlers(config?: Partial<MockSocketConfig>): MockSocketHandler {
  const handler = getMockSocketHandler(config);
  handler.start();
  return handler;
}

/**
 * Stops and cleans up the mock socket handler.
 */
export function stopMockSocketHandlers(): void {
  if (mockSocketInstance) {
    mockSocketInstance.stop();
    mockSocketInstance.removeAllListeners();
  }
}
