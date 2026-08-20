import { Component, input, computed } from '@angular/core';
import { LoadRunResult } from '../../services/load-runner.service';

@Component({
  selector: 'app-run-summary',
  standalone: true,
  templateUrl: './run-summary.html',
})
export class RunSummary {
  result = input<LoadRunResult | null>(null);

  throughput = computed(() => {
    const r = this.result();
    if (!r || r.durationMs === 0) return 0;
    return Math.round((r.total / r.durationMs) * 1000);
  });
}
