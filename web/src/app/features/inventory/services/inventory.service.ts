import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  Inventory,
  CreateInventoryRequest,
  AdjustStockRequest,
} from '../../../models/inventory.model';

@Injectable({ providedIn: 'root' })
export class InventoryService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/v1/inventories`;

  create(request: CreateInventoryRequest): Observable<Inventory> {
    return this.http.post<Inventory>(this.baseUrl, request);
  }

  getById(id: string): Observable<Inventory> {
    return this.http.get<Inventory>(`${this.baseUrl}/${id}`);
  }

  increase(id: string, request: AdjustStockRequest): Observable<Inventory> {
    return this.http.post<Inventory>(`${this.baseUrl}/${id}/increase`, request);
  }

  decrease(id: string, request: AdjustStockRequest): Observable<Inventory> {
    return this.http.post<Inventory>(`${this.baseUrl}/${id}/decrease`, request);
  }

  getByProductId(productId: string): Observable<Inventory> {
    const params = new HttpParams().set('productId', productId);
    return this.http.get<Inventory>(this.baseUrl, { params });
  }
}
