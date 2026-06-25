import type { NavigationGuardWithThis } from 'vue-router'
import type { UserRole } from '@/types/auth'
import { useAuthStore } from '@/stores/auth'

/**
 * Authentication guard — redirects unauthenticated users to login for protected routes.
 *
 * Logic:
 * - If the route has `meta.public: true`, allow navigation (no auth required).
 * - Otherwise, check if the user is authenticated.
 * - If not authenticated, redirect to the login page.
 *
 * Requirements: 1.9, 19.6
 */
export const authGuard: NavigationGuardWithThis<undefined> = (to, _from, next) => {
  if (to.meta.public) {
    return next()
  }

  const { isAuthenticated } = useAuthStore()

  if (!isAuthenticated.value) {
    return next({ name: 'login' })
  }

  next()
}

/**
 * Role guard — redirects users without the required role to catalog.
 *
 * Logic:
 * - If the route has `meta.roles`, check that the current user's role is in the list.
 * - If the user's role is not in the allowed list, redirect to catalog and log a warning.
 * - If no `meta.roles` is defined, allow navigation (route only requires authentication,
 *   which is already handled by authGuard).
 *
 * Requirements: 19.2, 19.3, 19.4, 19.5
 */
export const roleGuard: NavigationGuardWithThis<undefined> = (to, _from, next) => {
  const requiredRoles = to.meta.roles as UserRole[] | undefined

  if (!requiredRoles) {
    return next()
  }

  const { userRole } = useAuthStore()

  if (!userRole.value || !requiredRoles.includes(userRole.value)) {
    console.warn(
      `[Router] Unauthorized access attempt to "${to.path}" — user role "${userRole.value}" is not in allowed roles [${requiredRoles.join(', ')}]`
    )
    return next({ name: 'catalog' })
  }

  next()
}
