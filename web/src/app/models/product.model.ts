export enum ProductStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
}

export interface Product {
  id: string;
  name: string;
  description: string | null;
  price: string;
  status: ProductStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateProductRequest {
  name: string;
  description?: string | null;
  price: string;
}

export interface RenameProductRequest {
  name: string;
}

export interface ChangeProductPriceRequest {
  price: string;
}

export interface ChangeProductDescriptionRequest {
  description: string | null;
}
