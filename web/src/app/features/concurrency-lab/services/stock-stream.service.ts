import { Injectable, signal } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { StockUpdateEvent } from '../../../models/inventory.model';

@Injectable({ providedIn: 'root' })
export class StockStreamService {
  private eventSource: EventSource | null = null;

  readonly lastEvent = signal<StockUpdateEvent | null>(null);
  readonly connected = signal(false);

  connect(inventoryId: string): void {
    this.disconnect();

    const url = `${environment.apiUrl}/api/v1/inventories/${inventoryId}/stream`;
    this.eventSource = new EventSource(url);

    this.eventSource.onopen = () => this.connected.set(true);

    this.eventSource.addEventListener('stock-update', (event: MessageEvent) => {
      const data: StockUpdateEvent = JSON.parse(event.data);
      this.lastEvent.set(data);
    });

    this.eventSource.onerror = () => {
      this.connected.set(false);
    };
  }

  disconnect(): void {
    this.eventSource?.close();
    this.eventSource = null;
    this.connected.set(false);
    this.lastEvent.set(null);
  }
}
