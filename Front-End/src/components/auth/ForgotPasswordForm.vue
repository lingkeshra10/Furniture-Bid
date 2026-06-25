<script setup lang="ts">
import { ref } from 'vue';
import { z } from 'zod';
import { authService } from '@/services/api/authService';

const emailSchema = z
  .string({ error: 'Email is required' })
  .min(1, 'Email is required')
  .email('Please enter a valid email address');

const email = ref('');
const emailError = ref('');
const serverError = ref('');
const isLoading = ref(false);
const isSuccess = ref(false);

function validateEmail(): boolean {
  const result = emailSchema.safeParse(email.value);
  emailError.value = result.success ? '' : result.error.issues[0].message;
  return result.success;
}

async function handleSubmit(): Promise<void> {
  serverError.value = '';
  if (!validateEmail()) return;

  isLoading.value = true;
  try {
    await authService.resetPassword({ email: email.value });
    isSuccess.value = true;
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : 'Failed to send reset email. Please try again.';
    serverError.value = message;
  } finally {
    isLoading.value = false;
  }
}
</script>

<template>
  <!-- Success confirmation -->
  <div v-if="isSuccess" class="text-center space-y-4" role="status" aria-live="polite">
    <div class="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-green-100">
      <svg class="h-6 w-6 text-success" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
      </svg>
    </div>
    <h3 class="text-lg font-medium text-text">Check your email</h3>
    <p class="text-sm text-gray-600">
      If an account exists with <strong>{{ email }}</strong>, we've sent password reset instructions to that address.
    </p>
  </div>

  <!-- Form -->
  <form v-else @submit.prevent="handleSubmit" class="space-y-5" novalidate aria-label="Forgot password form">
    <p class="text-sm text-gray-600">
      Enter the email address associated with your account and we'll send you a link to reset your password.
    </p>

    <!-- Server error -->
    <div
      v-if="serverError"
      class="rounded-md bg-red-50 border border-red-200 p-3 text-sm text-red-700"
      role="alert"
    >
      {{ serverError }}
    </div>

    <!-- Email field -->
    <div>
      <label for="forgot-email" class="block text-sm font-medium text-text mb-1">
        Email
      </label>
      <input
        id="forgot-email"
        v-model="email"
        type="email"
        autocomplete="email"
        placeholder="you@example.com"
        class="w-full min-h-touch rounded-lg border px-4 py-3 text-text placeholder-gray-400 transition focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
        :class="emailError ? 'border-red-400' : 'border-gray-300'"
        :aria-invalid="!!emailError"
        :aria-describedby="emailError ? 'forgot-email-error' : undefined"
        @blur="validateEmail()"
      />
      <p
        v-if="emailError"
        id="forgot-email-error"
        class="mt-1 text-sm text-red-600"
        role="alert"
      >
        {{ emailError }}
      </p>
    </div>

    <!-- Submit button -->
    <button
      type="submit"
      :disabled="isLoading"
      class="w-full min-h-touch flex items-center justify-center rounded-lg bg-primary px-6 py-3 text-white font-medium transition hover:bg-primary/90 focus:outline-none focus:ring-2 focus:ring-primary/50 focus:ring-offset-2 disabled:opacity-60 disabled:cursor-not-allowed"
    >
      <svg
        v-if="isLoading"
        class="animate-spin -ml-1 mr-2 h-5 w-5 text-white"
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 24 24"
        aria-hidden="true"
      >
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
      </svg>
      {{ isLoading ? 'Sending...' : 'Send Reset Link' }}
    </button>
  </form>
</template>
