import { Component, input, computed } from '@angular/core';
import { LoadRunResult } from '../../services/load-runner.service';

@Component({
  selector: 'app-request-outcome-chart',
  standalone: true,
  templateUrl: './request-outcome-chart.html',
})
export class RequestOutcomeChart {
  result = input<LoadRunResult | null>(null);
  running = input(false);

  succeededPct = computed(() => this.pct((r) => r.succeeded));
  insufficientPct = computed(() => this.pct((r) => r.insufficientStock));
  otherPct = computed(() => this.pct((r) => r.otherErrors));

  private pct(pick: (r: LoadRunResult) => number): number {
    const r = this.result();
    if (!r || r.total === 0) return 0;
    return Math.round((pick(r) / r.total) * 100);
  }
}
