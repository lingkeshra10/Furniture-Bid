import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import CatalogGrid from './CatalogGrid.vue'
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

const mockListings: FurnitureListingSummary[] = [
  {
    id: '1',
    title: 'Modern Sofa',
    thumbnailUrl: 'https://example.com/sofa.jpg',
    currentBid: 200,
    timeRemaining: 3600000,
    condition: 'new',
    category: 'sofa',
  },
  {
    id: '2',
    title: 'Office Chair',
    thumbnailUrl: 'https://example.com/chair.jpg',
    currentBid: 150,
    timeRemaining: 7200000,
    condition: 'like-new',
    category: 'office-chair',
  },
  {
    id: '3',
    title: 'Bookshelf',
    thumbnailUrl: 'https://example.com/shelf.jpg',
    currentBid: 80,
    timeRemaining: 0,
    condition: 'fair',
    category: 'bookshelf',
  },
]

describe('CatalogGrid', () => {
  it('renders a ListingCard for each listing', async () => {
    const router = createTestRouter()
    await router.push('/')
    await router.isReady()

    const wrapper = mount(CatalogGrid, {
      props: { listings: mockListings },
      global: { plugins: [router] },
    })

    const links = wrapper.findAll('a')
    expect(links.length).toBe(3)
  })

  it('renders empty grid when no listings', async () => {
    const router = createTestRouter()
    await router.push('/')
    await router.isReady()

    const wrapper = mount(CatalogGrid, {
      props: { listings: [] },
      global: { plugins: [router] },
    })

    const links = wrapper.findAll('a')
    expect(links.length).toBe(0)
  })

  it('applies responsive grid classes', async () => {
    const router = createTestRouter()
    await router.push('/')
    await router.isReady()

    const wrapper = mount(CatalogGrid, {
      props: { listings: mockListings },
      global: { plugins: [router] },
    })

    const grid = wrapper.find('div')
    expect(grid.classes()).toContain('grid')
    expect(grid.classes()).toContain('grid-cols-1')
    expect(grid.classes()).toContain('sm:grid-cols-2')
    expect(grid.classes()).toContain('lg:grid-cols-3')
    expect(grid.classes()).toContain('xl:grid-cols-4')
  })
})
