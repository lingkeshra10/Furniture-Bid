<script setup lang="ts">
import { ref, onMounted } from 'vue';
import AppNavbar from '@/components/common/AppNavbar.vue';
import AppToast from '@/components/common/AppToast.vue';
import ConnectionStatus from '@/components/common/ConnectionStatus.vue';
import LoadingSpinner from '@/components/common/LoadingSpinner.vue';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const isRestoringSession = ref(true);
const localStorageUnavailable = ref(false);

/**
 * Check if localStorage is available.
 * Returns false in private browsing mode or when storage is disabled.
 */
function checkLocalStorageAvailability(): boolean {
  const testKey = '__storage_test__';
  try {
    localStorage.setItem(testKey, 'test');
    localStorage.removeItem(testKey);
    return true;
  } catch {
    return false;
  }
}

onMounted(async () => {
  // Check localStorage availability (Requirement 18.6)
  if (!checkLocalStorageAvailability()) {
    localStorageUnavailable.value = true;
  }

  // Attempt session restoration (Requirement 18.5)
  try {
    await authStore.restoreSession();
  } catch {
    // Session restoration failed — user will be treated as unauthenticated
  } finally {
    isRestoringSession.value = false;
  }
});
</script>

<template>
  <div class="min-h-screen bg-background text-text flex flex-col">
    <!-- Loading state while restoring session -->
    <template v-if="isRestoringSession">
      <div class="flex-1 flex items-center justify-center" role="status" aria-label="Restoring session">
        <LoadingSpinner />
      </div>
    </template>

    <template v-else>
      <!-- localStorage unavailability warning -->
      <div
        v-if="localStorageUnavailable"
        class="bg-yellow-100 border-b border-yellow-300 text-yellow-800 px-4 py-2 text-sm text-center"
        role="alert"
      >
        Session storage is unavailable. Your session will not persist across browser sessions.
      </div>

      <!-- Connection status banner (visible when disconnected/reconnecting) -->
      <ConnectionStatus />

      <!-- Main navigation -->
      <AppNavbar />

      <!-- Toast notifications -->
      <AppToast />

      <!-- Main content -->
      <main class="flex-1">
        <RouterView />
      </main>
    </template>
  </div>
</template>
