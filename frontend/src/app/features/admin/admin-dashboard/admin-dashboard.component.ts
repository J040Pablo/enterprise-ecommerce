import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { switchMap, catchError } from 'rxjs/operators';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ProductService } from '../../../core/services/product.service';
import { OrderService } from '../../../core/services/order.service';
import { ShippingService } from '../../../core/services/shipping.service';
import { InventoryService } from '../../../core/services/inventory.service';

export interface DashboardMetrics {
  // Inventory / Products
  totalProducts: number;
  activeProducts: number;
  totalStockQuantity: number;

  // Orders
  totalOrders: number;
  pendingOrders: number;
  confirmedOrders: number;

  // Payments
  totalPayments: number;
  approvedPayments: number;

  // Shipments
  totalShipments: number;
  processingShipments: number;
  shippedShipments: number;
  outForDeliveryShipments: number;
  deliveredShipments: number;
  cancelledShipments: number;
}

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatDividerModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.scss'
})
export class AdminDashboardComponent implements OnInit {
  private productService = inject(ProductService);
  private orderService = inject(OrderService);
  private shippingService = inject(ShippingService);
  private inventoryService = inject(InventoryService);

  loading = signal<boolean>(true);
  error = signal<string | null>(null);
  stats = signal<DashboardMetrics>({
    totalProducts: 0,
    activeProducts: 0,
    totalStockQuantity: 0,

    totalOrders: 0,
    pendingOrders: 0,
    confirmedOrders: 0,

    totalPayments: 0,
    approvedPayments: 0,

    totalShipments: 0,
    processingShipments: 0,
    shippedShipments: 0,
    outForDeliveryShipments: 0,
    deliveredShipments: 0,
    cancelledShipments: 0
  });

  ngOnInit(): void {
    this.loadMetrics();
  }

  loadMetrics(): void {
    this.loading.set(true);
    this.error.set(null);

    forkJoin({
      productsPage: this.productService.getProducts({}, 0, 100),
      orders: this.orderService.getAllOrders().pipe(catchError(() => of([]))),
      shippings: this.shippingService.getAllShippings().pipe(catchError(() => of([])))
    })
    .pipe(
      switchMap(({ productsPage, orders, shippings }) => {
        const products = productsPage.content || [];
        const totalProducts = productsPage.totalElements || products.length;
        const activeProducts = products.filter(p => p.active === true).length;

        // If there are products, fetch inventory stock for each product
        if (products.length === 0) {
          return of({
            products,
            totalProducts,
            activeProducts,
            totalStockQuantity: 0,
            orders,
            shippings
          });
        }

        const inventoryRequests = products.map(p =>
          this.inventoryService.getInventoryByProductId(p.id)
        );

        return forkJoin(inventoryRequests).pipe(
          switchMap(inventories => {
            const totalStockQuantity = inventories.reduce(
              (acc, curr) => acc + (curr.quantity || 0),
              0
            );
            return of({
              products,
              totalProducts,
              activeProducts,
              totalStockQuantity,
              orders,
              shippings
            });
          })
        );
      })
    )
    .subscribe({
      next: ({ totalProducts, activeProducts, totalStockQuantity, orders, shippings }) => {
        const orderList = orders || [];
        const totalOrders = orderList.length;
        const pendingOrders = orderList.filter(o => o.status === 'PENDING').length;
        const confirmedOrders = orderList.filter(o => o.status === 'CONFIRMED').length;

        // Payments
        const paymentsList = orderList.filter(o => o.payment != null).map(o => o.payment!);
        const totalPayments = paymentsList.length;
        const approvedPayments = paymentsList.filter(p => p.status === 'APPROVED').length;

        // Shipments
        let shippingList = shippings || [];
        if (shippingList.length === 0) {
          shippingList = orderList.filter(o => o.shipping != null).map(o => ({
            id: o.shipping!.id,
            orderId: o.id,
            trackingCode: o.shipping!.trackingCode || '',
            carrier: 'Transportadora',
            status: o.shipping!.status
          }));
        }

        const totalShipments = shippingList.length;
        const processingShipments = shippingList.filter(s => s.status === 'PROCESSING').length;
        const shippedShipments = shippingList.filter(s => s.status === 'SHIPPED').length;
        const outForDeliveryShipments = shippingList.filter(s => s.status === 'OUT_FOR_DELIVERY').length;
        const deliveredShipments = shippingList.filter(s => s.status === 'DELIVERED').length;
        const cancelledShipments = shippingList.filter(s => s.status === 'CANCELLED').length;

        this.stats.set({
          totalProducts,
          activeProducts,
          totalStockQuantity,

          totalOrders,
          pendingOrders,
          confirmedOrders,

          totalPayments,
          approvedPayments,

          totalShipments,
          processingShipments,
          shippedShipments,
          outForDeliveryShipments,
          deliveredShipments,
          cancelledShipments
        });

        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erro ao carregar estatísticas do dashboard:', err);
        this.error.set('Não foi possível carregar as estatísticas.');
        this.loading.set(false);
      }
    });
  }
}
