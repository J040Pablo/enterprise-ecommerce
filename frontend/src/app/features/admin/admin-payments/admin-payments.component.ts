import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormControl } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { HttpErrorResponse } from '@angular/common/http';
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
import { OrderService } from '../../../core/services/order.service';
import { PaymentService } from '../../../core/services/payment.service';
import { PaymentStatus } from '../../../core/models/order.model';
import { CopyUuidComponent } from '../../../shared/components/copy-uuid/copy-uuid.component';

export interface PaymentRow {
  id: string;
  orderId: string;
  status: PaymentStatus;
  totalAmount: number;
}

@Component({
  selector: 'app-admin-payments',
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
  templateUrl: './admin-payments.component.html',
  styleUrl: './admin-payments.component.scss'
})
export class AdminPaymentsComponent implements OnInit {
  private orderService = inject(OrderService);
  private paymentService = inject(PaymentService);
  private snackBar = inject(MatSnackBar);

  /** Full unfiltered list */
  private allPayments: PaymentRow[] = [];

  payments = signal<PaymentRow[]>([]);
  displayedPayments = signal<PaymentRow[]>([]);
  loading = signal<boolean>(true);
  error = signal<string | null>(null);
  actionLoadingId = signal<string | null>(null);

  pageSize = signal<number>(10);
  pageIndex = signal<number>(0);

  uuidSearch = new FormControl('');

  displayedColumns: string[] = ['id', 'orderId', 'status', 'totalAmount', 'actions'];

  ngOnInit(): void {
    this.loadPayments();

    this.uuidSearch.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe(() => {
        this.pageIndex.set(0);
        this.applyFilterAndPage();
      });
  }

  loadPayments(): void {
    this.loading.set(true);
    this.error.set(null);

    this.orderService.getAllOrders().subscribe({
      next: (orders) => {
        this.allPayments = (orders || [])
          .filter(o => o.payment != null)
          .map(o => ({
            id: o.payment!.id,
            orderId: o.id,
            status: o.payment!.status,
            totalAmount: o.totalAmount
          }));

        this.applyFilterAndPage();
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        console.error('[AdminPayments] HTTP', err?.status);
        this.error.set('Não foi possível carregar os registros de pagamento.');
        this.loading.set(false);
      }
    });
  }

  private applyFilterAndPage(): void {
    const term = (this.uuidSearch.value ?? '').trim().toLowerCase();
    const filtered = term
      ? this.allPayments.filter(p =>
          p.id.toLowerCase().includes(term) || p.orderId.toLowerCase().includes(term))
      : [...this.allPayments];

    this.payments.set(filtered);
    this.updatePageData();
  }

  updatePageData(): void {
    const start = this.pageIndex() * this.pageSize();
    const end = start + this.pageSize();
    this.displayedPayments.set(this.payments().slice(start, end));
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.updatePageData();
  }

  clearUuidSearch(): void {
    this.uuidSearch.setValue('');
  }

  approve(payment: PaymentRow): void {
    this.actionLoadingId.set(payment.id);
    this.paymentService.approvePayment(payment.id).subscribe({
      next: (res) => {
        this.snackBar.open('Pagamento aprovado com sucesso.', 'OK', { duration: 3000 });
        this.updatePaymentStatus(payment.id, res.status);
        this.actionLoadingId.set(null);
      },
      error: (err: HttpErrorResponse) => {
        console.error('[AdminPayments] HTTP', err?.status);
        this.snackBar.open(this.resolvePaymentError(err), 'Fechar', { duration: 5000 });
        this.actionLoadingId.set(null);
      }
    });
  }

  reject(payment: PaymentRow): void {
    this.actionLoadingId.set(payment.id);
    this.paymentService.rejectPayment(payment.id).subscribe({
      next: (res) => {
        this.snackBar.open('Pagamento rejeitado com sucesso.', 'OK', { duration: 3000 });
        this.updatePaymentStatus(payment.id, res.status);
        this.actionLoadingId.set(null);
      },
      error: (err: HttpErrorResponse) => {
        console.error('[AdminPayments] HTTP', err?.status);
        this.snackBar.open(this.resolvePaymentError(err), 'Fechar', { duration: 5000 });
        this.actionLoadingId.set(null);
      }
    });
  }

  refund(payment: PaymentRow): void {
    this.actionLoadingId.set(payment.id);
    this.paymentService.refundPayment(payment.id).subscribe({
      next: (res) => {
        this.snackBar.open('Pagamento estornado com sucesso.', 'OK', { duration: 3000 });
        this.updatePaymentStatus(payment.id, res.status);
        this.actionLoadingId.set(null);
      },
      error: (err: HttpErrorResponse) => {
        console.error('[AdminPayments] HTTP', err?.status);
        this.snackBar.open(this.resolvePaymentError(err), 'Fechar', { duration: 5000 });
        this.actionLoadingId.set(null);
      }
    });
  }

  private resolvePaymentError(err: HttpErrorResponse): string {
    switch (err.status) {
      case 400:
        return err.error?.message ?? 'Transição de estado inválida.';
      case 403:
        return 'Acesso não autorizado para esta operação.';
      case 404:
        return 'Pagamento não encontrado.';
      case 409:
        return 'Conflito: esta operação já foi processada.';
      default:
        return 'Ocorreu um erro inesperado. Tente novamente.';
    }
  }

  private updatePaymentStatus(paymentId: string, newStatus: PaymentStatus): void {
    this.allPayments = this.allPayments.map(p =>
      p.id === paymentId ? { ...p, status: newStatus } : p);
    this.applyFilterAndPage();
  }

  getStatusClass(status: PaymentStatus): string {
    switch (status) {
      case 'PENDING':  return 'status-pending';
      case 'APPROVED': return 'status-approved';
      case 'REJECTED': return 'status-rejected';
      case 'REFUNDED': return 'status-refunded';
      default:         return '';
    }
  }

  getStatusIcon(status: PaymentStatus): string {
    switch (status) {
      case 'PENDING':  return 'hourglass_empty';
      case 'APPROVED': return 'check_circle';
      case 'REJECTED': return 'cancel';
      case 'REFUNDED': return 'history';
      default:         return 'help';
    }
  }
}
