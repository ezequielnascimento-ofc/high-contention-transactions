import { Component, inject, OnDestroy, computed } from '@angular/core';
import { LoadRunnerService, LoadRunConfig } from '../../services/load-runner.service';
import { StockStreamService } from '../../services/stock-stream.service';
import { LoadConfigPanel } from '../load-config-panel/load-config-panel';
import { LiveStockGauge } from '../live-stock-gauge/live-stock-gauge';
import { RequestOutcomeChart } from '../request-outcome-chart/request-outcome-chart';
import { RunSummary } from '../run-summary/run-summary';

@Component({
  selector: 'app-concurrency-lab',
  standalone: true,
  imports: [LoadConfigPanel, LiveStockGauge, RequestOutcomeChart, RunSummary],
  templateUrl: './concurrency-lab.html',
})
export class ConcurrencyLab implements OnDestroy {
  private readonly loadRunner = inject(LoadRunnerService);
  private readonly stockStream = inject(StockStreamService);

  running = this.loadRunner.running;
  result = this.loadRunner.lastResult;

  liveQuantity = computed(() => this.stockStream.lastEvent()?.quantity ?? null);
  connected = this.stockStream.connected;

  onRun(config: LoadRunConfig): void {
    this.stockStream.connect(config.inventoryId);
    this.loadRunner.run(config);
  }

  ngOnDestroy(): void {
    this.stockStream.disconnect();
  }
}
