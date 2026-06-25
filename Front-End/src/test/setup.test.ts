import { describe, it, expect } from 'vitest'

describe('Test setup verification', () => {
  it('vitest runs with happy-dom environment', () => {
    expect(typeof document).toBe('object')
    expect(typeof window).toBe('object')
  })

  it('path alias @ resolves correctly', async () => {
    const constants = await import('@/utils/constants')
    expect(constants).toBeDefined()
  })

  it('localStorage mock is available', () => {
    localStorage.setItem('test-key', 'test-value')
    expect(localStorage.getItem('test-key')).toBe('test-value')
  })
})
