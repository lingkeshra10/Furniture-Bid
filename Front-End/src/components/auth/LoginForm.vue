<script setup lang="ts">
import { ref, reactive } from 'vue';
import { loginSchema, type LoginFormData } from '@/utils/validators';
import { useAuthStore } from '@/stores/auth';
import { useToast } from '@/composables/useToast';

const emit = defineEmits<{
  success: [];
}>();

const authStore = useAuthStore();
const { showToast } = useToast();

const form = reactive<LoginFormData>({
  email: '',
  password: '',
});

const errors = reactive<Record<string, string>>({
  email: '',
  password: '',
});

const serverError = ref('');

function validateField(field: keyof LoginFormData): void {
  const result = loginSchema.shape[field].safeParse(form[field]);
  errors[field] = result.success ? '' : result.error.issues[0].message;
}

function validateAll(): boolean {
  const result = loginSchema.safeParse(form);
  if (result.success) {
    errors.email = '';
    errors.password = '';
    return true;
  }
  // Reset errors
  errors.email = '';
  errors.password = '';
  for (const issue of result.error.issues) {
    const field = issue.path[0] as string;
    if (!errors[field]) {
      errors[field] = issue.message;
    }
  }
  return false;
}

async function handleSubmit(): Promise<void> {
  serverError.value = '';
  if (!validateAll()) return;

  try {
    await authStore.login({ email: form.email, password: form.password });
    showToast('Login successful', 'success');
    emit('success');
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : 'Login failed. Please try again.';
    serverError.value = message;
  }
}
</script>

<template>
  <form @submit.prevent="handleSubmit" class="space-y-5" novalidate aria-label="Login form">
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
      <label for="login-email" class="block text-sm font-medium text-text mb-1">
        Email
      </label>
      <input
        id="login-email"
        v-model="form.email"
        type="email"
        autocomplete="email"
        placeholder="you@example.com"
        class="w-full min-h-touch rounded-lg border px-4 py-3 text-text placeholder-gray-400 transition focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
        :class="errors.email ? 'border-red-400' : 'border-gray-300'"
        :aria-invalid="!!errors.email"
        :aria-describedby="errors.email ? 'login-email-error' : undefined"
        @blur="validateField('email')"
      />
      <p
        v-if="errors.email"
        id="login-email-error"
        class="mt-1 text-sm text-red-600"
        role="alert"
      >
        {{ errors.email }}
      </p>
    </div>

    <!-- Password field -->
    <div>
      <label for="login-password" class="block text-sm font-medium text-text mb-1">
        Password
      </label>
      <input
        id="login-password"
        v-model="form.password"
        type="password"
        autocomplete="current-password"
        placeholder="Enter your password"
        class="w-full min-h-touch rounded-lg border px-4 py-3 text-text placeholder-gray-400 transition focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
        :class="errors.password ? 'border-red-400' : 'border-gray-300'"
        :aria-invalid="!!errors.password"
        :aria-describedby="errors.password ? 'login-password-error' : undefined"
        @blur="validateField('password')"
      />
      <p
        v-if="errors.password"
        id="login-password-error"
        class="mt-1 text-sm text-red-600"
        role="alert"
      >
        {{ errors.password }}
      </p>
    </div>

    <!-- Submit button -->
    <button
      type="submit"
      :disabled="authStore.isLoading.value"
      class="w-full min-h-touch flex items-center justify-center rounded-lg bg-primary px-6 py-3 text-white font-medium transition hover:bg-primary/90 focus:outline-none focus:ring-2 focus:ring-primary/50 focus:ring-offset-2 disabled:opacity-60 disabled:cursor-not-allowed"
    >
      <svg
        v-if="authStore.isLoading.value"
        class="animate-spin -ml-1 mr-2 h-5 w-5 text-white"
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 24 24"
        aria-hidden="true"
      >
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
      </svg>
      {{ authStore.isLoading.value ? 'Signing in...' : 'Sign In' }}
    </button>
  </form>
</template>
