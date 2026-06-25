export type FurnitureCategory =
  | 'sofa'
  | 'dining-table'
  | 'office-chair'
  | 'wardrobe'
  | 'bed-frame'
  | 'coffee-table'
  | 'cabinet'
  | 'bookshelf';

export type FurnitureCondition = 'new' | 'like-new' | 'good' | 'fair' | 'poor';

export type ListingStatus = 'active' | 'ended' | 'flagged' | 'removed';

export interface Dimensions {
  width: number;   // centimeters
  height: number;  // centimeters
  length: number;  // centimeters
}

export interface FurnitureListing {
  id: string;
  title: string;
  description: string;
  category: FurnitureCategory;
  condition: FurnitureCondition;
  brand?: string;
  material?: string;
  dimensions: Dimensions;
  weight?: number;           // kilograms
  location?: string;
  images: string[];          // URL array, 1–10 images
  startingPrice: number;
  reservePrice: number;
  currentBid: number;
  bidCount: number;
  auctionEndDate: string;    // ISO 8601
  status: ListingStatus;
  sellerId: string;
  sellerDisplayName: string;
  sellerRating: number;
  createdAt: string;
}

export interface FurnitureListingSummary {
  id: string;
  title: string;
  thumbnailUrl: string;
  currentBid: number;
  timeRemaining: number;     // milliseconds
  condition: FurnitureCondition;
  category: FurnitureCategory;
}

export interface CreateListingRequest {
  title: string;
  description: string;
  category: FurnitureCategory;
  condition: FurnitureCondition;
  brand?: string;
  material?: string;
  dimensions: Dimensions;
  weight?: number;
  location?: string;
  startingPrice: number;
  reservePrice: number;
  auctionEndDate: string;
  images: File[];
}

export interface CatalogFilters {
  category?: FurnitureCategory[];
  condition?: FurnitureCondition[];
  priceMin?: number;
  priceMax?: number;
  location?: string;
}

export type CatalogSortOption =
  | 'ending-soonest'
  | 'price-low-high'
  | 'price-high-low'
  | 'newest';

export interface CatalogQuery {
  filters: CatalogFilters;
  sort: CatalogSortOption;
  page: number;
  pageSize: number;          // default 20
}
