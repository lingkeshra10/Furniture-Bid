import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import CatalogFilters from './CatalogFilters.vue'

describe('CatalogFilters', () => {
  it('renders all category checkboxes', () => {
    const wrapper = mount(CatalogFilters)

    const labels = wrapper.findAll('label')
    const categoryLabels = ['Sofa', 'Dining Table', 'Office Chair', 'Wardrobe', 'Bed Frame', 'Coffee Table', 'Cabinet', 'Bookshelf']
    categoryLabels.forEach((label) => {
      expect(wrapper.text()).toContain(label)
    })
  })

  it('renders all condition checkboxes', () => {
    const wrapper = mount(CatalogFilters)

    const conditionLabels = ['New', 'Like New', 'Good', 'Fair', 'Poor']
    conditionLabels.forEach((label) => {
      expect(wrapper.text()).toContain(label)
    })
  })

  it('renders price range inputs with min 0 and max 999999', () => {
    const wrapper = mount(CatalogFilters)

    const priceMin = wrapper.find('#price-min')
    const priceMax = wrapper.find('#price-max')

    expect(priceMin.attributes('min')).toBe('0')
    expect(priceMin.attributes('max')).toBe('999999')
    expect(priceMax.attributes('min')).toBe('0')
    expect(priceMax.attributes('max')).toBe('999999')
  })

  it('renders location text field', () => {
    const wrapper = mount(CatalogFilters)

    const locationInput = wrapper.find('#filter-location')
    expect(locationInput.exists()).toBe(true)
    expect(locationInput.attributes('type')).toBe('text')
  })

  it('emits update:filters when a category is selected', async () => {
    const wrapper = mount(CatalogFilters)

    const checkboxes = wrapper.findAll('input[type="checkbox"]')
    // First checkbox is 'sofa'
    await checkboxes[0].setValue(true)

    const emitted = wrapper.emitted('update:filters')
    expect(emitted).toBeTruthy()
    expect(emitted![emitted!.length - 1][0]).toMatchObject({ category: ['sofa'] })
  })

  it('emits update:filters when condition is selected', async () => {
    const wrapper = mount(CatalogFilters)

    const checkboxes = wrapper.findAll('input[type="checkbox"]')
    // Conditions start after 8 category checkboxes, first condition is 'new'
    await checkboxes[8].setValue(true)

    const emitted = wrapper.emitted('update:filters')
    expect(emitted).toBeTruthy()
    expect(emitted![emitted!.length - 1][0]).toMatchObject({ condition: ['new'] })
  })

  it('emits update:filters with location when text is typed', async () => {
    const wrapper = mount(CatalogFilters)

    const locationInput = wrapper.find('#filter-location')
    await locationInput.setValue('New York')

    const emitted = wrapper.emitted('update:filters')
    expect(emitted).toBeTruthy()
    expect(emitted![emitted!.length - 1][0]).toMatchObject({ location: 'New York' })
  })

  it('ensures checkbox labels have minimum touch target height', () => {
    const wrapper = mount(CatalogFilters)

    // Only checkbox labels have the min-h-touch class (not sr-only labels for inputs)
    const checkboxLabels = wrapper.findAll('label').filter((l) => l.classes().includes('min-h-touch'))
    // 8 categories + 5 conditions = 13 checkbox labels
    expect(checkboxLabels.length).toBe(13)
  })
})
