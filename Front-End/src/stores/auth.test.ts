import { describe, it, expect, vi, beforeEach } from 'vitest';

// Mock the auth service
vi.mock('@/services/api/authService', () => ({
  authService: {
    login: vi.fn(),
    register: vi.fn(),
    refreshToken: vi.fn(),
    socialLogin: vi.fn(),
  },
}));

// Mock the user profile service
vi.mock('@/services/api/userProfileService', () => ({
  userProfileService: {
    getProfile: vi.fn(),
  },
}));

// Mock the WebSocket socket service
vi.mock('@/services/websocket/socketClient', () => ({
  socketService: {
    connect: vi.fn(),
    disconnect: vi.fn(),
    subscribeNotifications: vi.fn(),
    joinAuctionRoom: vi.fn(),
    leaveAuctionRoom: vi.fn(),
    getConnectionStatus: vi.fn(),
  },
}));

import { useAuthStore } from './auth';
import { authService } from '@/services/api/authService';
import { userProfileService } from '@/services/api/userProfileService';
import { socketService } from '@/services/websocket/socketClient';
import type { LoginResponse, User } from '@/types/auth';

const mockUser: User = {
  id: 'user-123',
  email: 'test@example.com',
  displayName: 'Test User',
  role: 'buyer',
  createdAt: '2024-01-01T00:00:00Z',
};

const mockLoginResponse: LoginResponse = {
  token: 'jwt-token-abc',
  user: mockUser,
};

