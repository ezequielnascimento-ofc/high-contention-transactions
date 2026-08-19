import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  Product,
  CreateProductRequest,
  RenameProductRequest,
  ChangeProductPriceRequest,
  ChangeProductDescriptionRequest,
} from '../../../models/product.model';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/v1/products`;

  create(request: CreateProductRequest): Observable<Product> {
    return this.http.post<Product>(this.baseUrl, request);
  }

  getById(id: string): Observable<Product> {
    return this.http.get<Product>(`${this.baseUrl}/${id}`);
  }

  rename(id: string, request: RenameProductRequest): Observable<Product> {
    return this.http.patch<Product>(`${this.baseUrl}/${id}/name`, request);
  }

  changePrice(id: string, request: ChangeProductPriceRequest): Observable<Product> {
    return this.http.patch<Product>(`${this.baseUrl}/${id}/price`, request);
  }

  changeDescription(id: string, request: ChangeProductDescriptionRequest): Observable<Product> {
    return this.http.patch<Product>(`${this.baseUrl}/${id}/description`, request);
  }

  activate(id: string): Observable<Product> {
    return this.http.post<Product>(`${this.baseUrl}/${id}/activate`, {});
  }

  deactivate(id: string): Observable<Product> {
    return this.http.post<Product>(`${this.baseUrl}/${id}/deactivate`, {});
  }
}
