import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { InventoryService } from '../../services/inventory.service';
import { StockBadge } from '../../components/stock-badge/stock-badge';
import { Inventory } from '../../../../models/inventory.model';

@Component({
  selector: 'app-stock-adjust',
  standalone: true,
  imports: [ReactiveFormsModule, StockBadge],
  templateUrl: './stock-adjust.html',
})
export class StockAdjust {
  private readonly route = inject(ActivatedRoute);
  private readonly inventoryService = inject(InventoryService);
  private readonly fb = new FormBuilder();

  inventory = signal<Inventory | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);
  actionError = signal<string | null>(null);

  form = this.fb.group({
    quantity: [1, [Validators.required, Validators.min(1)]],
  });

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
    this.inventoryService.getById(id).subscribe({
      next: (inventory) => {
        this.inventory.set(inventory);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Inventário não encontrado');
        this.loading.set(false);
      },
    });
  }

  increase(): void {
    this.adjust((id, qty) => this.inventoryService.increase(id, { quantity: qty }));
  }

  decrease(): void {
    this.adjust((id, qty) => this.inventoryService.decrease(id, { quantity: qty }));
  }

  private adjust(
    action: (id: string, qty: number) => ReturnType<InventoryService['increase']>,
  ): void {
    const current = this.inventory();
    if (!current || this.form.invalid) return;

    this.actionError.set(null);
    const quantity = this.form.getRawValue().quantity!;

    action(current.id, quantity).subscribe({
      next: (updated) => {
        this.inventory.set(updated);
        this.form.reset({ quantity: 1 });
      },
      error: (err) => {
        this.actionError.set(
          err.status === 409
            ? 'Estoque insuficiente para essa operação.'
            : 'Erro ao ajustar estoque.',
        );
      },
    });
  }
}
