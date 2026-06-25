/**
 * Formatting utilities for the Furniture Bid System
 */

/**
 * Formats a number as currency (USD).
 * @param amount - The numeric amount to format
 * @returns Formatted currency string (e.g., "$1,234.56")
 */
export function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(amount);
}

/**
 * Formats an ISO 8601 date string to a localized date/time string.
 * @param isoString - ISO 8601 date string
 * @returns Formatted date/time (e.g., "Jan 15, 2025, 3:30 PM")
 */
export function formatDateTime(isoString: string): string {
  const date = new Date(isoString);
  return new Intl.DateTimeFormat('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
    hour12: true,
  }).format(date);
}

/**
 * Formats a remaining time in milliseconds as a countdown string.
 * @param remainingMs - Time remaining in milliseconds
 * @returns Countdown string (e.g., "2d 5h 30m 15s") or "Ended" if <= 0
 */
export function formatCountdown(remainingMs: number): string {
  if (remainingMs <= 0) {
    return 'Ended';
  }

  const seconds = Math.floor(remainingMs / 1000) % 60;
  const minutes = Math.floor(remainingMs / (1000 * 60)) % 60;
  const hours = Math.floor(remainingMs / (1000 * 60 * 60)) % 24;
  const days = Math.floor(remainingMs / (1000 * 60 * 60 * 24));

  const parts: string[] = [];
  if (days > 0) parts.push(`${days}d`);
  if (hours > 0) parts.push(`${hours}h`);
  if (minutes > 0) parts.push(`${minutes}m`);
  if (seconds > 0 || parts.length === 0) parts.push(`${seconds}s`);

  return parts.join(' ');
}

/**
 * Formats an ISO 8601 date string as a relative time (e.g., "2 hours ago", "in 3 days").
 * @param isoString - ISO 8601 date string
 * @returns Relative time string
 */
export function formatRelativeTime(isoString: string): string {
  const date = new Date(isoString);
  const now = new Date();
  const diffMs = date.getTime() - now.getTime();
  const absDiffMs = Math.abs(diffMs);

  const seconds = Math.floor(absDiffMs / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);

  let value: number;
  let unit: Intl.RelativeTimeFormatUnit;

  if (days > 0) {
    value = days;
    unit = 'day';
  } else if (hours > 0) {
    value = hours;
    unit = 'hour';
  } else if (minutes > 0) {
    value = minutes;
    unit = 'minute';
  } else {
    value = seconds;
    unit = 'second';
  }

  const rtf = new Intl.RelativeTimeFormat('en', { numeric: 'auto' });
  return rtf.format(diffMs < 0 ? -value : value, unit);
}
