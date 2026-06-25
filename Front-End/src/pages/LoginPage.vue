<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useToast } from '@/composables/useToast';
import LoginForm from '@/components/auth/LoginForm.vue';
import SocialLoginButtons from '@/components/auth/SocialLoginButtons.vue';

const router = useRouter();
const { showToast } = useToast();

function handleLoginSuccess(): void {
  router.push({ name: 'catalog' });
}

function handleSocialError(message: string): void {
  showToast(message, 'error');
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-background px-4 py-12">
    <div class="w-full max-w-md">
      <div class="bg-card rounded-2xl shadow-md p-8">
        <!-- Heading -->
        <h1 class="text-2xl font-bold text-text text-center mb-8">Sign In</h1>

        <!-- Login Form -->
        <LoginForm @success="handleLoginSuccess" />

        <!-- Forgot password link -->
        <div class="mt-4 text-center">
          <router-link
            :to="{ name: 'forgot-password' }"
            class="text-sm text-primary hover:text-primary/80 transition"
          >
            Forgot your password?
          </router-link>
        </div>

        <!-- Divider -->
        <div class="relative my-6">
          <div class="absolute inset-0 flex items-center">
            <div class="w-full border-t border-gray-200"></div>
          </div>
          <div class="relative flex justify-center text-sm">
            <span class="bg-card px-4 text-gray-500">Or continue with</span>
          </div>
        </div>

        <!-- Social Login -->
        <SocialLoginButtons @success="handleLoginSuccess" @error="handleSocialError" />

        <!-- Register link -->
        <p class="mt-6 text-center text-sm text-gray-600">
          Don't have an account?
          <router-link
            :to="{ name: 'register' }"
            class="font-medium text-primary hover:text-primary/80 transition"
          >
            Create one
          </router-link>
        </p>
      </div>
    </div>
  </div>
</template>
