import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { InventoryService } from '../../../inventory/services/inventory.service';
import { ProductCard } from '../product-card/product-card';
import { StockBadge } from '../../../inventory/components/stock-badge/stock-badge';
import { InventoryForm } from '../../../inventory/components/inventory-form/inventory-form';
import { Product } from '../../../../models/product.model';
import { Inventory, CreateInventoryRequest } from '../../../../models/inventory.model';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [ProductCard, StockBadge, InventoryForm],
  templateUrl: './product-detail.html',
})
export class ProductDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly productService = inject(ProductService);
  private readonly inventoryService = inject(InventoryService);

  product = signal<Product | null>(null);
  inventory = signal<Inventory | null>(null);
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
        this.loadInventory(id);
      },
      error: () => {
        this.error.set('Produto não encontrado');
        this.loading.set(false);
      },
    });
  }

  private loadInventory(productId: string): void {
    this.inventoryService.getByProductId(productId).subscribe({
      next: (inventory) => this.inventory.set(inventory),
      error: () => this.inventory.set(null), // 404 = ainda não tem estoque, estado esperado
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

  onCreateInventory(request: CreateInventoryRequest): void {
    this.inventoryService.create(request).subscribe((inventory) => {
      this.inventory.set(inventory);
    });
  }

  goToStockAdjust(): void {
    const inv = this.inventory();
    if (inv) this.router.navigate(['/inventory', inv.id]);
  }
}
