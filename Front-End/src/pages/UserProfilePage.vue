<script setup lang="ts">
import { ref, onMounted } from 'vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import ErrorState from '@/components/common/ErrorState.vue'
import { userProfileService } from '@/services/api/userProfileService'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime } from '@/utils/formatters'
import type { User } from '@/types/auth'

const authStore = useAuthStore()

const profile = ref<User | null>(null)
const isLoading = ref(false)
const error = ref<string | null>(null)

// Edit state
const isEditing = ref(false)
const editDisplayName = ref('')
const isSaving = ref(false)
const saveError = ref<string | null>(null)
const saveSuccess = ref(false)

async function loadProfile(): Promise<void> {
  isLoading.value = true
  error.value = null
  try {
    profile.value = await userProfileService.getProfile()
  } catch (e) {
    error.value = 'Failed to load your profile. Please try again.'
  } finally {
    isLoading.value = false
  }
}

function startEditing(): void {
  if (profile.value) {
    editDisplayName.value = profile.value.displayName
    isEditing.value = true
    saveError.value = null
    saveSuccess.value = false
  }
}

function cancelEditing(): void {
  isEditing.value = false
  saveError.value = null
}

async function saveProfile(): Promise<void> {
  if (!editDisplayName.value.trim()) {
    saveError.value = 'Display name cannot be empty.'
    return
  }
  if (editDisplayName.value.trim().length < 3 || editDisplayName.value.trim().length > 50) {
    saveError.value = 'Display name must be between 3 and 50 characters.'
    return
  }

  isSaving.value = true
  saveError.value = null
  saveSuccess.value = false

  try {
    const updated = await userProfileService.updateProfile({
      displayName: editDisplayName.value.trim(),
    })
    profile.value = updated
    isEditing.value = false
    saveSuccess.value = true

    // Update auth store user if available
    if (authStore.user) {
      authStore.user.displayName = updated.displayName
    }

    // Clear success message after 3 seconds
    setTimeout(() => {
      saveSuccess.value = false
    }, 3000)
  } catch (e) {
    saveError.value = 'Failed to update profile. Please try again.'
  } finally {
    isSaving.value = false
  }
}

function getRoleLabel(role: string): string {
  switch (role) {
    case 'buyer':
      return 'Buyer'
    case 'seller':
      return 'Seller'
    case 'admin':
      return 'Administrator'
    default:
      return role
  }
}

function getRoleBadgeColor(role: string): string {
  switch (role) {
    case 'buyer':
      return 'bg-blue-100 text-blue-800'
    case 'seller':
      return 'bg-purple-100 text-purple-800'
    case 'admin':
      return 'bg-red-100 text-red-800'
    default:
      return 'bg-gray-100 text-gray-800'
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<template>
  <div class="max-w-3xl mx-auto px-4 py-6 sm:px-6 lg:px-8">
    <h1 class="text-2xl font-bold text-text mb-6">My Profile</h1>

    <!-- Loading state -->
    <LoadingSpinner
      v-if="isLoading"
      size="lg"
      message="Loading your profile..."
    />

    <!-- Error state with retry -->
    <ErrorState
      v-else-if="error"
      :message="error"
      retry-label="Retry"
      @retry="loadProfile"
    />

    <!-- Profile display -->
    <div v-else-if="profile" class="bg-card rounded-lg border border-gray-200 p-6">
      <!-- Success message -->
      <div
        v-if="saveSuccess"
        class="mb-4 p-3 rounded-md bg-green-50 border border-green-200 text-green-800 text-sm"
        role="alert"
      >
        Profile updated successfully.
      </div>

      <!-- Avatar and name header -->
      <div class="flex items-center gap-4 mb-6">
        <div class="flex items-center justify-center w-16 h-16 rounded-full bg-primary/10 text-primary text-2xl font-bold shrink-0">
          {{ profile.displayName.charAt(0).toUpperCase() }}
        </div>
        <div class="flex-1 min-w-0">
          <h2 class="text-xl font-semibold text-text truncate">{{ profile.displayName }}</h2>
          <span
            class="inline-block mt-1 px-2 py-0.5 rounded-full text-xs font-medium"
            :class="getRoleBadgeColor(profile.role)"
          >
            {{ getRoleLabel(profile.role) }}
          </span>
        </div>
      </div>

      <!-- Profile details -->
      <div class="space-y-4">
        <!-- Display Name -->
        <div class="border-b border-gray-100 pb-4">
          <label class="block text-sm font-medium text-gray-500 mb-1">Display Name</label>
          <div v-if="!isEditing" class="flex items-center justify-between">
            <span class="text-text">{{ profile.displayName }}</span>
            <button
              class="px-4 py-2 text-sm font-medium text-primary hover:text-primary/80 border border-primary rounded-md transition-colors focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 min-w-touch min-h-touch flex items-center justify-center"
              @click="startEditing"
            >
              Edit
            </button>
          </div>
          <div v-else class="space-y-2">
            <input
              v-model="editDisplayName"
              type="text"
              class="w-full px-3 py-2 border border-gray-300 rounded-md text-sm text-text focus:outline-none focus:ring-2 focus:ring-primary focus:border-primary"
              placeholder="Enter display name"
              minlength="3"
              maxlength="50"
              aria-label="Display name"
            />
            <p v-if="saveError" class="text-sm text-red-600" role="alert">{{ saveError }}</p>
            <div class="flex items-center gap-2">
              <button
                class="px-4 py-2 text-sm font-medium text-white bg-primary hover:bg-primary/90 rounded-md transition-colors focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 min-w-touch min-h-touch flex items-center justify-center disabled:opacity-50 disabled:cursor-not-allowed"
                :disabled="isSaving"
                @click="saveProfile"
              >
                {{ isSaving ? 'Saving...' : 'Save' }}
              </button>
              <button
                class="px-4 py-2 text-sm font-medium text-gray-600 hover:text-gray-800 border border-gray-300 rounded-md transition-colors focus:outline-none focus:ring-2 focus:ring-gray-300 focus:ring-offset-2 min-w-touch min-h-touch flex items-center justify-center"
                :disabled="isSaving"
                @click="cancelEditing"
              >
                Cancel
              </button>
            </div>
          </div>
        </div>

        <!-- Email -->
        <div class="border-b border-gray-100 pb-4">
          <label class="block text-sm font-medium text-gray-500 mb-1">Email</label>
          <span class="text-text">{{ profile.email }}</span>
        </div>

        <!-- Role -->
        <div class="border-b border-gray-100 pb-4">
          <label class="block text-sm font-medium text-gray-500 mb-1">Role</label>
          <span class="text-text">{{ getRoleLabel(profile.role) }}</span>
        </div>

        <!-- Member since -->
        <div>
          <label class="block text-sm font-medium text-gray-500 mb-1">Member Since</label>
          <span class="text-text">{{ formatDateTime(profile.createdAt) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>
