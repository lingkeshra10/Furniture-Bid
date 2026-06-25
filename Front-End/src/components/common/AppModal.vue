<script setup lang="ts">
import { watch, ref, nextTick, onBeforeUnmount } from 'vue';

interface Props {
  show: boolean;
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  confirmVariant?: 'danger' | 'primary';
}

const props = withDefaults(defineProps<Props>(), {
  confirmLabel: 'Confirm',
  cancelLabel: 'Cancel',
  confirmVariant: 'primary',
});

const emit = defineEmits<{
  confirm: [];
  cancel: [];
}>();

const modalRef = ref<HTMLElement | null>(null);
const confirmBtnRef = ref<HTMLButtonElement | null>(null);

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape') {
    emit('cancel');
    return;
  }

  // Focus trap
  if (e.key === 'Tab' && modalRef.value) {
    const focusable = modalRef.value.querySelectorAll<HTMLElement>(
      'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
    );
    const first = focusable[0];
    const last = focusable[focusable.length - 1];

    if (e.shiftKey && document.activeElement === first) {
      e.preventDefault();
      last?.focus();
    } else if (!e.shiftKey && document.activeElement === last) {
      e.preventDefault();
      first?.focus();
    }
  }
}

watch(
  () => props.show,
  async (open) => {
    if (open) {
      await nextTick();
      confirmBtnRef.value?.focus();
      document.addEventListener('keydown', handleKeydown);
    } else {
      document.removeEventListener('keydown', handleKeydown);
    }
  }
);

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown);
});

const confirmClasses = {
  danger: 'bg-red-600 hover:bg-red-700 text-white focus:ring-red-500',
  primary: 'bg-primary hover:bg-primary/90 text-white focus:ring-primary',
};
</script>

<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition ease-out duration-200"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition ease-in duration-150"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="show"
        class="fixed inset-0 z-[10000] flex items-center justify-center p-4"
        role="dialog"
        aria-modal="true"
        :aria-labelledby="'modal-title'"
      >
        <!-- Backdrop -->
        <div
          class="absolute inset-0 bg-black/50"
          @click="emit('cancel')"
        />

        <!-- Modal content -->
        <div
          ref="modalRef"
          class="relative bg-card rounded-lg shadow-xl max-w-md w-full p-6 z-10"
        >
          <!-- Title -->
          <h2 id="modal-title" class="text-lg font-semibold text-text mb-2">
            {{ title }}
          </h2>

          <!-- Message -->
          <p class="text-sm text-gray-600 mb-6">
            {{ message }}
          </p>

          <!-- Actions -->
          <div class="flex items-center justify-end gap-3">
            <button
              class="px-4 py-2 rounded-md text-sm font-medium text-text bg-gray-100 hover:bg-gray-200 transition-colors focus:outline-none focus:ring-2 focus:ring-gray-400 focus:ring-offset-2 min-w-touch min-h-touch flex items-center justify-center"
              @click="emit('cancel')"
            >
              {{ cancelLabel }}
            </button>
            <button
              ref="confirmBtnRef"
              class="px-4 py-2 rounded-md text-sm font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 min-w-touch min-h-touch flex items-center justify-center"
              :class="confirmClasses[confirmVariant]"
              @click="emit('confirm')"
            >
              {{ confirmLabel }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
