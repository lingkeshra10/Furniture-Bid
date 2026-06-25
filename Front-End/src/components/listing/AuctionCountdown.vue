<script setup lang="ts">
import { useCountdown } from '@/composables/useCountdown'

const props = defineProps<{
  endDate: string
}>()

const { days, hours, minutes, seconds, isEnded } = useCountdown(props.endDate)
</script>

<template>
  <div v-if="isEnded" class="text-center">
    <span class="text-red-600 font-semibold text-lg">Auction Ended</span>
  </div>

  <div v-else class="flex items-center justify-center gap-2">
    <div class="flex flex-col items-center bg-background rounded-lg px-3 py-2 min-w-[52px]">
      <span class="text-xl font-bold text-primary">{{ days }}</span>
      <span class="text-xs text-gray-500 uppercase">Days</span>
    </div>

    <span class="text-xl font-bold text-gray-400">:</span>

    <div class="flex flex-col items-center bg-background rounded-lg px-3 py-2 min-w-[52px]">
      <span class="text-xl font-bold text-primary">{{ hours }}</span>
      <span class="text-xs text-gray-500 uppercase">Hrs</span>
    </div>

    <span class="text-xl font-bold text-gray-400">:</span>

    <div class="flex flex-col items-center bg-background rounded-lg px-3 py-2 min-w-[52px]">
      <span class="text-xl font-bold text-primary">{{ minutes }}</span>
      <span class="text-xs text-gray-500 uppercase">Min</span>
    </div>

    <span class="text-xl font-bold text-gray-400">:</span>

    <div class="flex flex-col items-center bg-background rounded-lg px-3 py-2 min-w-[52px]">
      <span class="text-xl font-bold text-primary">{{ seconds }}</span>
      <span class="text-xs text-gray-500 uppercase">Sec</span>
    </div>
  </div>
</template>
