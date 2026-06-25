<script setup lang="ts">
import { useNotification } from '@/composables/useNotification';
import NotificationDropdown from './NotificationDropdown.vue';

/**
 * NotificationBell component displays a bell icon with an unread count badge.
 * Clicking the bell toggles the notification dropdown.
 *
 * Requirements:
 * - 7.1: Display bell icon showing unread count, displaying "99+" when count exceeds 99
 * - 7.2: Click opens dropdown with 20 most recent notifications
 */
const {
  unreadCount,
  displayCount,
  isDropdownOpen,
  toggleDropdown,
  closeDropdown,
} = useNotification();

function handleClickOutside(event: MouseEvent) {
  const target = event.target as HTMLElement;
  if (!target.closest('.notification-bell-container')) {
    closeDropdown();
  }
}

// Close dropdown on outside click
if (typeof window !== 'undefined') {
  window.addEventListener('click', handleClickOutside);
}
</script>

<template>
  <div class="notification-bell-container relative">
    <button
      class="relative p-2 rounded-full text-text hover:bg-background hover:text-primary transition-colors min-w-[44px] min-h-[44px] flex items-center justify-center"
      aria-label="Notifications"
      :aria-expanded="isDropdownOpen"
      aria-haspopup="true"
      @click.stop="toggleDropdown"
    >
      <!-- Bell Icon -->
      <svg
        class="h-6 w-6"
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
        stroke-width="2"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
        />
      </svg>

      <!-- Unread count badge -->
      <span
        v-if="unreadCount > 0"
        class="absolute top-0.5 right-0.5 inline-flex items-center justify-center px-1.5 py-0.5 text-xs font-bold leading-none text-white bg-accent rounded-full min-w-[18px]"
        aria-label="Unread notifications count"
      >
        {{ displayCount }}
      </span>
    </button>

    <!-- Dropdown -->
    <Transition
      enter-active-class="transition ease-out duration-200"
      enter-from-class="opacity-0 scale-95 -translate-y-1"
      enter-to-class="opacity-100 scale-100 translate-y-0"
      leave-active-class="transition ease-in duration-150"
      leave-from-class="opacity-100 scale-100 translate-y-0"
      leave-to-class="opacity-0 scale-95 -translate-y-1"
    >
      <NotificationDropdown v-if="isDropdownOpen" />
    </Transition>
  </div>
</template>
