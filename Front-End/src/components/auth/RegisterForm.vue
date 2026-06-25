<script setup lang="ts">
import { ref, reactive } from 'vue';
import { registrationSchema, type RegistrationFormData } from '@/utils/validators';
import { useAuthStore } from '@/stores/auth';
import { useToast } from '@/composables/useToast';

const emit = defineEmits<{
  success: [];
}>();

const authStore = useAuthStore();
const { showToast } = useToast();

const form = reactive<RegistrationFormData>({
  email: '',
  password: '',
  displayName: '',
});

const errors = reactive<Record<string, string>>({
  email: '',
  password: '',
  displayName: '',
});

const serverError = ref('');

function validateField(field: keyof RegistrationFormData): void {
  const result = registrationSchema.shape[field].safeParse(form[field]);
  errors[field] = result.success ? '' : result.error.issues[0].message;
}

function validateAll(): boolean {
  const result = registrationSchema.safeParse(form);
  if (result.success) {
    errors.email = '';
    errors.password = '';
    errors.displayName = '';
    return true;
  }
  // Reset errors
  errors.email = '';
  errors.password = '';
  errors.displayName = '';
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
    await authStore.register({
      email: form.email,
      password: form.password,
      displayName: form.displayName,
    });
    showToast('Registration successful', 'success');
    emit('success');
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : 'Registration failed. Please try again.';
    serverError.value = message;
  }
}
</script>

<template>
  <form @submit.prevent="handleSubmit" class="space-y-5" novalidate aria-label="Registration form">
    <!-- Server error -->
    <div
      v-if="serverError"
      class="rounded-md bg-red-50 border border-red-200 p-3 text-sm text-red-700"
      role="alert"
    >
      {{ serverError }}
    </div>

    <!-- Display Name field -->
    <div>
      <label for="register-displayName" class="block text-sm font-medium text-text mb-1">
        Display Name
      </label>
      <input
        id="register-displayName"
        v-model="form.displayName"
        type="text"
        autocomplete="name"
        placeholder="Your display name"
        class="w-full min-h-touch rounded-lg border px-4 py-3 text-text placeholder-gray-400 transition focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
        :class="errors.displayName ? 'border-red-400' : 'border-gray-300'"
        :aria-invalid="!!errors.displayName"
        :aria-describedby="errors.displayName ? 'register-displayName-error' : undefined"
        @blur="validateField('displayName')"
      />
      <p
        v-if="errors.displayName"
        id="register-displayName-error"
        class="mt-1 text-sm text-red-600"
        role="alert"
      >
        {{ errors.displayName }}
      </p>
    </div>

    <!-- Email field -->
    <div>
      <label for="register-email" class="block text-sm font-medium text-text mb-1">
        Email
      </label>
      <input
        id="register-email"
        v-model="form.email"
        type="email"
        autocomplete="email"
        placeholder="you@example.com"
        class="w-full min-h-touch rounded-lg border px-4 py-3 text-text placeholder-gray-400 transition focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
        :class="errors.email ? 'border-red-400' : 'border-gray-300'"
        :aria-invalid="!!errors.email"
        :aria-describedby="errors.email ? 'register-email-error' : undefined"
        @blur="validateField('email')"
      />
      <p
        v-if="errors.email"
        id="register-email-error"
        class="mt-1 text-sm text-red-600"
        role="alert"
      >
        {{ errors.email }}
      </p>
    </div>

    <!-- Password field -->
    <div>
      <label for="register-password" class="block text-sm font-medium text-text mb-1">
        Password
      </label>
      <input
        id="register-password"
        v-model="form.password"
        type="password"
        autocomplete="new-password"
        placeholder="8+ characters, upper/lowercase & digit"
        class="w-full min-h-touch rounded-lg border px-4 py-3 text-text placeholder-gray-400 transition focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
        :class="errors.password ? 'border-red-400' : 'border-gray-300'"
        :aria-invalid="!!errors.password"
        :aria-describedby="errors.password ? 'register-password-error' : 'register-password-hint'"
        @blur="validateField('password')"
      />
      <p
        v-if="errors.password"
        id="register-password-error"
        class="mt-1 text-sm text-red-600"
        role="alert"
      >
        {{ errors.password }}
      </p>
      <p
        v-else
        id="register-password-hint"
        class="mt-1 text-xs text-gray-500"
      >
        8-64 characters with uppercase, lowercase, and a digit
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
      {{ authStore.isLoading.value ? 'Creating account...' : 'Create Account' }}
    </button>
  </form>
</template>
