import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormControl } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ShippingService } from '../../../core/services/shipping.service';
import { OrderService } from '../../../core/services/order.service';
import { ShippingResponse } from '../../../core/models/shipping.model';
import { ShippingStatus } from '../../../core/models/order.model';
import { CopyUuidComponent } from '../../../shared/components/copy-uuid/copy-uuid.component';

@Component({
  selector: 'app-admin-shipping',
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
    MatProgressBarModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatFormFieldModule,
    MatInputModule,
    CopyUuidComponent
  ],
  templateUrl: './admin-shipping.component.html',
  styleUrl: './admin-shipping.component.scss'
})
export class AdminShippingComponent implements OnInit {
  private shippingService = inject(ShippingService);
  private orderService = inject(OrderService);
  private snackBar = inject(MatSnackBar);

  /** Full unfiltered list */
  private allShippings: ShippingResponse[] = [];

  shippings = signal<ShippingResponse[]>([]);
  displayedShippings = signal<ShippingResponse[]>([]);
  loading = signal<boolean>(true);
  error = signal<string | null>(null);
  actionLoadingId = signal<string | null>(null);

  pageSize = signal<number>(10);
  pageIndex = signal<number>(0);

  uuidSearch = new FormControl('');

  displayedColumns: string[] = [
    'id',
    'orderId',
    'trackingCode',
    'carrier',
    'status',
    'actions'
  ];

  ngOnInit(): void {
    this.loadShippings();

    this.uuidSearch.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe(() => {
        this.pageIndex.set(0);
        this.applyFilterAndPage();
      });
  }

  loadShippings(): void {
    this.loading.set(true);
    this.error.set(null);

    this.shippingService.getAllShippings().subscribe({
      next: (data) => {
        if (data && data.length > 0) {
          this.allShippings = data;
          this.applyFilterAndPage();
          this.loading.set(false);
        } else {
          this.loadFromOrders();
        }
      },
      error: () => {
        this.loadFromOrders();
      }
    });
  }

  private loadFromOrders(): void {
    this.orderService.getAllOrders().subscribe({
      next: (orders) => {
        this.allShippings = (orders || [])
          .filter(o => o.shipping != null)
          .map(o => ({
            id: o.shipping!.id,
            orderId: o.id,
            trackingCode: o.shipping!.trackingCode || 'N/A',
            carrier: 'Transportadora',
            status: o.shipping!.status
          }));

        this.applyFilterAndPage();
        this.loading.set(false);
      },
      error: (err) => {
        console.error('[AdminShipping] HTTP', err?.status);
        this.error.set('Não foi possível carregar as informações de envio.');
        this.loading.set(false);
      }
    });
  }

  private applyFilterAndPage(): void {
    const term = (this.uuidSearch.value ?? '').trim().toLowerCase();
    const filtered = term
      ? this.allShippings.filter(s =>
          s.id.toLowerCase().includes(term) || s.orderId.toLowerCase().includes(term))
      : [...this.allShippings];

    this.shippings.set(filtered);
    this.updatePageData();
  }

  updatePageData(): void {
    const start = this.pageIndex() * this.pageSize();
    const end = start + this.pageSize();
    this.displayedShippings.set(this.shippings().slice(start, end));
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.updatePageData();
  }

  clearUuidSearch(): void {
    this.uuidSearch.setValue('');
  }

  markShipped(shipping: ShippingResponse): void {
    this.actionLoadingId.set(shipping.id);
    this.shippingService.markAsShipped(shipping.id).subscribe({
      next: (res) => {
        this.snackBar.open('Status atualizado para ENVIADO!', 'OK', { duration: 3000 });
        this.updateStatus(shipping.id, res.status);
        this.actionLoadingId.set(null);
      },
      error: (err) => {
        console.error('[AdminShipping] HTTP', err?.status);
        this.snackBar.open('Erro na transição de status para ENVIADO.', 'Fechar', { duration: 4000 });
        this.actionLoadingId.set(null);
      }
    });
  }

  markOutForDelivery(shipping: ShippingResponse): void {
    this.actionLoadingId.set(shipping.id);
    this.shippingService.markAsOutForDelivery(shipping.id).subscribe({
      next: (res) => {
        this.snackBar.open('Status: SAIU PARA ENTREGA!', 'OK', { duration: 3000 });
        this.updateStatus(shipping.id, res.status);
        this.actionLoadingId.set(null);
      },
      error: (err) => {
        console.error('[AdminShipping] HTTP', err?.status);
        this.snackBar.open('Erro na transição de status.', 'Fechar', { duration: 4000 });
        this.actionLoadingId.set(null);
      }
    });
  }

  markDelivered(shipping: ShippingResponse): void {
    this.actionLoadingId.set(shipping.id);
    this.shippingService.markAsDelivered(shipping.id).subscribe({
      next: (res) => {
        this.snackBar.open('Envio marcado como ENTREGUE!', 'OK', { duration: 3000 });
        this.updateStatus(shipping.id, res.status);
        this.actionLoadingId.set(null);
      },
      error: (err) => {
        console.error('[AdminShipping] HTTP', err?.status);
        this.snackBar.open('Erro na transição de status para ENTREGUE.', 'Fechar', { duration: 4000 });
        this.actionLoadingId.set(null);
      }
    });
  }

  private updateStatus(id: string, newStatus: ShippingStatus): void {
    this.allShippings = this.allShippings.map(s => s.id === id ? { ...s, status: newStatus } : s);
    this.applyFilterAndPage();
  }

  getStatusClass(status: ShippingStatus): string {
    switch (status) {
      case 'PROCESSING':      return 'status-processing';
      case 'SHIPPED':         return 'status-shipped';
      case 'OUT_FOR_DELIVERY':return 'status-out';
      case 'DELIVERED':       return 'status-delivered';
      case 'CANCELLED':       return 'status-cancelled';
      default:                return '';
    }
  }

  getStatusIcon(status: ShippingStatus): string {
    switch (status) {
      case 'PROCESSING':       return 'sync';
      case 'SHIPPED':          return 'local_shipping';
      case 'OUT_FOR_DELIVERY': return 'directions_car';
      case 'DELIVERED':        return 'task_alt';
      case 'CANCELLED':        return 'cancel';
      default:                 return 'help';
    }
  }
}
