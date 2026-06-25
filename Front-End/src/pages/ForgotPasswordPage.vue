<script setup lang="ts">
import ForgotPasswordForm from '@/components/auth/ForgotPasswordForm.vue';
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-background px-4 py-12">
    <div class="w-full max-w-md">
      <div class="bg-card rounded-2xl shadow-md p-8">
        <!-- Heading -->
        <h1 class="text-2xl font-bold text-text text-center mb-8">Reset Password</h1>

        <!-- Forgot Password Form -->
        <ForgotPasswordForm />

        <!-- Back to login link -->
        <p class="mt-6 text-center text-sm text-gray-600">
          Remember your password?
          <router-link
            :to="{ name: 'login' }"
            class="font-medium text-primary hover:text-primary/80 transition"
          >
            Back to sign in
          </router-link>
        </p>
      </div>
    </div>
  </div>
</template>
