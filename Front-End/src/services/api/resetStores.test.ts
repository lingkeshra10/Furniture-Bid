import { describe, it, expect, vi, beforeEach } from 'vitest';

// Create stable mock function references
const mockClearAuth = vi.fn();
const mockAuctionReset = vi.fn();
const mockFurnitureReset = vi.fn();
const mockNotificationReset = vi.fn();
const mockWatchlistReset = vi.fn();
const mockRouterPush = vi.fn();

// Mock all stores with stable references
vi.mock('@/stores/auth', () => ({
  useAuthStore: () => ({
    clearAuth: mockClearAuth,
  }),
}));

vi.mock('@/stores/auction', () => ({
  useAuctionStore: () => ({
    $reset: mockAuctionReset,
  }),
}));

vi.mock('@/stores/furniture', () => ({
  useFurnitureStore: () => ({
    $reset: mockFurnitureReset,
  }),
}));

vi.mock('@/stores/notification', () => ({
  useNotificationStore: () => ({
    $reset: mockNotificationReset,
  }),
}));

vi.mock('@/stores/watchlist', () => ({
  useWatchlistStore: () => ({
    $reset: mockWatchlistReset,
  }),
}));

// Mock router
vi.mock('@/router/index', () => ({
  default: {
    push: mockRouterPush,
  },
}));

describe('handleUnauthorized', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.setItem('furniture_bid_auth_token', 'test-token');
  });

  it('should clear the auth store', async () => {
    const { handleUnauthorized } = await import('./resetStores');
    await handleUnauthorized();
    expect(mockClearAuth).toHaveBeenCalled();
  });

  it('should reset the auction store', async () => {
    const { handleUnauthorized } = await import('./resetStores');
    await handleUnauthorized();
    expect(mockAuctionReset).toHaveBeenCalled();
  });

  it('should reset the furniture store', async () => {
    const { handleUnauthorized } = await import('./resetStores');
    await handleUnauthorized();
    expect(mockFurnitureReset).toHaveBeenCalled();
  });

  it('should reset the notification store', async () => {
    const { handleUnauthorized } = await import('./resetStores');
    await handleUnauthorized();
    expect(mockNotificationReset).toHaveBeenCalled();
  });

  it('should reset the watchlist store', async () => {
    const { handleUnauthorized } = await import('./resetStores');
    await handleUnauthorized();
    expect(mockWatchlistReset).toHaveBeenCalled();
  });

  it('should remove auth_token from localStorage', async () => {
    expect(localStorage.getItem('furniture_bid_auth_token')).toBe('test-token');

    const { handleUnauthorized } = await import('./resetStores');
    await handleUnauthorized();

    expect(localStorage.getItem('furniture_bid_auth_token')).toBeNull();
  });

  it('should redirect to login page', async () => {
    const { handleUnauthorized } = await import('./resetStores');
    await handleUnauthorized();
    expect(mockRouterPush).toHaveBeenCalledWith({ name: 'login' });
  });
});
