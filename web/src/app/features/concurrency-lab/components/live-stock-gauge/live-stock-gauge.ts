import { Component, input } from '@angular/core';

@Component({
  selector: 'app-live-stock-gauge',
  standalone: true,
  templateUrl: './live-stock-gauge.html',
})
export class LiveStockGauge {
  quantity = input<number | null>(null);
  connected = input(false);
}
