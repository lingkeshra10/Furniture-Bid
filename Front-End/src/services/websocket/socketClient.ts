import { ref, type Ref } from 'vue';
import { io, type Socket } from 'socket.io-client';
import type { BidUpdateEvent, AuctionEndEvent, ConnectionStatus } from '@/types/common';
import type { Notification } from '@/types/notification';
import {
  SocketEvents,
  type OutbidPayload,
  type AuctionEndingPayload,
} from './events';

/**
 * Singleton WebSocket service wrapping Socket.IO with:
 * - JWT-authenticated connections
 * - Typed event subscriptions
 * - Room management with automatic resubscription on reconnect
 * - Reactive connection status
 * - Exponential backoff reconnection (1s → 30s, max 10 attempts)
 * - Token expiry handling
 *
 * Validates: Requirements 15.1, 15.2, 15.3, 15.5
 */
class SocketService {
  private static instance: SocketService;
  private socket: Socket | null = null;
  private subscribedRooms: Set<string> = new Set();
  private connectionStatus = ref<ConnectionStatus>('disconnected');
  private reconnectAttempts = 0;
  private readonly maxReconnectAttempts = 10;

  private constructor() {
    // Private constructor enforces singleton pattern
  }

  static getInstance(): SocketService {
    if (!SocketService.instance) {
      SocketService.instance = new SocketService();
    }
    return SocketService.instance;
  }

  /**
   * Establish a WebSocket connection using the provided JWT token.
   * In mock mode (VITE_USE_MOCKS=true), sets connected status without
   * opening a real socket — actual mock event handlers live elsewhere.
   */
  connect(token: string): void {
    // Disconnect any existing connection first
    if (this.socket) {
      this.disconnect();
    }

    if (import.meta.env.VITE_USE_MOCKS === 'true') {
      this.connectionStatus.value = 'connected';
      return;
    }

    const wsUrl = import.meta.env.VITE_WS_URL || 'http://localhost:3000';

    this.socket = io(wsUrl, {
      auth: { token },
      reconnection: true,
      reconnectionDelay: 1000,
      reconnectionDelayMax: 30000,
      reconnectionAttempts: this.maxReconnectAttempts,
    });

    this.setupConnectionListeners(token);
  }

  /**
   * Disconnect the socket, clear all listeners, and reset state.
   * Prevents memory leaks by cleaning up subscriptions.
   */
  disconnect(): void {
    this.subscribedRooms.clear();
    if (this.socket) {
      this.socket.removeAllListeners();
      this.socket.disconnect();
      this.socket = null;
    }
    this.connectionStatus.value = 'disconnected';
    this.reconnectAttempts = 0;
  }

  // ─── Typed Event Subscriptions ─────────────────────────────────────────

  /**
   * Subscribe to bid updates for a specific auction.
   */
  onBidUpdate(auctionId: string, callback: (event: BidUpdateEvent) => void): void {
    this.socket?.on(`${SocketEvents.BID_UPDATE}:${auctionId}`, callback);
  }

  /**
   * Subscribe to outbid notifications for the current user.
   */
  onOutbid(callback: (event: OutbidPayload) => void): void {
    this.socket?.on(SocketEvents.OUTBID, callback);
  }

  /**
   * Subscribe to auction ending soon notifications.
   */
  onAuctionEnding(callback: (event: AuctionEndingPayload) => void): void {
    this.socket?.on(SocketEvents.AUCTION_ENDING, callback);
  }

  /**
   * Subscribe to auction won events.
   */
  onAuctionWon(callback: (event: AuctionEndEvent) => void): void {
    this.socket?.on(SocketEvents.AUCTION_WON, callback);
  }

  /**
   * Subscribe to auction lost events.
   */
  onAuctionLost(callback: (event: AuctionEndEvent) => void): void {
    this.socket?.on(SocketEvents.AUCTION_LOST, callback);
  }

  /**
   * Subscribe to general notification events.
   */
  onNotification(callback: (notification: Notification) => void): void {
    this.socket?.on(SocketEvents.NOTIFICATION, callback);
  }

  // ─── Room Management ───────────────────────────────────────────────────

  /**
   * Join a specific auction room to receive bid updates.
   */
  joinAuctionRoom(auctionId: string): void {
    this.socket?.emit(SocketEvents.JOIN_AUCTION, { auctionId });
    this.subscribedRooms.add(`auction:${auctionId}`);
  }

  /**
   * Leave an auction room and stop receiving bid updates.
   */
  leaveAuctionRoom(auctionId: string): void {
    this.socket?.emit(SocketEvents.LEAVE_AUCTION, { auctionId });
    this.subscribedRooms.delete(`auction:${auctionId}`);
  }

  /**
   * Subscribe to the user's notification channel.
   */
  subscribeNotifications(userId: string): void {
    this.socket?.emit(SocketEvents.SUBSCRIBE_NOTIFICATIONS, { userId });
    this.subscribedRooms.add(`notifications:${userId}`);
  }

  // ─── Connection Status ─────────────────────────────────────────────────

  /**
   * Reactive connection status for UI components to observe.
   */
  getConnectionStatus(): Ref<ConnectionStatus> {
    return this.connectionStatus;
  }

  // ─── Private Helpers ───────────────────────────────────────────────────

  private setupConnectionListeners(token: string): void {
    if (!this.socket) return;

    this.socket.on('connect', () => {
      this.connectionStatus.value = 'connected';
      this.reconnectAttempts = 0;
      this.resubscribeRooms();
    });

    this.socket.on('disconnect', () => {
      this.connectionStatus.value = 'reconnecting';
    });

    this.socket.on('reconnect_attempt', (attempt: number) => {
      this.reconnectAttempts = attempt;
      this.connectionStatus.value = 'reconnecting';
    });

    this.socket.on('reconnect_failed', () => {
      this.connectionStatus.value = 'disconnected';
    });

    this.socket.on('connect_error', (error: Error) => {
      if (error.message === 'jwt expired') {
        this.handleTokenExpired(token);
      }
    });
  }

  /**
   * Re-emit room join events after a reconnection so the server
   * knows which rooms this client should be in.
   */
  private resubscribeRooms(): void {
    for (const room of this.subscribedRooms) {
      const [type, ...idParts] = room.split(':');
      const id = idParts.join(':');
      if (type === 'auction') {
        this.socket?.emit(SocketEvents.JOIN_AUCTION, { auctionId: id });
      } else if (type === 'notifications') {
        this.socket?.emit(SocketEvents.SUBSCRIBE_NOTIFICATIONS, { userId: id });
      }
    }
  }

  /**
   * Handle JWT expiry by attempting a token refresh via the auth store.
   * If refresh succeeds, reconnect with the new token.
   * If refresh fails, disconnect and let the auth flow handle logout.
   */
  private async handleTokenExpired(_previousToken: string): Promise<void> {
    try {
      // Dynamic import to avoid circular dependency with store
      const { useAuthStore } = await import('@/stores/auth');
      const authStore = useAuthStore();
      await authStore.refreshToken();
      if (authStore.token) {
        this.connect(authStore.token);
      }
    } catch {
      this.disconnect();
    }
  }
}

export const socketService = SocketService.getInstance();
