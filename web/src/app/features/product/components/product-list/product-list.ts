import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { ProductForm } from '../product-form/product-form';
import { ProductCard } from '../product-card/product-card';
import { CreateProductRequest, Product } from '../../../../models/product.model';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [ProductForm, ProductCard],
  templateUrl: './product-list.html',
})
export class ProductList {
  private readonly productService = inject(ProductService);
  private readonly router = inject(Router);

  products = signal<Product[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

  constructor() {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.productService.getAll().subscribe({
      next: (products) => {
        this.products.set(products);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Erro ao carregar produtos');
        this.loading.set(false);
      },
    });
  }

  onCreate(request: CreateProductRequest): void {
    this.productService.create(request).subscribe(() => this.load());
  }

  openDetail(product: Product): void {
    this.router.navigate(['/products', product.id]);
  }
}
