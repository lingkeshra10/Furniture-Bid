import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import type { UserRole } from '@/types/auth'

declare module 'vue-router' {
  interface RouteMeta {
    /** Route is accessible without authentication */
    public?: boolean
    /** Roles allowed to access this route (requires authentication) */
    roles?: UserRole[]
  }
}

const routes: RouteRecordRaw[] = [
  // Public routes
  {
    path: '/login',
    name: 'login',
    component: () => import('@/pages/LoginPage.vue'),
    meta: { public: true },
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/pages/RegisterPage.vue'),
    meta: { public: true },
  },
  {
    path: '/forgot-password',
    name: 'forgot-password',
    component: () => import('@/pages/ForgotPasswordPage.vue'),
    meta: { public: true },
  },
  {
    path: '/catalog',
    name: 'catalog',
    component: () => import('@/pages/CatalogPage.vue'),
    meta: { public: true },
  },
  {
    path: '/listing/:id',
    name: 'listing-detail',
    component: () => import('@/pages/ListingDetailPage.vue'),
    meta: { public: true },
  },

  // Authenticated routes (all roles)
  {
    path: '/watchlist',
    name: 'watchlist',
    component: () => import('@/pages/WatchlistPage.vue'),
    meta: { roles: ['buyer', 'seller', 'admin'] },
  },
  {
    path: '/bidding-history',
    name: 'bidding-history',
    component: () => import('@/pages/BiddingHistoryPage.vue'),
    meta: { roles: ['buyer', 'seller', 'admin'] },
  },
  {
    path: '/profile',
    name: 'profile',
    component: () => import('@/pages/UserProfilePage.vue'),
    meta: { roles: ['buyer', 'seller', 'admin'] },
  },

  // Seller routes
  {
    path: '/seller/dashboard',
    name: 'seller-dashboard',
    component: () => import('@/pages/SellerDashboardPage.vue'),
    meta: { roles: ['seller', 'admin'] },
  },
  {
    path: '/seller/create-listing',
    name: 'create-listing',
    component: () => import('@/pages/CreateListingPage.vue'),
    meta: { roles: ['seller', 'admin'] },
  },

  // Admin routes
  {
    path: '/admin',
    name: 'admin-dashboard',
    component: () => import('@/pages/AdminDashboardPage.vue'),
    meta: { roles: ['admin'] },
  },

  // Catch-all — show 404 page
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/pages/NotFoundPage.vue'),
    meta: { public: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// Navigation guards (guards file will be created in task 6.2)
import { authGuard, roleGuard } from './guards'

router.beforeEach(authGuard)
router.beforeEach(roleGuard)

export default router
