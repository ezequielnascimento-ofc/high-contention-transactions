import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { ProductCard } from '../../components/product-card/product-card';
import { Product } from '../../../../models/product.model';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [ProductCard],
  templateUrl: './product-detail.html',
})
export class ProductDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly productService = inject(ProductService);

  product = signal<Product | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);

  constructor() {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.error.set('ID não informado');
      this.loading.set(false);
      return;
    }
    this.load(id);
  }

  private load(id: string): void {
    this.loading.set(true);
    this.productService.getById(id).subscribe({
      next: (product) => {
        this.product.set(product);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Produto não encontrado');
        this.loading.set(false);
      },
    });
  }

  toggleActivation(): void {
    const current = this.product();
    if (!current) return;

    const action =
      current.status === 'ACTIVE'
        ? this.productService.deactivate(current.id)
        : this.productService.activate(current.id);

    action.subscribe((updated) => this.product.set(updated));
  }
}
