/**
 * Global store reset utility for 401 unauthorized handling.
 *
 * Uses lazy imports to avoid circular dependency issues between the API client
 * and the Pinia stores (stores import API services which import the client).
 *
 * On HTTP 401 response:
 * 1. Clears auth store (token=null, user=null)
 * 2. Resets auction store (clear currentBids, autoBidConfigs)
 * 3. Resets furniture store (clear listings, currentListing, etc.)
 * 4. Resets notification store (clear notifications, unreadCount=0)
 * 5. Resets watchlist store (clear watchedItems, watchedIds)
 * 6. Removes auth token from localStorage
 * 7. Redirects to login page
 *
 * Requirement: 18.3
 */

const AUTH_TOKEN_KEY = 'furniture_bid_auth_token';

/**
 * Reset all domain stores to initial defaults, clear persisted token,
 * and redirect to login.
 *
 * This function lazily accesses stores at call time to avoid circular
 * import issues during module initialization.
 */
export async function handleUnauthorized(): Promise<void> {
  // 1. Clear auth store
  const { useAuthStore } = await import('@/stores/auth');
  const authStore = useAuthStore();
  authStore.clearAuth();

  // 2. Reset auction store
  const { useAuctionStore } = await import('@/stores/auction');
  const auctionStore = useAuctionStore();
  auctionStore.$reset();

  // 3. Reset furniture store
  const { useFurnitureStore } = await import('@/stores/furniture');
  const furnitureStore = useFurnitureStore();
  furnitureStore.$reset();

  // 4. Reset notification store
  const { useNotificationStore } = await import('@/stores/notification');
  const notificationStore = useNotificationStore();
  notificationStore.$reset();

  // 5. Reset watchlist store
  const { useWatchlistStore } = await import('@/stores/watchlist');
  const watchlistStore = useWatchlistStore();
  watchlistStore.$reset();

  // 6. Remove auth token from localStorage (belt-and-suspenders — clearAuth does this too)
  try {
    localStorage.removeItem(AUTH_TOKEN_KEY);
  } catch {
    // localStorage unavailable — token already cleared from memory via clearAuth
  }

  // 7. Redirect to login page via router
  const router = await import('@/router/index');
  router.default.push({ name: 'login' });
}
