<script setup lang="ts">
import { ref, computed } from 'vue';
import { MAX_LISTING_IMAGES, MAX_IMAGE_SIZE, SUPPORTED_IMAGE_TYPES } from '@/utils/constants';

const emit = defineEmits<{
  'update:files': [files: File[]];
}>();

const props = defineProps<{
  files: File[];
  error?: string;
}>();

const dragOver = ref(false);
const fileInputRef = ref<HTMLInputElement | null>(null);

const previews = computed(() =>
  props.files.map((file) => ({
    name: file.name,
    url: URL.createObjectURL(file),
    size: file.size,
  }))
);

const canAddMore = computed(() => props.files.length < MAX_LISTING_IMAGES);

function validateFile(file: File): string | null {
  if (!SUPPORTED_IMAGE_TYPES.includes(file.type as typeof SUPPORTED_IMAGE_TYPES[number])) {
    return `"${file.name}" is not a supported format. Use JPEG, PNG, or WebP.`;
  }
  if (file.size > MAX_IMAGE_SIZE) {
    return `"${file.name}" exceeds the 5MB size limit.`;
  }
  return null;
}

const localError = ref('');

function addFiles(newFiles: FileList | File[]): void {
  localError.value = '';
  const fileArray = Array.from(newFiles);
  const validFiles: File[] = [];

  for (const file of fileArray) {
    const error = validateFile(file);
    if (error) {
      localError.value = error;
      return;
    }
    validFiles.push(file);
  }

  const totalCount = props.files.length + validFiles.length;
  if (totalCount > MAX_LISTING_IMAGES) {
    localError.value = `You can upload a maximum of ${MAX_LISTING_IMAGES} images. You already have ${props.files.length}.`;
    return;
  }

  emit('update:files', [...props.files, ...validFiles]);
}

function removeFile(index: number): void {
  localError.value = '';
  const updated = [...props.files];
  updated.splice(index, 1);
  emit('update:files', updated);
}

function handleDrop(event: DragEvent): void {
  dragOver.value = false;
  if (event.dataTransfer?.files) {
    addFiles(event.dataTransfer.files);
  }
}

function handleDragOver(event: DragEvent): void {
  event.preventDefault();
  dragOver.value = true;
}

function handleDragLeave(): void {
  dragOver.value = false;
}

function openFilePicker(): void {
  fileInputRef.value?.click();
}

function handleFileInput(event: Event): void {
  const input = event.target as HTMLInputElement;
  if (input.files) {
    addFiles(input.files);
    input.value = '';
  }
}

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}
</script>

<template>
  <div class="space-y-3">
    <label class="block text-sm font-medium text-text">
      Images <span class="text-red-500">*</span>
      <span class="text-xs text-gray-500 ml-1">({{ props.files.length }}/{{ MAX_LISTING_IMAGES }})</span>
    </label>

    <!-- Drop zone -->
    <div
      v-if="canAddMore"
      class="relative border-2 border-dashed rounded-lg p-6 text-center transition-colors cursor-pointer"
      :class="[
        dragOver ? 'border-primary bg-primary/5' : 'border-gray-300 hover:border-primary/50',
      ]"
      role="button"
      tabindex="0"
      aria-label="Drop images here or click to browse"
      @drop.prevent="handleDrop"
      @dragover="handleDragOver"
      @dragleave="handleDragLeave"
      @click="openFilePicker"
      @keydown.enter="openFilePicker"
      @keydown.space.prevent="openFilePicker"
    >
      <svg
        class="mx-auto h-10 w-10 text-gray-400"
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
        aria-hidden="true"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="1.5"
          d="M12 16V4m0 0l-4 4m4-4l4 4M4 20h16"
        />
      </svg>
      <p class="mt-2 text-sm text-gray-600">
        Drag & drop images here, or
        <span class="text-primary font-medium">browse</span>
      </p>
      <p class="mt-1 text-xs text-gray-500">
        JPEG, PNG, or WebP — max 5MB each
      </p>
    </div>

    <!-- Hidden file input -->
    <input
      ref="fileInputRef"
      type="file"
      multiple
      accept="image/jpeg,image/png,image/webp"
      class="hidden"
      @change="handleFileInput"
    />

    <!-- Error message -->
    <p
      v-if="localError || props.error"
      class="text-sm text-red-600"
      role="alert"
    >
      {{ localError || props.error }}
    </p>

    <!-- Image previews -->
    <div v-if="previews.length > 0" class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-3">
      <div
        v-for="(preview, index) in previews"
        :key="index"
        class="relative group rounded-lg overflow-hidden border border-gray-200"
      >
        <img
          :src="preview.url"
          :alt="`Preview ${index + 1}: ${preview.name}`"
          class="w-full h-24 object-cover"
        />
        <div class="absolute inset-0 bg-black/0 group-hover:bg-black/30 transition-colors" />
        <button
          type="button"
          class="absolute top-1 right-1 w-6 h-6 flex items-center justify-center rounded-full bg-red-500 text-white opacity-0 group-hover:opacity-100 transition-opacity focus:opacity-100"
          :aria-label="`Remove image ${index + 1}: ${preview.name}`"
          @click.stop="removeFile(index)"
        >
          <svg class="w-3.5 h-3.5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
            <path d="M6.28 5.22a.75.75 0 00-1.06 1.06L8.94 10l-3.72 3.72a.75.75 0 101.06 1.06L10 11.06l3.72 3.72a.75.75 0 101.06-1.06L11.06 10l3.72-3.72a.75.75 0 00-1.06-1.06L10 8.94 6.28 5.22z" />
          </svg>
        </button>
        <p class="text-xs text-gray-500 p-1 truncate">{{ formatFileSize(preview.size) }}</p>
      </div>
    </div>
  </div>
</template>
