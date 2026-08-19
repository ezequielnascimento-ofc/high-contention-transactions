import { Component, input } from '@angular/core';

@Component({
  selector: 'app-stock-badge',
  standalone: true,
  templateUrl: './stock-badge.html',
})
export class StockBadge {
  quantity = input.required<number>();
}
