import { ref, computed } from 'vue';
import type { User, LoginRequest, RegisterRequest, LoginResponse } from '@/types/auth';
import type { UserRole } from '@/types/auth';
import { authService, type SocialLoginRequest } from '@/services/api/authService';
import { userProfileService } from '@/services/api/userProfileService';
import { socketService } from '@/services/websocket/socketClient';

/**
 * Auth store implemented as a singleton composable using Vue's reactivity system.
 * State is declared at module scope so it's shared across all component usages.
 *
 * Requirements:
 * - 1.5: Store JWT token on valid login
 * - 1.8: Clear JWT token on logout
 * - 18.1: Stores organized by domain
 * - 18.2: Persist auth token across browser sessions using localStorage
 * - 18.5: On app load, validate JWT by fetching user profile and restore auth state
 */

const TOKEN_STORAGE_KEY = 'furniture_bid_auth_token';

// --- Singleton reactive state (shared across all usages) ---
const token = ref<string | null>(null);
const user = ref<User | null>(null);
const isLoading = ref(false);

// --- Getters ---
const isAuthenticated = computed(() => !!token.value);
const userRole = computed<UserRole | null>(() => user.value?.role ?? null);
const userId = computed<string | null>(() => user.value?.id ?? null);

// --- localStorage helpers ---
function persistToken(value: string | null): void {
  try {
    if (value) {
      localStorage.setItem(TOKEN_STORAGE_KEY, value);
    } else {
      localStorage.removeItem(TOKEN_STORAGE_KEY);
    }
  } catch {
    // localStorage unavailable (e.g. private browsing quota exceeded)
    // Silently degrade — token remains in-memory only
  }
}

function loadPersistedToken(): string | null {
  try {
    return localStorage.getItem(TOKEN_STORAGE_KEY);
  } catch {
    return null;
  }
}

// --- Actions ---

/**
 * Authenticate with email and password credentials.
 * Stores JWT token and user data on success, then establishes WebSocket connection.
 *
 * Requirement 13.1: Connect WebSocket on login with JWT token
 * Requirement 13.6: Subscribe to notification channel on authentication
 */
async function login(credentials: LoginRequest): Promise<void> {
  isLoading.value = true;
  try {
    const response: LoginResponse = await authService.login(credentials);
    token.value = response.token;
    user.value = response.user;
    persistToken(response.token);

    // Establish WebSocket connection and subscribe to notifications
    socketService.connect(response.token);
    if (response.user?.id) {
      socketService.subscribeNotifications(response.user.id);
    }
  } finally {
    isLoading.value = false;
  }
}

/**
 * Authenticate via OAuth provider (Google or Facebook).
 * The provider token is exchanged server-side for a JWT.
 *
 * Requirement 13.1: Connect WebSocket on login with JWT token
 * Requirement 13.6: Subscribe to notification channel on authentication
 */
async function loginWithOAuth(provider: 'google' | 'facebook', providerToken: string): Promise<void> {
  isLoading.value = true;
  try {
    const request: SocialLoginRequest = { provider, token: providerToken };
    const response: LoginResponse = await authService.socialLogin(request);
    token.value = response.token;
    user.value = response.user;
    persistToken(response.token);

    // Establish WebSocket connection and subscribe to notifications
    socketService.connect(response.token);
    if (response.user?.id) {
      socketService.subscribeNotifications(response.user.id);
    }
  } finally {
    isLoading.value = false;
  }
}

/**
 * Register a new account.
 * Stores JWT token and user data on success, then establishes WebSocket connection.
 *
 * Requirement 13.1: Connect WebSocket on login with JWT token
 * Requirement 13.6: Subscribe to notification channel on authentication
 */
async function register(data: RegisterRequest): Promise<void> {
  isLoading.value = true;
  try {
    const response: LoginResponse = await authService.register(data);
    token.value = response.token;
    user.value = response.user;
    persistToken(response.token);

    // Establish WebSocket connection and subscribe to notifications
    socketService.connect(response.token);
    if (response.user?.id) {
      socketService.subscribeNotifications(response.user.id);
    }
  } finally {
    isLoading.value = false;
  }
}

/**
 * Log out the current user.
 * Disconnects WebSocket, clears all auth state and removes persisted token.
 *
 * Requirement 13.7: Disconnect WebSocket on logout
 */
async function logout(): Promise<void> {
  socketService.disconnect();
  clearAuth();
}

/**
 * Refresh the JWT token using the current session.
 * Updates the stored token on success and reconnects WebSocket; clears auth on failure.
 *
 * Requirement 13.5: Handle token refresh for expired WebSocket connections
 */
async function refreshToken(): Promise<void> {
  try {
    const response: LoginResponse = await authService.refreshToken();
    token.value = response.token;
    user.value = response.user;
    persistToken(response.token);
  } catch {
    clearAuth();
  }
}

/**
 * Restore a previous session on app load.
 * If a token exists in localStorage, validate it by fetching the user profile.
 * On success, establish WebSocket connection and subscribe to notifications.
 * On failure, clear the stale token.
 *
 * Requirement 18.5, 13.1, 13.6
 */
async function restoreSession(): Promise<void> {
  const persistedToken = loadPersistedToken();
  if (!persistedToken) {
    return;
  }

  // Set token so API interceptor can attach it to the profile request
  token.value = persistedToken;
  isLoading.value = true;

  try {
    const profile = await userProfileService.getProfile();
    user.value = profile;

    // Establish WebSocket connection and subscribe to notifications
    socketService.connect(persistedToken);
    if (profile?.id) {
      socketService.subscribeNotifications(profile.id);
    }
  } catch {
    // Token is invalid or expired — clear state
    clearAuth();
  } finally {
    isLoading.value = false;
  }
}

/**
 * Immediately clear all auth state and persisted token.
 */
function clearAuth(): void {
  token.value = null;
  user.value = null;
  isLoading.value = false;
  persistToken(null);
}

// --- Composable export ---

/**
 * Singleton composable for auth state management.
 * All component usages share the same reactive state.
 */
export function useAuthStore() {
  return {
    // State
    token,
    user,
    isLoading,

    // Getters
    isAuthenticated,
    userRole,
    userId,

    // Actions
    login,
    loginWithOAuth,
    register,
    logout,
    refreshToken,
    restoreSession,
    clearAuth,
  };
}
