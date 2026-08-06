import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormControl } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTooltipModule } from '@angular/material/tooltip';
import { OrderService } from '../../../core/services/order.service';
import { OrderResponse, OrderStatus } from '../../../core/models/order.model';
import { CopyUuidComponent } from '../../../shared/components/copy-uuid/copy-uuid.component';

@Component({
  selector: 'app-admin-orders',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatIconModule,
    MatButtonModule,
    MatChipsModule,
    MatMenuModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatFormFieldModule,
    MatInputModule,
    MatTooltipModule,
    CopyUuidComponent
  ],
  templateUrl: './admin-orders.component.html',
  styleUrl: './admin-orders.component.scss'
})
export class AdminOrdersComponent implements OnInit {
  private orderService = inject(OrderService);
  private snackBar = inject(MatSnackBar);

  /** Full unfiltered list */
  private allOrders: OrderResponse[] = [];

  orders = signal<OrderResponse[]>([]);
  displayedOrders = signal<OrderResponse[]>([]);
  loading = signal<boolean>(true);
  error = signal<string | null>(null);
  actionLoadingId = signal<string | null>(null);

  pageSize = signal<number>(10);
  pageIndex = signal<number>(0);

  uuidSearch = new FormControl('');

  displayedColumns: string[] = [
    'id',
    'userId',
    'createdAt',
    'status',
    'totalAmount',
    'actions'
  ];

  statusList: OrderStatus[] = [
    'PENDING',
    'CONFIRMED',
    'PROCESSING',
    'SHIPPED',
    'DELIVERED',
    'CANCELLED'
  ];

  ngOnInit(): void {
    this.loadOrders();

    this.uuidSearch.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe(() => {
        this.pageIndex.set(0);
        this.applyFilterAndPage();
      });
  }

  loadOrders(): void {
    this.loading.set(true);
    this.error.set(null);

    this.orderService.getAllOrders().subscribe({
      next: (data) => {
        this.allOrders = data || [];
        this.orders.set(this.allOrders);
        this.applyFilterAndPage();
        this.loading.set(false);
      },
      error: (err) => {
        console.error('[AdminOrders] HTTP', err?.status);
        this.error.set('Não foi possível carregar a lista de pedidos.');
        this.loading.set(false);
      }
    });
  }

  private applyFilterAndPage(): void {
    const term = (this.uuidSearch.value ?? '').trim().toLowerCase();
    const filtered = term
      ? this.allOrders.filter(o => o.id.toLowerCase().includes(term))
      : [...this.allOrders];

    this.orders.set(filtered);
    this.updatePageData();
  }

  updatePageData(): void {
    const start = this.pageIndex() * this.pageSize();
    const end = start + this.pageSize();
    this.displayedOrders.set(this.orders().slice(start, end));
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.updatePageData();
  }

  clearUuidSearch(): void {
    this.uuidSearch.setValue('');
  }

  changeStatus(order: OrderResponse, newStatus: OrderStatus): void {
    if (order.status === newStatus) return;

    this.actionLoadingId.set(order.id);
    this.orderService.updateStatus(order.id, newStatus).subscribe({
      next: (res) => {
        this.snackBar.open(`Status atualizado para ${newStatus}.`, 'OK', { duration: 3000 });
        this.updateLocalStatus(order.id, res.status);
        this.actionLoadingId.set(null);
      },
      error: (err) => {
        console.error('[AdminOrders] HTTP', err?.status);
        this.snackBar.open('Transição de status não permitida.', 'Fechar', { duration: 4000 });
        this.actionLoadingId.set(null);
      }
    });
  }

  cancelOrder(order: OrderResponse): void {
    this.actionLoadingId.set(order.id);
    this.orderService.cancelOrder(order.id).subscribe({
      next: (res) => {
        this.snackBar.open('Pedido cancelado com sucesso.', 'OK', { duration: 3000 });
        this.updateLocalStatus(order.id, res.status);
        this.actionLoadingId.set(null);
      },
      error: (err) => {
        console.error('[AdminOrders] HTTP', err?.status);
        this.snackBar.open('Não foi possível cancelar o pedido.', 'Fechar', { duration: 4000 });
        this.actionLoadingId.set(null);
      }
    });
  }

  private updateLocalStatus(id: string, newStatus: OrderStatus): void {
    this.allOrders = this.allOrders.map(o => o.id === id ? { ...o, status: newStatus } : o);
    this.applyFilterAndPage();
  }

  getStatusClass(status: OrderStatus): string {
    switch (status) {
      case 'PENDING':    return 'status-pending';
      case 'CONFIRMED':  return 'status-confirmed';
      case 'PROCESSING': return 'status-processing';
      case 'SHIPPED':    return 'status-shipped';
      case 'DELIVERED':  return 'status-delivered';
      case 'CANCELLED':  return 'status-cancelled';
      default:           return '';
    }
  }

  getStatusIcon(status: OrderStatus): string {
    switch (status) {
      case 'PENDING':    return 'hourglass_empty';
      case 'CONFIRMED':  return 'check_circle';
      case 'PROCESSING': return 'sync';
      case 'SHIPPED':    return 'local_shipping';
      case 'DELIVERED':  return 'task_alt';
      case 'CANCELLED':  return 'cancel';
      default:           return 'help';
    }
  }
}
