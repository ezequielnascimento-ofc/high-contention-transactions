import { Routes } from '@angular/router';

export const inventoryRoutes: Routes = [
  {
    path: ':id',
    loadComponent: () =>
      import('./components/stock-adjust/stock-adjust').then((m) => m.StockAdjust),
  },
];
