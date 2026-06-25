import { describe, it, expect, vi, beforeEach } from 'vitest'
import { authGuard, roleGuard } from './guards'
import type { RouteLocationNormalized } from 'vue-router'

// Mock the auth store
const mockIsAuthenticated = { value: false }
const mockUserRole = { value: null as string | null }

vi.mock('@/stores/auth', () => ({
  useAuthStore: () => ({
    isAuthenticated: mockIsAuthenticated,
    userRole: mockUserRole,
  }),
}))

function createMockRoute(meta: Record<string, unknown> = {}): RouteLocationNormalized {
  return {
    path: '/test',
    name: 'test',
    meta,
    params: {},
    query: {},
    hash: '',
    fullPath: '/test',
    matched: [],
    redirectedFrom: undefined,
  } as unknown as RouteLocationNormalized
}

describe('authGuard', () => {
  beforeEach(() => {
    mockIsAuthenticated.value = false
    mockUserRole.value = null
  })

  it('allows navigation to public routes regardless of auth state', () => {
    const to = createMockRoute({ public: true })
    const from = createMockRoute()
    const next = vi.fn()

    authGuard(to, from, next)

    expect(next).toHaveBeenCalledWith()
  })

  it('redirects unauthenticated users to login for protected routes', () => {
    mockIsAuthenticated.value = false
    const to = createMockRoute({}) // no public flag = protected
    const from = createMockRoute()
    const next = vi.fn()

    authGuard(to, from, next)

    expect(next).toHaveBeenCalledWith({ name: 'login' })
  })

  it('allows authenticated users to access protected routes', () => {
    mockIsAuthenticated.value = true
    const to = createMockRoute({})
    const from = createMockRoute()
    const next = vi.fn()

    authGuard(to, from, next)

    expect(next).toHaveBeenCalledWith()
  })

  it('redirects unauthenticated users for role-protected routes', () => {
    mockIsAuthenticated.value = false
    const to = createMockRoute({ roles: ['buyer'] })
    const from = createMockRoute()
    const next = vi.fn()

    authGuard(to, from, next)

    expect(next).toHaveBeenCalledWith({ name: 'login' })
  })
})

describe('roleGuard', () => {
  beforeEach(() => {
    mockIsAuthenticated.value = true
    mockUserRole.value = null
  })

  it('allows navigation when route has no role restriction', () => {
    const to = createMockRoute({})
    const from = createMockRoute()
    const next = vi.fn()

    roleGuard(to, from, next)

    expect(next).toHaveBeenCalledWith()
  })

  it('allows navigation when user role matches required roles', () => {
    mockUserRole.value = 'buyer'
    const to = createMockRoute({ roles: ['buyer', 'seller', 'admin'] })
    const from = createMockRoute()
    const next = vi.fn()

    roleGuard(to, from, next)

    expect(next).toHaveBeenCalledWith()
  })

  it('redirects to catalog when user role is not in required roles', () => {
    mockUserRole.value = 'buyer'
    const to = createMockRoute({ roles: ['seller', 'admin'] })
    const from = createMockRoute()
    const next = vi.fn()

    roleGuard(to, from, next)

    expect(next).toHaveBeenCalledWith({ name: 'catalog' })
  })

  it('redirects to catalog when user has no role (null)', () => {
    mockUserRole.value = null
    const to = createMockRoute({ roles: ['buyer'] })
    const from = createMockRoute()
    const next = vi.fn()

    roleGuard(to, from, next)

    expect(next).toHaveBeenCalledWith({ name: 'catalog' })
  })

  it('allows admin access to admin-only routes', () => {
    mockUserRole.value = 'admin'
    const to = createMockRoute({ roles: ['admin'] })
    const from = createMockRoute()
    const next = vi.fn()

    roleGuard(to, from, next)

    expect(next).toHaveBeenCalledWith()
  })

  it('blocks buyer from seller routes', () => {
    mockUserRole.value = 'buyer'
    const to = createMockRoute({ roles: ['seller', 'admin'] })
    const from = createMockRoute()
    const next = vi.fn()

    roleGuard(to, from, next)

    expect(next).toHaveBeenCalledWith({ name: 'catalog' })
  })

  it('allows seller access to seller routes', () => {
    mockUserRole.value = 'seller'
    const to = createMockRoute({ roles: ['seller', 'admin'] })
    const from = createMockRoute()
    const next = vi.fn()

    roleGuard(to, from, next)

    expect(next).toHaveBeenCalledWith()
  })

  it('logs a warning on unauthorized access', () => {
    mockUserRole.value = 'buyer'
    const consoleSpy = vi.spyOn(console, 'warn').mockImplementation(() => {})
    const to = createMockRoute({ roles: ['admin'] })
    to.path = '/admin'
    const from = createMockRoute()
    const next = vi.fn()

    roleGuard(to, from, next)

    expect(consoleSpy).toHaveBeenCalledWith(
      expect.stringContaining('Unauthorized access attempt')
    )
    consoleSpy.mockRestore()
  })
})
