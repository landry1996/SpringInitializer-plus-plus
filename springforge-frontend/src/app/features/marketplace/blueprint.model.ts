export interface Blueprint {
  id: string;
  name: string;
  description: string;
  author: string;
  version: string;
  category: BlueprintCategory;
  tags: string[];
  configurationJson: string;
  downloads: number;
  rating: number;
  ratingCount: number;
  published: boolean;
  verified: boolean;
  createdAt: string;
  updatedAt: string;
}

export type BlueprintCategory =
  | 'MICROSERVICE'
  | 'REST_API'
  | 'BATCH'
  | 'MESSAGING'
  | 'FULLSTACK'
  | 'LIBRARY'
  | 'GATEWAY'
  | 'EVENT_DRIVEN';

export interface BlueprintSearchCriteria {
  query?: string;
  category?: BlueprintCategory;
  tags?: string[];
  author?: string;
  minRating?: number;
  sortBy?: string;
  sortDir?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
