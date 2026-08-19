import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { ProductForm } from '../../components/product-form/product-form';
import { CreateProductRequest } from '../../../../models/product.model';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [ProductForm],
  templateUrl: './product-list.html',
})
export class ProductList {
  private readonly productService = inject(ProductService);
  private readonly router = inject(Router);

  searchId = signal('');
  searchError = signal<string | null>(null);

  onCreate(request: CreateProductRequest): void {
    this.productService.create(request).subscribe((product) => {
      this.router.navigate(['/products', product.id]);
    });
  }

  onSearch(id: string): void {
    if (!id) return;
    this.searchError.set(null);
    this.router.navigate(['/products', id]);
  }
}
