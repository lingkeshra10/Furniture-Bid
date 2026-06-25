/**
 * App-wide constants for the Furniture Bid System
 */

/** Minimum amount by which a new bid must exceed the current highest bid */
export const BID_INCREMENT = 1.00;

/** Number of items loaded per page/batch */
export const PAGE_SIZE = 20;

/** Maximum number of WebSocket reconnection attempts */
export const MAX_RECONNECT_ATTEMPTS = 10;

/** Toast notification auto-dismiss duration in milliseconds */
export const TOAST_DURATION = 5000;

/** Responsive breakpoints in pixels */
export const BREAKPOINTS = {
  /** Mobile: below 768px */
  mobile: 768,
  /** Tablet: 768px to 1024px */
  tablet: 1024,
  /** Desktop: above 1024px */
  desktop: 1025,
} as const;

/** WebSocket reconnection delay boundaries in milliseconds */
export const RECONNECT_DELAY = {
  initial: 1000,
  max: 30000,
} as const;

/** API request timeout in milliseconds */
export const API_TIMEOUT = 30000;

/** Maximum number of images per listing */
export const MAX_LISTING_IMAGES = 10;

/** Maximum file size per image in bytes (5 MB) */
export const MAX_IMAGE_SIZE = 5 * 1024 * 1024;

/** Supported image MIME types */
export const SUPPORTED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp'] as const;
