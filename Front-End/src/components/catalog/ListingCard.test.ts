import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import ListingCard from './ListingCard.vue'
import type { FurnitureListingSummary } from '@/types/furniture'

function createTestRouter() {
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: { template: '<div />' } },
      { path: '/listing/:id', name: 'listing-detail', component: { template: '<div />' } },
    ],
  })
}

const mockListing: FurnitureListingSummary = {
  id: 'listing-1',
  title: 'Vintage Oak Dining Table',
  thumbnailUrl: 'https://example.com/thumb.jpg',
  currentBid: 450.0,
  timeRemaining: 86400000, // 1 day
  condition: 'good',
  category: 'dining-table',
}

describe('ListingCard', () => {
  it('renders listing title', async () => {
    const router = createTestRouter()
    await router.push('/')
    await router.isReady()

    const wrapper = mount(ListingCard, {
      props: { listing: mockListing },
      global: { plugins: [router] },
    })

    expect(wrapper.text()).toContain('Vintage Oak Dining Table')
  })

  it('renders formatted current bid as currency', async () => {
    const router = createTestRouter()
    await router.push('/')
    await router.isReady()

    const wrapper = mount(ListingCard, {
      props: { listing: mockListing },
      global: { plugins: [router] },
    })

    expect(wrapper.text()).toContain('$450.00')
  })

  it('renders time remaining countdown', async () => {
    const router = createTestRouter()
    await router.push('/')
    await router.isReady()

    const wrapper = mount(ListingCard, {
      props: { listing: mockListing },
      global: { plugins: [router] },
    })

    expect(wrapper.text()).toContain('1d')
  })

  it('renders condition badge', async () => {
    const router = createTestRouter()
    await router.push('/')
    await router.isReady()

    const wrapper = mount(ListingCard, {
      props: { listing: mockListing },
      global: { plugins: [router] },
    })

    expect(wrapper.text()).toContain('Good')
  })

  it('links to listing detail route', async () => {
    const router = createTestRouter()
    await router.push('/')
    await router.isReady()

    const wrapper = mount(ListingCard, {
      props: { listing: mockListing },
      global: { plugins: [router] },
    })

    const link = wrapper.find('a')
    expect(link.attributes('href')).toBe('/listing/listing-1')
  })

  it('shows "Ended" for expired listings', async () => {
    const router = createTestRouter()
    await router.push('/')
    await router.isReady()

    const endedListing: FurnitureListingSummary = {
      ...mockListing,
      timeRemaining: 0,
    }

    const wrapper = mount(ListingCard, {
      props: { listing: endedListing },
      global: { plugins: [router] },
    })

    expect(wrapper.text()).toContain('Ended')
  })

  it('renders thumbnail image with alt text', async () => {
    const router = createTestRouter()
    await router.push('/')
    await router.isReady()

    const wrapper = mount(ListingCard, {
      props: { listing: mockListing },
      global: { plugins: [router] },
    })

    const img = wrapper.find('img')
    expect(img.attributes('src')).toBe('https://example.com/thumb.jpg')
    expect(img.attributes('alt')).toBe('Vintage Oak Dining Table')
  })
})
