import axios, { type AxiosInstance, type InternalAxiosRequestConfig } from 'axios';

/**
 * Centralized Axios API client with request/response interceptors.
 *
 * - Base URL: from VITE_API_BASE_URL env var, defaults to '/api'
 * - Timeout: 30 seconds
 * - Content-Type: application/json
 * - Request interceptor: attaches JWT Bearer token from auth store
 * - Response interceptor: handles 401, 403, 429, 5xx, and network errors
 *
 * Requirements: 14.1, 14.5, 14.6, 14.7
 */

// Lazy accessor for the auth store to avoid circular dependencies.
// The auth store and router may not exist yet during early development.
let _getAuthStore: (() => { token: string | null; logout: () => Promise<void> }) | null = null;
let _getRouter: (() => { push: (location: { name: string }) => void }) | null = null;

/**
 * Register the auth store accessor. Called once during app initialization.
 */
export function registerAuthStore(getter: () => { token: string | null; logout: () => Promise<void> }): void {
  _getAuthStore = getter;
}

/**
 * Register the router accessor. Called once during app initialization.
 */
export function registerRouter(getter: () => { push: (location: { name: string }) => void }): void {
  _getRouter = getter;
}

const apiClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
});

// Request interceptor: attach JWT from auth store
apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  if (_getAuthStore) {
    const authStore = _getAuthStore();
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`;
    }
  }
  return config;
});

// Response interceptor: centralized error handling
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const status = error.response?.status;

    if (status === 401) {
      // Clear all domain stores, remove persisted token, and redirect to login
      // Uses dynamic import to avoid circular dependencies (Requirement 18.3)
      try {
        const { handleUnauthorized } = await import('./resetStores');
        await handleUnauthorized();
      } catch {
        // Fallback: at minimum clear auth store if resetStores fails
        if (_getAuthStore) {
          try {
            const authStore = _getAuthStore();
            await authStore.logout();
          } catch {
            // Suppress — original 401 error propagates below
          }
        }
      }
    } else if (status === 403) {
      // Redirect to catalog (unauthorized access)
      if (_getRouter) {
        _getRouter().push({ name: 'catalog' });
      }
    } else if (status === 429) {
      showToast('Rate limit exceeded. Please try again later.', 'warning');
    } else if (status && status >= 500) {
      showToast('Server error. Please try again.', 'error');
    } else if (!error.response) {
      // Network error — no response received
      showToast('Network error. Check your connection.', 'error');
    }

    return Promise.reject(error);
  }
);

/**
 * Simple toast notification helper.
 * Dispatches a custom event that the AppToast component listens for.
 * This provides a framework-agnostic way for services to trigger UI notifications.
 */
export function showToast(message: string, type: 'success' | 'warning' | 'error' | 'info' = 'info'): void {
  if (typeof window !== 'undefined') {
    window.dispatchEvent(
      new CustomEvent('app:toast', { detail: { message, type } })
    );
  }
}

/**
 * Returns the appropriate API client instance.
 * When VITE_USE_MOCKS=true, returns a mock client for front-end development
 * without a running backend. Otherwise returns the real Axios client.
 *
 * Requirement: 14.4
 */
export function getApiClient(): AxiosInstance {
  if (import.meta.env.VITE_USE_MOCKS === 'true') {
    return getMockClient() ?? apiClient;
  }
  return apiClient;
}

// Placeholder for mock client — will be provided by task 2.4
let _mockClient: AxiosInstance | null = null;

/**
 * Register a mock API client. Called by mock infrastructure when mock mode is active.
 */
export function registerMockClient(client: AxiosInstance): void {
  _mockClient = client;
}

function getMockClient(): AxiosInstance | null {
  return _mockClient;
}

export { apiClient };
export default apiClient;
