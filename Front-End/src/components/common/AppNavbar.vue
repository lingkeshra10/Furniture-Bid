<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import NotificationBell from '@/components/notifications/NotificationBell.vue';

const router = useRouter();
const authStore = useAuthStore();

const mobileMenuOpen = ref(false);
const userMenuOpen = ref(false);

const isSeller = computed(() => authStore.userRole.value === 'seller' || authStore.userRole.value === 'admin');
const isAdmin = computed(() => authStore.userRole.value === 'admin');

interface NavLink {
  label: string;
  to: string;
  show: boolean;
}

const navLinks = computed<NavLink[]>(() => [
  { label: 'Catalog', to: '/catalog', show: true },
  { label: 'Watchlist', to: '/watchlist', show: authStore.isAuthenticated.value },
  { label: 'Bid History', to: '/bidding-history', show: authStore.isAuthenticated.value },
  { label: 'Seller Dashboard', to: '/seller/dashboard', show: isSeller.value },
  { label: 'Admin', to: '/admin', show: isAdmin.value },
]);

const visibleLinks = computed(() => navLinks.value.filter((link) => link.show));

function toggleMobileMenu() {
  mobileMenuOpen.value = !mobileMenuOpen.value;
  if (mobileMenuOpen.value) {
    userMenuOpen.value = false;
  }
}

function toggleUserMenu() {
  userMenuOpen.value = !userMenuOpen.value;
}

function closeMenus() {
  mobileMenuOpen.value = false;
  userMenuOpen.value = false;
}

async function handleLogout() {
  closeMenus();
  await authStore.logout();
  router.push('/login');
}

function navigateTo(path: string) {
  closeMenus();
  router.push(path);
}
</script>

