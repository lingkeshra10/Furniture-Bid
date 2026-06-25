import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';

// Mock socket.io-client before importing socketClient
const mockSocket = {
  on: vi.fn(),
  emit: vi.fn(),
  disconnect: vi.fn(),
  removeAllListeners: vi.fn(),
};

vi.mock('socket.io-client', () => ({
  io: vi.fn(() => mockSocket),
}));

// We need to reset the singleton between tests, so we use a fresh import approach
describe('SocketService', () => {
  let socketService: typeof import('./socketClient')['socketService'];

  beforeEach(async () => {
    vi.resetModules();
    vi.clearAllMocks();

    // Reset mock socket methods
    mockSocket.on = vi.fn();
    mockSocket.emit = vi.fn();
    mockSocket.disconnect = vi.fn();
    mockSocket.removeAllListeners = vi.fn();

    // Re-mock after module reset
    vi.doMock('socket.io-client', () => ({
      io: vi.fn(() => mockSocket),
    }));

    const module = await import('./socketClient');
    socketService = module.socketService;
  });

  afterEach(() => {
    socketService.disconnect();
  });

  describe('Singleton pattern', () => {
    it('returns the same instance on multiple calls', async () => {
      const module = await import('./socketClient');
      const instance1 = module.socketService;
      const instance2 = module.socketService;
      expect(instance1).toBe(instance2);
    });
  });

  describe('connect()', () => {
    it('sets connection status to connected in mock mode', () => {
      vi.stubEnv('VITE_USE_MOCKS', 'true');

      socketService.connect('test-token');

      expect(socketService.getConnectionStatus().value).toBe('connected');
      vi.unstubAllEnvs();
    });

    it('creates a socket.io connection with auth token when not in mock mode', async () => {
      vi.stubEnv('VITE_USE_MOCKS', 'false');
      vi.stubEnv('VITE_WS_URL', 'http://localhost:4000');

      const { io } = await import('socket.io-client');

      socketService.connect('my-jwt-token');

      expect(io).toHaveBeenCalledWith('http://localhost:4000', {
        auth: { token: 'my-jwt-token' },
        reconnection: true,
        reconnectionDelay: 1000,
        reconnectionDelayMax: 30000,
        reconnectionAttempts: 10,
      });

      vi.unstubAllEnvs();
    });

    it('sets up connect, disconnect, and error listeners', () => {
      vi.stubEnv('VITE_USE_MOCKS', 'false');
      vi.stubEnv('VITE_WS_URL', 'http://localhost:4000');

      socketService.connect('token');

      const registeredEvents = mockSocket.on.mock.calls.map(
        (call: [string, unknown]) => call[0]
      );
      expect(registeredEvents).toContain('connect');
      expect(registeredEvents).toContain('disconnect');
      expect(registeredEvents).toContain('reconnect_attempt');
      expect(registeredEvents).toContain('reconnect_failed');
      expect(registeredEvents).toContain('connect_error');

      vi.unstubAllEnvs();
    });
  });

  describe('disconnect()', () => {
    it('removes all listeners, disconnects socket, and sets status to disconnected', () => {
      vi.stubEnv('VITE_USE_MOCKS', 'false');
      vi.stubEnv('VITE_WS_URL', 'http://localhost:4000');

      socketService.connect('token');
      socketService.disconnect();

      expect(mockSocket.removeAllListeners).toHaveBeenCalled();
      expect(mockSocket.disconnect).toHaveBeenCalled();
      expect(socketService.getConnectionStatus().value).toBe('disconnected');

      vi.unstubAllEnvs();
    });

    it('clears subscribed rooms on disconnect', () => {
      vi.stubEnv('VITE_USE_MOCKS', 'false');
      vi.stubEnv('VITE_WS_URL', 'http://localhost:4000');

      socketService.connect('token');
      socketService.joinAuctionRoom('auction-1');
      socketService.disconnect();

      // After disconnect, reconnecting and joining should start fresh
      expect(socketService.getConnectionStatus().value).toBe('disconnected');

      vi.unstubAllEnvs();
    });
  });

  describe('Event subscription methods', () => {
    beforeEach(() => {
      vi.stubEnv('VITE_USE_MOCKS', 'false');
      vi.stubEnv('VITE_WS_URL', 'http://localhost:4000');
      socketService.connect('token');
    });

    afterEach(() => {
      vi.unstubAllEnvs();
    });

    it('onBidUpdate subscribes to auction-specific bid events', () => {
      const callback = vi.fn();
      socketService.onBidUpdate('auction-123', callback);

      expect(mockSocket.on).toHaveBeenCalledWith('bid:update:auction-123', callback);
    });

    it('onOutbid subscribes to outbid events', () => {
      const callback = vi.fn();
      socketService.onOutbid(callback);

      expect(mockSocket.on).toHaveBeenCalledWith('outbid', callback);
    });

    it('onAuctionEnding subscribes to auction ending events', () => {
      const callback = vi.fn();
      socketService.onAuctionEnding(callback);

      expect(mockSocket.on).toHaveBeenCalledWith('auction:ending', callback);
    });

    it('onAuctionWon subscribes to auction won events', () => {
      const callback = vi.fn();
      socketService.onAuctionWon(callback);

      expect(mockSocket.on).toHaveBeenCalledWith('auction:won', callback);
    });

    it('onAuctionLost subscribes to auction lost events', () => {
      const callback = vi.fn();
      socketService.onAuctionLost(callback);

      expect(mockSocket.on).toHaveBeenCalledWith('auction:lost', callback);
    });

    it('onNotification subscribes to notification events', () => {
      const callback = vi.fn();
      socketService.onNotification(callback);

      expect(mockSocket.on).toHaveBeenCalledWith('notification', callback);
    });
  });

  describe('Room management', () => {
    beforeEach(() => {
      vi.stubEnv('VITE_USE_MOCKS', 'false');
      vi.stubEnv('VITE_WS_URL', 'http://localhost:4000');
      socketService.connect('token');
    });

    afterEach(() => {
      vi.unstubAllEnvs();
    });

    it('joinAuctionRoom emits join:auction event', () => {
      socketService.joinAuctionRoom('auction-456');

      expect(mockSocket.emit).toHaveBeenCalledWith('join:auction', {
        auctionId: 'auction-456',
      });
    });

    it('leaveAuctionRoom emits leave:auction event', () => {
      socketService.leaveAuctionRoom('auction-456');

      expect(mockSocket.emit).toHaveBeenCalledWith('leave:auction', {
        auctionId: 'auction-456',
      });
    });

    it('subscribeNotifications emits subscribe:notifications event', () => {
      socketService.subscribeNotifications('user-789');

      expect(mockSocket.emit).toHaveBeenCalledWith('subscribe:notifications', {
        userId: 'user-789',
      });
    });
  });

  describe('Connection status', () => {
    it('starts as disconnected', () => {
      expect(socketService.getConnectionStatus().value).toBe('disconnected');
    });

    it('returns a Vue ref that can be observed reactively', () => {
      const status = socketService.getConnectionStatus();
      expect(status.value).toBe('disconnected');
    });
  });

  describe('Reconnection behavior', () => {
    beforeEach(() => {
      vi.stubEnv('VITE_USE_MOCKS', 'false');
      vi.stubEnv('VITE_WS_URL', 'http://localhost:4000');
      socketService.connect('token');
    });

    afterEach(() => {
      vi.unstubAllEnvs();
    });

    it('sets status to connected and resubscribes rooms on connect event', () => {
      // Join a room first
      socketService.joinAuctionRoom('auction-1');
      mockSocket.emit.mockClear();

      // Simulate connect event
      const connectHandler = mockSocket.on.mock.calls.find(
        (call: [string, unknown]) => call[0] === 'connect'
      )?.[1] as () => void;
      connectHandler();

      expect(socketService.getConnectionStatus().value).toBe('connected');
      // Should have re-emitted the join for the subscribed room
      expect(mockSocket.emit).toHaveBeenCalledWith('join:auction', {
        auctionId: 'auction-1',
      });
    });

    it('sets status to reconnecting on disconnect event', () => {
      const disconnectHandler = mockSocket.on.mock.calls.find(
        (call: [string, unknown]) => call[0] === 'disconnect'
      )?.[1] as () => void;
      disconnectHandler();

      expect(socketService.getConnectionStatus().value).toBe('reconnecting');
    });

    it('sets status to disconnected on reconnect_failed event', () => {
      const failedHandler = mockSocket.on.mock.calls.find(
        (call: [string, unknown]) => call[0] === 'reconnect_failed'
      )?.[1] as () => void;
      failedHandler();

      expect(socketService.getConnectionStatus().value).toBe('disconnected');
    });
  });
});
