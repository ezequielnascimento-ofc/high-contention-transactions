export interface Inventory {
  id: string;
  productId: string;
  quantity: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateInventoryRequest {
  productId: string;
  quantity: number;
}

export interface AdjustStockRequest {
  quantity: number;
}

export interface StockUpdateEvent {
  inventoryId: string;
  quantity: number;
  timestamp: string;
}
