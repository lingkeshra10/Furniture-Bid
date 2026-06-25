<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import type { AdminUserRow, AccountStatus } from '@/types/user';
import { adminService } from '@/services/api/adminService';
import { useToast } from '@/composables/useToast';
import { formatDateTime } from '@/utils/formatters';
import { PAGE_SIZE } from '@/utils/constants';
import PaginationControls from '@/components/common/PaginationControls.vue';
import AppModal from '@/components/common/AppModal.vue';

const { showToast } = useToast();

// State
const users = ref<AdminUserRow[]>([]);
const searchQuery = ref('');
const currentPage = ref(1);
const totalUsers = ref(0);
const isLoading = ref(false);
const actionLoading = ref<string | null>(null);

// Delete confirmation modal
const showDeleteModal = ref(false);
const userToDelete = ref<AdminUserRow | null>(null);

// Computed
const filteredUsers = computed(() => {
  if (!searchQuery.value.trim()) return users.value;
  const query = searchQuery.value.toLowerCase().trim();
  return users.value.filter(
    (user) =>
      user.displayName.toLowerCase().includes(query) ||
      user.email.toLowerCase().includes(query)
  );
});

const totalPages = computed(() => {
  const total = searchQuery.value.trim() ? filteredUsers.value.length : totalUsers.value;
  return Math.max(1, Math.ceil(total / PAGE_SIZE));
});

const paginatedUsers = computed(() => {
  if (searchQuery.value.trim()) {
    const start = (currentPage.value - 1) * PAGE_SIZE;
    return filteredUsers.value.slice(start, start + PAGE_SIZE);
  }
  return users.value;
});

// Methods
async function fetchUsers() {
  isLoading.value = true;
  try {
    const response = await adminService.getUsers(currentPage.value, PAGE_SIZE);
    users.value = response.data;
    totalUsers.value = response.total;
  } catch (error) {
    showToast('Failed to load users. Please try again.', 'error');
  } finally {
    isLoading.value = false;
  }
}

async function suspendUser(user: AdminUserRow) {
  actionLoading.value = user.id;
  try {
    await adminService.suspendUser(user.id);
    user.status = 'suspended';
    showToast(`${user.displayName} has been suspended.`, 'success');
  } catch (error) {
    const message = error instanceof Error ? error.message : 'Failed to suspend user.';
    showToast(message, 'error');
  } finally {
    actionLoading.value = null;
  }
}

async function activateUser(user: AdminUserRow) {
  actionLoading.value = user.id;
  try {
    await adminService.activateUser(user.id);
    user.status = 'active';
    showToast(`${user.displayName} has been activated.`, 'success');
  } catch (error) {
    const message = error instanceof Error ? error.message : 'Failed to activate user.';
    showToast(message, 'error');
  } finally {
    actionLoading.value = null;
  }
}

function confirmDelete(user: AdminUserRow) {
  userToDelete.value = user;
  showDeleteModal.value = true;
}

async function handleDeleteConfirm() {
  if (!userToDelete.value) return;

  const user = userToDelete.value;
  showDeleteModal.value = false;
  actionLoading.value = user.id;

  try {
    await adminService.deleteUser(user.id);
    users.value = users.value.filter((u) => u.id !== user.id);
    totalUsers.value -= 1;
    showToast(`${user.displayName} has been deleted.`, 'success');
  } catch (error) {
    const message = error instanceof Error ? error.message : 'Failed to delete user.';
    showToast(message, 'error');
  } finally {
    actionLoading.value = null;
    userToDelete.value = null;
  }
}

function handleDeleteCancel() {
  showDeleteModal.value = false;
  userToDelete.value = null;
}

function handlePageChange(page: number) {
  currentPage.value = page;
}

