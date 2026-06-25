<script setup lang="ts">
import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth';

const emit = defineEmits<{
  success: [];
  error: [message: string];
}>();

const authStore = useAuthStore();
const oauthLoading = ref<'google' | 'facebook' | null>(null);

/**
 * Initiates an OAuth flow for the given provider.
 * In production this would open a popup/redirect to the provider's consent screen.
 * The provider token would then be exchanged server-side for a JWT.
 */
async function handleOAuth(provider: 'google' | 'facebook'): Promise<void> {
  oauthLoading.value = provider;
  try {
    // Simulate obtaining a provider token from the OAuth flow.
    // In production, this would be replaced by an actual OAuth SDK integration
    // (e.g., Google Identity Services or Facebook Login SDK).
    const providerToken = await initiateOAuthFlow(provider);
    await authStore.loginWithOAuth(provider, providerToken);
    emit('success');
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : `${provider} login failed. Please try again.`;
    emit('error', message);
  } finally {
    oauthLoading.value = null;
  }
}

/**
 * Placeholder for the actual OAuth popup/redirect flow.
 * This will be implemented when integrating with real OAuth SDKs.
 */
async function initiateOAuthFlow(provider: 'google' | 'facebook'): Promise<string> {
  // This would typically:
  // 1. Open a popup or redirect to provider's auth URL
  // 2. Handle the callback with the authorization code
  // 3. Return the token for server-side exchange
  throw new Error(`OAuth flow for ${provider} is not yet configured`);
}
</script>

<template>
  <div class="space-y-3">
    <!-- Google OAuth -->
    <button
      type="button"
      :disabled="!!oauthLoading"
      class="w-full min-h-touch flex items-center justify-center gap-3 rounded-lg border border-gray-300 bg-white px-6 py-3 text-text font-medium transition hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-primary/50 focus:ring-offset-2 disabled:opacity-60 disabled:cursor-not-allowed"
      :aria-busy="oauthLoading === 'google'"
      @click="handleOAuth('google')"
    >
      <svg
        v-if="oauthLoading === 'google'"
        class="animate-spin h-5 w-5 text-gray-500"
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 24 24"
        aria-hidden="true"
      >
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
      </svg>
      <svg
        v-else
        class="h-5 w-5"
        viewBox="0 0 24 24"
        aria-hidden="true"
      >
        <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
        <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
        <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
        <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
      </svg>
      {{ oauthLoading === 'google' ? 'Connecting...' : 'Continue with Google' }}
    </button>

    <!-- Facebook OAuth -->
    <button
      type="button"
      :disabled="!!oauthLoading"
      class="w-full min-h-touch flex items-center justify-center gap-3 rounded-lg border border-gray-300 bg-white px-6 py-3 text-text font-medium transition hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-primary/50 focus:ring-offset-2 disabled:opacity-60 disabled:cursor-not-allowed"
      :aria-busy="oauthLoading === 'facebook'"
      @click="handleOAuth('facebook')"
    >
      <svg
        v-if="oauthLoading === 'facebook'"
        class="animate-spin h-5 w-5 text-gray-500"
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 24 24"
        aria-hidden="true"
      >
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
      </svg>
      <svg
        v-else
        class="h-5 w-5"
        viewBox="0 0 24 24"
        aria-hidden="true"
      >
        <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z" fill="#1877F2"/>
      </svg>
      {{ oauthLoading === 'facebook' ? 'Connecting...' : 'Continue with Facebook' }}
    </button>
  </div>
</template>
