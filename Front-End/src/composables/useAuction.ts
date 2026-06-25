import { computed, ref, onMounted, onUnmounted } from 'vue';
import { useAuctionStore } from '@/stores/auction';
import { useFurnitureStore } from '@/stores/furniture';
import { useAuthStore } from '@/stores/auth';
import { useToast } from '@/composables/useToast';
import { socketService } from '@/services/websocket/socketClient';
import { BID_INCREMENT, PAGE_SIZE } from '@/utils/constants';
import type { BidUpdateEvent } from '@/types/common';

/**
 * Composable for bid placement logic, auto-bid management, and WebSocket bid update handling.
 *
 * Validates: Requirements 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7
 */
export function useAuction(auctionId: string) {
  const auctionStore = useAuctionStore();
  const furnitureStore = useFurnitureStore();
  const authStore = useAuthStore();
  const { showToast } = useToast();

  const bidError = ref<string | null>(null);
  const bidSuccess = ref<string | null>(null);
  const currentBidOverride = ref<number | null>(null);

  // Minimum bid is current bid + increment
  const minimumBid = computed(() => {
    const current = currentBidOverride.value ?? furnitureStore.currentListing?.currentBid ?? 0;
    return parseFloat((current + BID_INCREMENT).toFixed(2));
  });

  const isSubmitting = computed(() => auctionStore.bidSubmitting.value);

  const autoBidConfig = computed(() => auctionStore.getAutoBidConfig(auctionId));

  const isAutoBidActive = computed(() => autoBidConfig.value?.isActive ?? false);

  /**
   * Place a bid on the auction.
   */
  async function submitBid(amount: number): Promise<boolean> {
    bidError.value = null;
    bidSuccess.value = null;

    // Validate amount
    if (amount < minimumBid.value) {
      bidError.value = `Bid must be at least ${minimumBid.value.toFixed(2)}`;
      return false;
    }

    if (!authStore.isAuthenticated.value) {
      bidError.value = 'You must be logged in to place a bid';
      return false;
    }

    try {
      const response = await auctionStore.placeBid({
        auctionId,
        amount,
      });

      if (response.success) {
        bidSuccess.value = 'Bid placed successfully!';
        showToast('Bid placed successfully!', 'success');
        return true;
      } else {
        bidError.value = response.error ?? 'Failed to place bid. Please try again.';
        return false;
      }
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Network error. Please try again.';
      bidError.value = message;
      return false;
    }
  }

  /**
   * Toggle auto-bid activation/deactivation.
   */
  async function toggleAutoBid(maxAmount: number): Promise<boolean> {
    if (!authStore.isAuthenticated.value) {
      showToast('You must be logged in to use auto-bid', 'error');
      return false;
    }

    try {
      if (isAutoBidActive.value) {
        await auctionStore.deactivateAutoBid(auctionId);
        showToast('Auto-bid deactivated', 'info');
      } else {
        if (maxAmount < minimumBid.value) {
          showToast('Max amount must be at least the minimum bid', 'error');
          return false;
        }
        await auctionStore.activateAutoBid({ auctionId, maxAmount });
        showToast('Auto-bid activated', 'success');
      }
      return true;
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to update auto-bid. Please try again.';
      showToast(message, 'error');
      return false;
    }
  }

  /**
   * Handle incoming bid update from WebSocket.
   */
  function handleBidUpdate(event: BidUpdateEvent): void {
    auctionStore.handleBidUpdate(event);
    furnitureStore.updateCurrentBid(event.auctionId, event.currentBid, event.bidCount);
    currentBidOverride.value = event.currentBid;

    // Check if auto-bid limit reached
    const config = auctionStore.getAutoBidConfig(auctionId);
    if (config?.isActive && event.currentBid >= config.maxAmount) {
      showToast('Auto-bid limit reached. Your maximum amount has been exceeded.', 'warning');
    }
  }

  /**
   * Fetch bid history for the auction.
   */
  async function fetchHistory(page = 1): Promise<void> {
    await auctionStore.fetchBidHistory({
      auctionId,
      page,
      pageSize: PAGE_SIZE,
    });
  }

  // Subscribe to WebSocket on mount
  onMounted(() => {
    socketService.joinAuctionRoom(auctionId);
    socketService.onBidUpdate(auctionId, handleBidUpdate);
  });

  // Unsubscribe on unmount
  onUnmounted(() => {
    socketService.leaveAuctionRoom(auctionId);
  });

  return {
    minimumBid,
    submitBid,
    isSubmitting,
    bidError,
    bidSuccess,
    autoBidConfig,
    toggleAutoBid,
    isAutoBidActive,
    fetchHistory,
  };
}