describe('useAuthStore', () => {
  let store: ReturnType<typeof useAuthStore>;

  beforeEach(() => {
    store = useAuthStore();
    // Reset state before each test
    store.clearAuth();
    vi.clearAllMocks();
  });

  describe('initial state', () => {
    it('starts with null token and user', () => {
      expect(store.token.value).toBeNull();
      expect(store.user.value).toBeNull();
      expect(store.isLoading.value).toBe(false);
    });

    it('isAuthenticated is false when no token', () => {
      expect(store.isAuthenticated.value).toBe(false);
    });

    it('userRole is null when no user', () => {
      expect(store.userRole.value).toBeNull();
    });

    it('userId is null when no user', () => {
      expect(store.userId.value).toBeNull();
    });
  });

  describe('login', () => {
    it('stores token and user on successful login', async () => {
      vi.mocked(authService.login).mockResolvedValue(mockLoginResponse);

      await store.login({ email: 'test@example.com', password: 'Password1' });

      expect(store.token.value).toBe('jwt-token-abc');
      expect(store.user.value).toEqual(mockUser);
      expect(store.isAuthenticated.value).toBe(true);
      expect(store.userRole.value).toBe('buyer');
      expect(store.userId.value).toBe('user-123');
    });

    it('persists token to localStorage on successful login', async () => {
      vi.mocked(authService.login).mockResolvedValue(mockLoginResponse);

      await store.login({ email: 'test@example.com', password: 'Password1' });

      expect(localStorage.getItem('furniture_bid_auth_token')).toBe('jwt-token-abc');
    });

    it('sets isLoading during login and resets after', async () => {
      let resolveLogin: (value: LoginResponse) => void;
      const promise = new Promise<LoginResponse>((resolve) => { resolveLogin = resolve; });
      vi.mocked(authService.login).mockReturnValue(promise);

      const loginPromise = store.login({ email: 'test@example.com', password: 'Password1' });
      expect(store.isLoading.value).toBe(true);

      resolveLogin!(mockLoginResponse);
      await loginPromise;
      expect(store.isLoading.value).toBe(false);
    });

    it('resets isLoading on login failure', async () => {
      vi.mocked(authService.login).mockRejectedValue(new Error('Invalid credentials'));

      await expect(store.login({ email: 'bad@example.com', password: 'wrong' })).rejects.toThrow('Invalid credentials');
      expect(store.isLoading.value).toBe(false);
    });

    it('connects WebSocket and subscribes to notifications on successful login', async () => {
      vi.mocked(authService.login).mockResolvedValue(mockLoginResponse);

      await store.login({ email: 'test@example.com', password: 'Password1' });

      expect(socketService.connect).toHaveBeenCalledWith('jwt-token-abc');
      expect(socketService.subscribeNotifications).toHaveBeenCalledWith('user-123');
    });
  });

  describe('loginWithOAuth', () => {
    it('stores token and user on successful OAuth login', async () => {
      vi.mocked(authService.socialLogin).mockResolvedValue(mockLoginResponse);

      await store.loginWithOAuth('google', 'oauth-token-xyz');

      expect(store.token.value).toBe('jwt-token-abc');
      expect(store.user.value).toEqual(mockUser);
      expect(store.isAuthenticated.value).toBe(true);
    });

    it('persists token to localStorage', async () => {
      vi.mocked(authService.socialLogin).mockResolvedValue(mockLoginResponse);

      await store.loginWithOAuth('facebook', 'oauth-token-xyz');

      expect(localStorage.getItem('furniture_bid_auth_token')).toBe('jwt-token-abc');
    });

    it('connects WebSocket and subscribes to notifications on OAuth login', async () => {
      vi.mocked(authService.socialLogin).mockResolvedValue(mockLoginResponse);

      await store.loginWithOAuth('google', 'oauth-token-xyz');

      expect(socketService.connect).toHaveBeenCalledWith('jwt-token-abc');
      expect(socketService.subscribeNotifications).toHaveBeenCalledWith('user-123');
    });
  });

  describe('register', () => {
    it('stores token and user on successful registration', async () => {
      vi.mocked(authService.register).mockResolvedValue(mockLoginResponse);

      await store.register({ email: 'new@example.com', password: 'Password1', displayName: 'New User' });

      expect(store.token.value).toBe('jwt-token-abc');
      expect(store.user.value).toEqual(mockUser);
      expect(store.isAuthenticated.value).toBe(true);
    });

    it('persists token to localStorage', async () => {
      vi.mocked(authService.register).mockResolvedValue(mockLoginResponse);

      await store.register({ email: 'new@example.com', password: 'Password1', displayName: 'New User' });

      expect(localStorage.getItem('furniture_bid_auth_token')).toBe('jwt-token-abc');
    });

    it('connects WebSocket and subscribes to notifications on registration', async () => {
      vi.mocked(authService.register).mockResolvedValue(mockLoginResponse);

      await store.register({ email: 'new@example.com', password: 'Password1', displayName: 'New User' });

      expect(socketService.connect).toHaveBeenCalledWith('jwt-token-abc');
      expect(socketService.subscribeNotifications).toHaveBeenCalledWith('user-123');
    });
  });

  describe('logout', () => {
    it('clears token, user, and localStorage', async () => {
      // First login
      vi.mocked(authService.login).mockResolvedValue(mockLoginResponse);
      await store.login({ email: 'test@example.com', password: 'Password1' });

      // Then logout
      await store.logout();

      expect(store.token.value).toBeNull();
      expect(store.user.value).toBeNull();
      expect(store.isAuthenticated.value).toBe(false);
      expect(localStorage.getItem('furniture_bid_auth_token')).toBeNull();
    });

    it('disconnects WebSocket on logout', async () => {
      vi.mocked(authService.login).mockResolvedValue(mockLoginResponse);
      await store.login({ email: 'test@example.com', password: 'Password1' });

      await store.logout();

      expect(socketService.disconnect).toHaveBeenCalled();
    });
  });

  describe('refreshToken', () => {
    it('updates token and user on successful refresh', async () => {
      const newResponse: LoginResponse = {
        token: 'new-jwt-token',
        user: { ...mockUser, displayName: 'Updated User' },
      };
      vi.mocked(authService.refreshToken).mockResolvedValue(newResponse);

      await store.refreshToken();

      expect(store.token.value).toBe('new-jwt-token');
      expect(store.user.value?.displayName).toBe('Updated User');
      expect(localStorage.getItem('furniture_bid_auth_token')).toBe('new-jwt-token');
    });

    it('clears auth on refresh failure', async () => {
      // Set up initial auth state
      vi.mocked(authService.login).mockResolvedValue(mockLoginResponse);
      await store.login({ email: 'test@example.com', password: 'Password1' });

      // Refresh fails
      vi.mocked(authService.refreshToken).mockRejectedValue(new Error('Expired'));

      await store.refreshToken();

      expect(store.token.value).toBeNull();
      expect(store.user.value).toBeNull();
      expect(store.isAuthenticated.value).toBe(false);
      expect(localStorage.getItem('furniture_bid_auth_token')).toBeNull();
    });
  });

  describe('restoreSession', () => {
    it('restores user from valid persisted token', async () => {
      localStorage.setItem('furniture_bid_auth_token', 'persisted-token');
      vi.mocked(userProfileService.getProfile).mockResolvedValue(mockUser);

      await store.restoreSession();

      expect(store.token.value).toBe('persisted-token');
      expect(store.user.value).toEqual(mockUser);
      expect(store.isAuthenticated.value).toBe(true);
    });

    it('connects WebSocket and subscribes to notifications on session restore', async () => {
      localStorage.setItem('furniture_bid_auth_token', 'persisted-token');
      vi.mocked(userProfileService.getProfile).mockResolvedValue(mockUser);

      await store.restoreSession();

      expect(socketService.connect).toHaveBeenCalledWith('persisted-token');
      expect(socketService.subscribeNotifications).toHaveBeenCalledWith('user-123');
    });

    it('clears auth when persisted token is invalid', async () => {
      localStorage.setItem('furniture_bid_auth_token', 'invalid-token');
      vi.mocked(userProfileService.getProfile).mockRejectedValue(new Error('Unauthorized'));

      await store.restoreSession();

      expect(store.token.value).toBeNull();
      expect(store.user.value).toBeNull();
      expect(store.isAuthenticated.value).toBe(false);
      expect(localStorage.getItem('furniture_bid_auth_token')).toBeNull();
    });

    it('does nothing when no token is persisted', async () => {
      await store.restoreSession();

      expect(store.token.value).toBeNull();
      expect(store.user.value).toBeNull();
      expect(userProfileService.getProfile).not.toHaveBeenCalled();
    });

    it('sets isLoading while restoring session', async () => {
      localStorage.setItem('furniture_bid_auth_token', 'persisted-token');
      let resolveProfile: (value: User) => void;
      const promise = new Promise<User>((resolve) => { resolveProfile = resolve; });
      vi.mocked(userProfileService.getProfile).mockReturnValue(promise);

      const restorePromise = store.restoreSession();
      expect(store.isLoading.value).toBe(true);

      resolveProfile!(mockUser);
      await restorePromise;
      expect(store.isLoading.value).toBe(false);
    });
  });

  describe('clearAuth', () => {
    it('resets all state and removes persisted token', async () => {
      vi.mocked(authService.login).mockResolvedValue(mockLoginResponse);
      await store.login({ email: 'test@example.com', password: 'Password1' });

      store.clearAuth();

      expect(store.token.value).toBeNull();
      expect(store.user.value).toBeNull();
      expect(store.isLoading.value).toBe(false);
      expect(store.isAuthenticated.value).toBe(false);
      expect(store.userRole.value).toBeNull();
      expect(store.userId.value).toBeNull();
      expect(localStorage.getItem('furniture_bid_auth_token')).toBeNull();
    });
  });

  describe('singleton behavior', () => {
    it('shares state across multiple useAuthStore() calls', async () => {
      vi.mocked(authService.login).mockResolvedValue(mockLoginResponse);

      const store1 = useAuthStore();
      const store2 = useAuthStore();

      await store1.login({ email: 'test@example.com', password: 'Password1' });

      // store2 should see the same state
      expect(store2.token.value).toBe('jwt-token-abc');
      expect(store2.isAuthenticated.value).toBe(true);
      expect(store2.user.value).toEqual(mockUser);
    });
  });
});
