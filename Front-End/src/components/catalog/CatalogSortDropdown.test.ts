import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import CatalogSortDropdown from './CatalogSortDropdown.vue'

describe('CatalogSortDropdown', () => {
  it('renders all sort options', () => {
    const wrapper = mount(CatalogSortDropdown)

    const options = wrapper.findAll('option')
    expect(options.length).toBe(4)
    expect(options[0].text()).toBe('Ending Soonest')
    expect(options[1].text()).toBe('Price: Low to High')
    expect(options[2].text()).toBe('Price: High to Low')
    expect(options[3].text()).toBe('Newest')
  })

  it('defaults to ending-soonest', () => {
    const wrapper = mount(CatalogSortDropdown)

    const select = wrapper.find('select')
    expect((select.element as HTMLSelectElement).value).toBe('ending-soonest')
  })

  it('emits update:sort when selection changes', async () => {
    const wrapper = mount(CatalogSortDropdown)

    const select = wrapper.find('select')
    await select.setValue('price-low-high')

    const emitted = wrapper.emitted('update:sort')
    expect(emitted).toBeTruthy()
    expect(emitted![0][0]).toBe('price-low-high')
  })

  it('uses provided modelValue as initial selection', () => {
    const wrapper = mount(CatalogSortDropdown, {
      props: { modelValue: 'newest' },
    })

    const select = wrapper.find('select')
    expect((select.element as HTMLSelectElement).value).toBe('newest')
  })

  it('has accessible label for the select', () => {
    const wrapper = mount(CatalogSortDropdown)

    const label = wrapper.find('label')
    expect(label.attributes('for')).toBe('sort-select')
    expect(label.text()).toBe('Sort by')
  })

  it('select has minimum touch target height', () => {
    const wrapper = mount(CatalogSortDropdown)

    const select = wrapper.find('select')
    expect(select.classes()).toContain('min-h-touch')
  })
})
