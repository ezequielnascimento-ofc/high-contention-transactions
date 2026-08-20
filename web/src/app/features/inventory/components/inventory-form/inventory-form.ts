import { Component, input, output } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { CreateInventoryRequest } from '../../../../models/inventory.model';

@Component({
  selector: 'app-inventory-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './inventory-form.html',
})
export class InventoryForm {
  productId = input.required<string>();
  submitted = output<CreateInventoryRequest>();

  private readonly fb = new FormBuilder();

  form = this.fb.group({
    quantity: [0, [Validators.required, Validators.min(0)]],
  });

  onSubmit(): void {
    if (this.form.invalid) return;
    this.submitted.emit({
      productId: this.productId(),
      quantity: this.form.getRawValue().quantity!,
    });
  }
}
