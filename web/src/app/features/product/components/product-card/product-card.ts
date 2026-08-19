import { Component, input, output } from '@angular/core';
import { Product, ProductStatus } from '../../../../models/product.model';

@Component({
  selector: 'app-product-card',
  standalone: true,
  templateUrl: './product-card.html',
})
export class ProductCard {
  product = input.required<Product>();
  activateToggled = output<void>();

  readonly ProductStatus = ProductStatus;
}
