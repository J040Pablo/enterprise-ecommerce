import { Routes } from '@angular/router';
import { AdminLayoutComponent } from './admin-layout/admin-layout.component';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    component: AdminLayoutComponent,
    children: [
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./admin-dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent)
      },
      {
        path: 'products',
        loadComponent: () =>
          import('./admin-products/admin-products.component').then(m => m.AdminProductsComponent)
      },
      {
        path: 'categories',
        loadComponent: () =>
          import('./admin-categories/admin-categories.component').then(m => m.AdminCategoriesComponent)
      },
      {
        path: 'orders',
        loadComponent: () =>
          import('./admin-orders/admin-orders.component').then(m => m.AdminOrdersComponent)
      },
      {
        path: 'payments',
        loadComponent: () =>
          import('./admin-payments/admin-payments.component').then(m => m.AdminPaymentsComponent)
      },
      {
        path: 'shipping',
        loadComponent: () =>
          import('./admin-shipping/admin-shipping.component').then(m => m.AdminShippingComponent)
      }
    ]
  }
];
