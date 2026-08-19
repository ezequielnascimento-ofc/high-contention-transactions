import { Component, output } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { CreateProductRequest } from '../../../../models/product.model';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './product-form.html',
})
export class ProductForm {
  private readonly fb = new FormBuilder();
  submitted = output<CreateProductRequest>();

  form = this.fb.group({
    name: ['', Validators.required],
    description: [''],
    price: ['', [Validators.required, Validators.min(0)]],
  });

  onSubmit(): void {
    if (this.form.invalid) return;

    const value = this.form.getRawValue();
    this.submitted.emit({
      name: value.name!,
      description: value.description || null,
      price: value.price!,
    });
    this.form.reset();
  }
}