<template>
  <nav class="bg-card shadow-sm border-b border-gray-100 relative z-50" aria-label="Main navigation">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-16">
        <!-- Logo / Brand -->
        <div class="flex-shrink-0">
          <router-link to="/catalog" class="text-primary font-bold text-xl" @click="closeMenus">
            FurnitureBid
          </router-link>
        </div>

        <!-- Desktop Navigation (full menu) -->
        <div class="hidden desktop:flex desktop:items-center desktop:gap-1">
          <router-link
            v-for="link in visibleLinks"
            :key="link.to"
            :to="link.to"
            class="px-4 py-2 rounded-md text-sm font-medium text-text hover:bg-background hover:text-primary transition-colors min-w-touch min-h-touch flex items-center"
            active-class="bg-background text-primary"
          >
            {{ link.label }}
          </router-link>
        </div>

        <!-- Tablet Navigation (condensed) -->
        <div class="hidden tablet:flex tablet:items-center tablet:gap-0.5">
          <router-link
            v-for="link in visibleLinks"
            :key="link.to"
            :to="link.to"
            class="px-3 py-2 rounded-md text-xs font-medium text-text hover:bg-background hover:text-primary transition-colors min-w-touch min-h-touch flex items-center"
            active-class="bg-background text-primary"
          >
            {{ link.label }}
          </router-link>
        </div>

        <!-- Right side actions -->
        <div class="flex items-center gap-2">
          <!-- Notification Bell -->
          <NotificationBell v-if="authStore.isAuthenticated.value" />

          <!-- User Menu (Desktop/Tablet) -->
          <div v-if="authStore.isAuthenticated.value" class="relative hidden tablet:block desktop:block">
            <button
              class="flex items-center gap-2 px-3 py-2 rounded-md text-sm font-medium text-text hover:bg-background hover:text-primary transition-colors min-w-touch min-h-touch"
              aria-haspopup="true"
              :aria-expanded="userMenuOpen"
              @click="toggleUserMenu"
            >
              <span class="truncate max-w-[120px]">{{ authStore.user.value?.displayName ?? 'Account' }}</span>
              <svg class="h-4 w-4 transition-transform" :class="{ 'rotate-180': userMenuOpen }" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M19 9l-7 7-7-7" />
              </svg>
            </button>

            <!-- User Dropdown -->
            <Transition
              enter-active-class="transition ease-out duration-100"
              enter-from-class="opacity-0 scale-95"
              enter-to-class="opacity-100 scale-100"
              leave-active-class="transition ease-in duration-75"
              leave-from-class="opacity-100 scale-100"
              leave-to-class="opacity-0 scale-95"
            >
              <div
                v-if="userMenuOpen"
                class="absolute right-0 mt-2 w-48 bg-card rounded-md shadow-lg border border-gray-100 py-1 z-50"
                role="menu"
              >
                <router-link
                  to="/profile"
                  class="block px-4 py-2 text-sm text-text hover:bg-background hover:text-primary min-h-touch flex items-center"
                  role="menuitem"
                  @click="closeMenus"
                >
                  Profile
                </router-link>
                <button
                  class="w-full text-left px-4 py-2 text-sm text-text hover:bg-background hover:text-primary min-h-touch flex items-center"
                  role="menuitem"
                  @click="handleLogout"
                >
                  Logout
                </button>
              </div>
            </Transition>
          </div>

          <!-- Login button (when not authenticated) -->
          <router-link
            v-if="!authStore.isAuthenticated.value"
            to="/login"
            class="hidden tablet:inline-flex desktop:inline-flex items-center px-4 py-2 rounded-md text-sm font-medium text-white bg-primary hover:bg-primary/90 transition-colors min-w-touch min-h-touch"
          >
            Login
          </router-link>

          <!-- Mobile Hamburger -->
          <button
            class="tablet:hidden desktop:hidden p-2 rounded-md text-text hover:bg-background hover:text-primary transition-colors min-w-touch min-h-touch flex items-center justify-center"
            aria-label="Open menu"
            :aria-expanded="mobileMenuOpen"
            @click="toggleMobileMenu"
          >
            <svg v-if="!mobileMenuOpen" class="h-6 w-6" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M4 6h16M4 12h16M4 18h16" />
            </svg>
            <svg v-else class="h-6 w-6" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- Mobile Menu -->
    <Transition
      enter-active-class="transition ease-out duration-200"
      enter-from-class="opacity-0 -translate-y-1"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition ease-in duration-150"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 -translate-y-1"
    >
      <div
        v-if="mobileMenuOpen"
        class="tablet:hidden desktop:hidden bg-card border-t border-gray-100 shadow-lg"
      >
        <div class="px-4 py-3 space-y-1">
          <router-link
            v-for="link in visibleLinks"
            :key="link.to"
            :to="link.to"
            class="block px-4 py-3 rounded-md text-base font-medium text-text hover:bg-background hover:text-primary transition-colors min-h-touch flex items-center"
            active-class="bg-background text-primary"
            @click="closeMenus"
          >
            {{ link.label }}
          </router-link>

          <!-- Mobile auth actions -->
          <template v-if="authStore.isAuthenticated.value">
            <router-link
              to="/profile"
              class="block px-4 py-3 rounded-md text-base font-medium text-text hover:bg-background hover:text-primary transition-colors min-h-touch flex items-center"
              @click="closeMenus"
            >
              Profile
            </router-link>
            <button
              class="w-full text-left px-4 py-3 rounded-md text-base font-medium text-text hover:bg-background hover:text-primary transition-colors min-h-touch flex items-center"
              @click="handleLogout"
            >
              Logout
            </button>
          </template>
          <template v-else>
            <router-link
              to="/login"
              class="block px-4 py-3 rounded-md text-base font-medium text-white bg-primary hover:bg-primary/90 text-center transition-colors min-h-touch flex items-center justify-center"
              @click="closeMenus"
            >
              Login
            </router-link>
          </template>
        </div>
      </div>
    </Transition>
  </nav>
</template>