function getStatusBadgeClasses(status: AccountStatus): string {
  switch (status) {
    case 'active':
      return 'bg-green-100 text-green-800';
    case 'suspended':
      return 'bg-amber-100 text-amber-800';
    case 'deleted':
      return 'bg-red-100 text-red-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
}

function getRoleBadgeClasses(role: string): string {
  switch (role) {
    case 'admin':
      return 'bg-purple-100 text-purple-800';
    case 'seller':
      return 'bg-blue-100 text-blue-800';
    case 'buyer':
      return 'bg-gray-100 text-gray-700';
    default:
      return 'bg-gray-100 text-gray-800';
  }
}

// Reset to page 1 when search changes
watch(searchQuery, () => {
  currentPage.value = 1;
});

// Fetch when page changes (only for non-search mode)
watch(currentPage, () => {
  if (!searchQuery.value.trim()) {
    fetchUsers();
  }
});

onMounted(() => {
  fetchUsers();
});
</script>

<template>
  <div class="space-y-4">
    <!-- Header and Search -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
      <h2 class="text-lg font-semibold text-text">User Management</h2>
      <div class="relative w-full sm:w-72">
        <svg
          class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          stroke-width="2"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
          />
        </svg>
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Search by name or email..."
          class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary focus:border-primary"
          aria-label="Search users by name or email"
        />
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="isLoading" class="flex items-center justify-center py-12">
      <svg class="animate-spin h-8 w-8 text-primary" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
      </svg>
    </div>

    <!-- Content when not loading -->
    <template v-else-if="paginatedUsers.length > 0">
      <!-- Desktop Table View (>= 768px) -->
      <div class="hidden md:block overflow-x-auto">
        <table class="w-full text-sm text-left" aria-label="User management table">
          <thead class="bg-gray-50 border-b border-gray-200">
            <tr>
              <th scope="col" class="px-4 py-3 font-medium text-gray-600">Display Name</th>
              <th scope="col" class="px-4 py-3 font-medium text-gray-600">Email</th>
              <th scope="col" class="px-4 py-3 font-medium text-gray-600">Role</th>
              <th scope="col" class="px-4 py-3 font-medium text-gray-600">Registration Date</th>
              <th scope="col" class="px-4 py-3 font-medium text-gray-600">Status</th>
              <th scope="col" class="px-4 py-3 font-medium text-gray-600">Actions</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr
              v-for="user in paginatedUsers"
              :key="user.id"
              class="hover:bg-gray-50 transition-colors"
            >
              <td class="px-4 py-3 font-medium text-text">{{ user.displayName }}</td>
              <td class="px-4 py-3 text-gray-600">{{ user.email }}</td>
              <td class="px-4 py-3">
                <span
                  class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium capitalize"
                  :class="getRoleBadgeClasses(user.role)"
                >
                  {{ user.role }}
                </span>
              </td>
              <td class="px-4 py-3 text-gray-600">{{ formatDateTime(user.registeredAt) }}</td>
              <td class="px-4 py-3">
                <span
                  class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium capitalize"
                  :class="getStatusBadgeClasses(user.status)"
                >
                  {{ user.status }}
                </span>
              </td>
              <td class="px-4 py-3">
                <div class="flex items-center gap-2">
                  <button
                    v-if="user.status === 'active'"
                    class="px-3 py-1.5 text-xs font-medium text-amber-700 bg-amber-50 hover:bg-amber-100 rounded transition-colors min-w-touch min-h-touch flex items-center justify-center disabled:opacity-50 disabled:cursor-not-allowed"
                    :disabled="actionLoading === user.id"
                    :aria-label="`Suspend ${user.displayName}`"
                    @click="suspendUser(user)"
                  >
                    Suspend
                  </button>
                  <button
                    v-if="user.status === 'suspended'"
                    class="px-3 py-1.5 text-xs font-medium text-green-700 bg-green-50 hover:bg-green-100 rounded transition-colors min-w-touch min-h-touch flex items-center justify-center disabled:opacity-50 disabled:cursor-not-allowed"
                    :disabled="actionLoading === user.id"
                    :aria-label="`Activate ${user.displayName}`"
                    @click="activateUser(user)"
                  >
                    Activate
                  </button>
                  <button
                    class="px-3 py-1.5 text-xs font-medium text-red-700 bg-red-50 hover:bg-red-100 rounded transition-colors min-w-touch min-h-touch flex items-center justify-center disabled:opacity-50 disabled:cursor-not-allowed"
                    :disabled="actionLoading === user.id"
                    :aria-label="`Delete ${user.displayName}`"
                    @click="confirmDelete(user)"
                  >
                    Delete
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Mobile Card View (< 768px) - Stacked Cards -->
      <div class="md:hidden space-y-3">
        <div
          v-for="user in paginatedUsers"
          :key="'mobile-' + user.id"
          class="bg-card border border-gray-200 rounded-lg p-4 space-y-3"
        >
          <div class="flex items-start justify-between">
            <div>
              <p class="font-medium text-text">{{ user.displayName }}</p>
              <p class="text-sm text-gray-500">{{ user.email }}</p>
            </div>
            <span
              class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium capitalize"
              :class="getStatusBadgeClasses(user.status)"
            >
              {{ user.status }}
            </span>
          </div>

          <div class="flex items-center gap-3 text-sm text-gray-600">
            <span
              class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium capitalize"
              :class="getRoleBadgeClasses(user.role)"
            >
              {{ user.role }}
            </span>
            <span>{{ formatDateTime(user.registeredAt) }}</span>
          </div>

          <div class="flex items-center gap-2 pt-2 border-t border-gray-100">
            <button
              v-if="user.status === 'active'"
              class="flex-1 px-3 py-2 text-xs font-medium text-amber-700 bg-amber-50 hover:bg-amber-100 rounded transition-colors min-h-touch flex items-center justify-center disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="actionLoading === user.id"
              :aria-label="`Suspend ${user.displayName}`"
              @click="suspendUser(user)"
            >
              Suspend
            </button>
            <button
              v-if="user.status === 'suspended'"
              class="flex-1 px-3 py-2 text-xs font-medium text-green-700 bg-green-50 hover:bg-green-100 rounded transition-colors min-h-touch flex items-center justify-center disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="actionLoading === user.id"
              :aria-label="`Activate ${user.displayName}`"
              @click="activateUser(user)"
            >
              Activate
            </button>
            <button
              class="flex-1 px-3 py-2 text-xs font-medium text-red-700 bg-red-50 hover:bg-red-100 rounded transition-colors min-h-touch flex items-center justify-center disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="actionLoading === user.id"
              :aria-label="`Delete ${user.displayName}`"
              @click="confirmDelete(user)"
            >
              Delete
            </button>
          </div>
        </div>
      </div>
    </template>

    <!-- Empty State -->
    <div v-else-if="!isLoading" class="text-center py-12">
      <svg class="mx-auto h-12 w-12 text-gray-300" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
        <path stroke-linecap="round" stroke-linejoin="round" d="M15 19.128a9.38 9.38 0 002.625.372 9.337 9.337 0 004.121-.952 4.125 4.125 0 00-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 018.624 21c-2.331 0-4.512-.645-6.374-1.766l-.001-.109a6.375 6.375 0 0111.964-3.07M12 6.375a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0zm8.25 2.25a2.625 2.625 0 11-5.25 0 2.625 2.625 0 015.25 0z" />
      </svg>
      <p class="mt-3 text-sm text-gray-500">
        {{ searchQuery.trim() ? 'No users match your search.' : 'No users found.' }}
      </p>
    </div>

    <!-- Pagination -->
    <div v-if="!isLoading && totalPages > 1" class="pt-4">
      <PaginationControls
        :current-page="currentPage"
        :total-pages="totalPages"
        @page-change="handlePageChange"
      />
    </div>

    <!-- Delete Confirmation Modal -->
    <AppModal
      :show="showDeleteModal"
      title="Delete User"
      :message="`Are you sure you want to delete ${userToDelete?.displayName ?? 'this user'}? This action cannot be undone.`"
      confirm-label="Delete"
      cancel-label="Cancel"
      confirm-variant="danger"
      @confirm="handleDeleteConfirm"
      @cancel="handleDeleteCancel"
    />
  </div>
</template>
