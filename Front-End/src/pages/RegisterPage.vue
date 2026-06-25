<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useToast } from '@/composables/useToast';
import RegisterForm from '@/components/auth/RegisterForm.vue';

const router = useRouter();
const { showToast } = useToast();

function handleRegisterSuccess(): void {
  showToast('Registration successful! Please sign in.', 'success');
  router.push({ name: 'login' });
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-background px-4 py-12">
    <div class="w-full max-w-md">
      <div class="bg-card rounded-2xl shadow-md p-8">
        <!-- Heading -->
        <h1 class="text-2xl font-bold text-text text-center mb-8">Create Account</h1>

        <!-- Register Form -->
        <RegisterForm @success="handleRegisterSuccess" />

        <!-- Login link -->
        <p class="mt-6 text-center text-sm text-gray-600">
          Already have an account?
          <router-link
            :to="{ name: 'login' }"
            class="font-medium text-primary hover:text-primary/80 transition"
          >
            Sign in
          </router-link>
        </p>
      </div>
    </div>
  </div>
</template>
