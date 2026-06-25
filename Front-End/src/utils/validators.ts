import { z } from 'zod';

/**
 * Zod validation schemas for the Furniture Bid System forms.
 */

// --- Registration Form Schema ---
// Requirements 1.1: email valid format, password 8-64 chars with uppercase+lowercase+digit, displayName 3-50 chars

export const registrationSchema = z.object({
  email: z
    .string({ error: 'Email is required' })
    .min(1, 'Email is required')
    .email('Please enter a valid email address'),
  password: z
    .string({ error: 'Password is required' })
    .min(8, 'Password must be at least 8 characters')
    .max(64, 'Password must be no more than 64 characters')
    .regex(/[A-Z]/, 'Password must contain at least one uppercase letter')
    .regex(/[a-z]/, 'Password must contain at least one lowercase letter')
    .regex(/\d/, 'Password must contain at least one digit'),
  displayName: z
    .string({ error: 'Display name is required' })
    .min(3, 'Display name must be at least 3 characters')
    .max(50, 'Display name must be no more than 50 characters'),
});

export type RegistrationFormData = z.infer<typeof registrationSchema>;

// --- Login Form Schema ---
// Requirements 1.2: email and password required

export const loginSchema = z.object({
  email: z
    .string({ error: 'Email is required' })
    .min(1, 'Email is required')
    .email('Please enter a valid email address'),
  password: z
    .string({ error: 'Password is required' })
    .min(1, 'Password is required'),
});

export type LoginFormData = z.infer<typeof loginSchema>;

// --- Create Listing Form Schema ---
// Requirements 8.2: title 5-100 chars, description 20-2000 chars, startingPrice 0.01-999999.99,
// reservePrice >= startingPrice and <= 999999.99, dimensions 1-9999 per axis,
// weight 0.1-9999 (optional), auction end date 24h-30d in future

const TWENTY_FOUR_HOURS_MS = 24 * 60 * 60 * 1000;
const THIRTY_DAYS_MS = 30 * 24 * 60 * 60 * 1000;

export const createListingSchema = z.object({
  title: z
    .string({ error: 'Title is required' })
    .min(5, 'Title must be at least 5 characters')
    .max(100, 'Title must be no more than 100 characters'),
  description: z
    .string({ error: 'Description is required' })
    .min(20, 'Description must be at least 20 characters')
    .max(2000, 'Description must be no more than 2000 characters'),
  category: z.enum([
    'sofa',
    'dining-table',
    'office-chair',
    'wardrobe',
    'bed-frame',
    'coffee-table',
    'cabinet',
    'bookshelf',
  ], { error: 'Category is required' }),
  condition: z.enum([
    'new',
    'like-new',
    'good',
    'fair',
    'poor',
  ], { error: 'Condition is required' }),
  brand: z.string().optional(),
  material: z.string().optional(),
  dimensions: z.object({
    width: z
      .number({ error: 'Width must be a valid number' })
      .min(1, 'Width must be at least 1 cm')
      .max(9999, 'Width must be no more than 9999 cm'),
    height: z
      .number({ error: 'Height must be a valid number' })
      .min(1, 'Height must be at least 1 cm')
      .max(9999, 'Height must be no more than 9999 cm'),
    length: z
      .number({ error: 'Length must be a valid number' })
      .min(1, 'Length must be at least 1 cm')
      .max(9999, 'Length must be no more than 9999 cm'),
  }),
  weight: z
    .number({ error: 'Weight must be a valid number' })
    .min(0.1, 'Weight must be at least 0.1 kg')
    .max(9999, 'Weight must be no more than 9999 kg')
    .optional(),
  location: z.string().optional(),
  startingPrice: z
    .number({ error: 'Starting price must be a valid number' })
    .min(0.01, 'Starting price must be at least $0.01')
    .max(999999.99, 'Starting price must be no more than $999,999.99'),
  reservePrice: z
    .number({ error: 'Reserve price must be a valid number' })
    .max(999999.99, 'Reserve price must be no more than $999,999.99'),
  auctionEndDate: z
    .string({ error: 'Auction end date is required' })
    .refine(
      (val) => {
        const date = new Date(val);
        return !isNaN(date.getTime());
      },
      { message: 'Please enter a valid date' }
    )
    .refine(
      (val) => {
        const date = new Date(val);
        const now = new Date();
        return date.getTime() - now.getTime() >= TWENTY_FOUR_HOURS_MS;
      },
      { message: 'Auction end date must be at least 24 hours in the future' }
    )
    .refine(
      (val) => {
        const date = new Date(val);
        const now = new Date();
        return date.getTime() - now.getTime() <= THIRTY_DAYS_MS;
      },
      { message: 'Auction end date must be no more than 30 days in the future' }
    ),
}).refine(
  (data) => data.reservePrice >= data.startingPrice,
  {
    message: 'Reserve price must be greater than or equal to starting price',
    path: ['reservePrice'],
  }
);

export type CreateListingFormData = z.infer<typeof createListingSchema>;

// --- Bid Placement Schema ---
// Requirements 4.1: amount 0.01-999999999.99

export const bidPlacementSchema = z.object({
  amount: z
    .number({ error: 'Bid amount must be a valid number' })
    .min(0.01, 'Bid must be at least $0.01')
    .max(999999999.99, 'Bid must be no more than $999,999,999.99'),
});

export type BidPlacementFormData = z.infer<typeof bidPlacementSchema>;

// --- Auto-Bid Schema ---
// Requirements 5.1: maxAmount must be a valid positive number

export const autoBidSchema = z.object({
  maxAmount: z
    .number({ error: 'Maximum bid amount must be a valid number' })
    .positive('Maximum bid amount must be a positive number')
    .max(999999999.99, 'Maximum bid amount must be no more than $999,999,999.99'),
});

export type AutoBidFormData = z.infer<typeof autoBidSchema>;
