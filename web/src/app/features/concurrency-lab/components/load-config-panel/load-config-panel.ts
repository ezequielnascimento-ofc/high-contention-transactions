import { Component, output } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { LoadRunConfig } from '../../services/load-runner.service';

@Component({
  selector: 'app-load-config-panel',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './load-config-panel.html',
})
export class LoadConfigPanel {
  runRequested = output<LoadRunConfig>();

  private readonly fb = new FormBuilder();

  form = this.fb.group({
    inventoryId: ['', Validators.required],
    requestCount: [100, [Validators.required, Validators.min(1), Validators.max(10000)]],
    decreaseAmount: [1, [Validators.required, Validators.min(1)]],
  });

  presets = [10, 100, 1000, 10000];

  applyPreset(count: number): void {
    this.form.patchValue({ requestCount: count });
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    const value = this.form.getRawValue();
    this.runRequested.emit({
      inventoryId: value.inventoryId!,
      requestCount: value.requestCount!,
      decreaseAmount: value.decreaseAmount!,
    });
  }
}
