import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'products', pathMatch: 'full' },
  {
    path: 'products',
    loadChildren: () => import('./features/product/product.routes').then((m) => m.productRoutes),
  },
  {
    path: 'inventory',
    loadChildren: () =>
      import('./features/inventory/inventory.routes').then((m) => m.inventoryRoutes),
  },
  //   {
  //     path: 'concurrency-lab',
  //     loadChildren: () =>
  //       import('./features/concurrency-lab/concurrency-lab.routes').then(
  //         (m) => m.concurrencyLabRoutes,
  //       ),
  //   },
  { path: '**', redirectTo: 'products' },
];
