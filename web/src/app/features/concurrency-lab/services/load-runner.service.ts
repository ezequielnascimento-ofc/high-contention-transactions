import { Injectable, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { InventoryService } from '../../inventory/services/inventory.service';

export interface LoadRunConfig {
  inventoryId: string;
  requestCount: number;
  decreaseAmount: number;
}

export interface LoadRunResult {
  total: number;
  succeeded: number;
  insufficientStock: number;
  otherErrors: number;
  durationMs: number;
}

type OutcomeKind = 'success' | 'insufficient-stock' | 'other-error';

@Injectable({ providedIn: 'root' })
export class LoadRunnerService {
  private readonly inventoryService = inject(InventoryService);

  readonly running = signal(false);
  readonly lastResult = signal<LoadRunResult | null>(null);

  run(config: LoadRunConfig): void {
    if (this.running()) return;

    this.running.set(true);
    this.lastResult.set(null);

    const startedAt = performance.now();

    const requests = Array.from({ length: config.requestCount }, () =>
      this.inventoryService.decrease(config.inventoryId, { quantity: config.decreaseAmount }).pipe(
        map((): OutcomeKind => 'success'),
        catchError((error: HttpErrorResponse) => {
          const outcome: OutcomeKind = error.status === 409 ? 'insufficient-stock' : 'other-error';
          return of(outcome);
        }),
      ),
    );

    forkJoin(requests).subscribe((outcomes) => {
      const durationMs = performance.now() - startedAt;

      this.lastResult.set({
        total: outcomes.length,
        succeeded: outcomes.filter((o) => o === 'success').length,
        insufficientStock: outcomes.filter((o) => o === 'insufficient-stock').length,
        otherErrors: outcomes.filter((o) => o === 'other-error').length,
        durationMs,
      });

      this.running.set(false);
    });
  }
}
