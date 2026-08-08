import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormControl, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatRadioModule } from '@angular/material/radio';
import { Inject } from '@angular/core';
import { ProductService } from '../../../core/services/product.service';
import { InventoryService } from '../../../core/services/inventory.service';
import { Product } from '../../../core/models/product.model';

export interface StockAdjustDialogData {
  product: Product;
}

@Component({
  selector: 'app-stock-adjust-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatRadioModule,
    MatProgressSpinnerModule
  ],
  template: `
    <h2 mat-dialog-title class="dialog-header">
      <mat-icon color="primary">tune</mat-icon>
      <span>Ajustar Estoque</span>
    </h2>

    <mat-dialog-content>
      <p class="product-name">{{ data.product.name }}</p>
      <p class="current-stock">Estoque atual: <strong>{{ data.product.stockQuantity }}</strong></p>

      <form [formGroup]="form" class="form-container" id="stockAdjustForm" (ngSubmit)="onSubmit()">
        <mat-radio-group formControlName="mode" class="mode-group">
          <mat-radio-button value="set">Definir quantidade</mat-radio-button>
          <mat-radio-button value="increase">Aumentar</mat-radio-button>
          <mat-radio-button value="decrease">Diminuir</mat-radio-button>
        </mat-radio-group>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>{{ quantityLabel }}</mat-label>
          <input matInput type="number" formControlName="quantity" min="0" />
          <mat-error *ngIf="form.get('quantity')?.hasError('required')">Obrigatório.</mat-error>
          <mat-error *ngIf="form.get('quantity')?.hasError('min')">Valor inválido.</mat-error>
        </mat-form-field>
      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button type="button" (click)="dialogRef.close(null)">Cancelar</button>
      <button
        mat-flat-button
        color="primary"
        type="submit"
        form="stockAdjustForm"
        [disabled]="form.invalid || submitting()"
      >
        <mat-spinner diameter="18" *ngIf="submitting()"></mat-spinner>
        <span *ngIf="!submitting()">Salvar</span>
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .dialog-header {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-weight: 700;
    }
    .product-name {
      margin: 0 0 0.25rem;
      font-weight: 600;
      color: #0f172a;
    }
    .current-stock {
      margin: 0 0 1rem;
      color: #64748b;
    }
    .form-container {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }
    .mode-group {
      display: flex;
      flex-direction: column;
      gap: 0.35rem;
      margin-bottom: 0.5rem;
    }
    .full-width { width: 100%; }
  `]
})
export class StockAdjustDialogComponent implements OnInit {
  private fb = inject(FormBuilder);
  dialogRef = inject(MatDialogRef<StockAdjustDialogComponent>);
  private inventoryService = inject(InventoryService);

  form!: FormGroup;
  submitting = signal(false);

  constructor(@Inject(MAT_DIALOG_DATA) public data: StockAdjustDialogData) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      mode: ['set', Validators.required],
      quantity: [this.data.product.stockQuantity ?? 0, [Validators.required, Validators.min(0)]]
    });

    this.form.get('mode')?.valueChanges.subscribe(mode => {
      const qtyCtrl = this.form.get('quantity');
      if (mode === 'set') {
        qtyCtrl?.setValidators([Validators.required, Validators.min(0)]);
        qtyCtrl?.setValue(this.data.product.stockQuantity ?? 0);
      } else {
        qtyCtrl?.setValidators([Validators.required, Validators.min(1)]);
        qtyCtrl?.setValue(1);
      }
      qtyCtrl?.updateValueAndValidity();
    });
  }

  get quantityLabel(): string {
    const mode = this.form?.get('mode')?.value;
    if (mode === 'increase') return 'Unidades a adicionar';
    if (mode === 'decrease') return 'Unidades a remover';
    return 'Nova quantidade';
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { mode, quantity } = this.form.value;
    const productId = this.data.product.id;
    this.submitting.set(true);

    const request$ =
      mode === 'increase'
        ? this.inventoryService.increaseStock(productId, quantity)
        : mode === 'decrease'
          ? this.inventoryService.decreaseStock(productId, quantity)
          : this.inventoryService.setStock(productId, quantity);

    request$.subscribe({
      next: (response) => {
        this.submitting.set(false);
        this.dialogRef.close(response);
      },
      error: (err) => {
        this.submitting.set(false);
        this.dialogRef.close({ error: err });
      }
    });
  }
}

@Component({
  selector: 'app-admin-inventory',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatInputModule,
    MatFormFieldModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDialogModule
  ],
  templateUrl: './admin-inventory.component.html',
  styleUrl: './admin-inventory.component.scss'
})
export class AdminInventoryComponent implements OnInit {
  private productService = inject(ProductService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  dataSource = new MatTableDataSource<Product>([]);
  totalElements = signal(0);
  pageSize = signal(10);
  pageIndex = signal(0);
  loading = signal(true);
  error = signal<string | null>(null);
  actionLoadingId = signal<string | null>(null);

  searchControl = new FormControl('');
  displayedColumns: string[] = ['image', 'name', 'categoryName', 'stock', 'actions'];
  readonly brokenImages = new Set<string>();

  ngOnInit(): void {
    this.loadProducts();

    this.searchControl.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe(() => {
        this.pageIndex.set(0);
        this.loadProducts();
      });
  }

  loadProducts(): void {
    this.loading.set(true);
    this.error.set(null);

    const filterName = this.searchControl.value || undefined;
    this.productService
      .getProducts({ name: filterName }, this.pageIndex(), this.pageSize())
      .subscribe({
        next: (page) => {
          // New array reference so MatTable refreshes after stock changes
          this.dataSource.data = [...(page.content || [])];
          this.totalElements.set(page.totalElements || 0);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('Não foi possível carregar o estoque dos produtos.');
          this.loading.set(false);
        }
      });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadProducts();
  }

  onImageError(productId: string): void {
    this.brokenImages.add(productId);
  }

  openAdjustDialog(product: Product): void {
    this.actionLoadingId.set(product.id);
    const dialogRef = this.dialog.open(StockAdjustDialogComponent, {
      width: '440px',
      data: { product }
    });

    dialogRef.afterClosed().subscribe((result) => {
      this.actionLoadingId.set(null);
      if (!result) return;

      if (result.error) {
        const err = result.error;
        const status = err?.status;
        let msg = err?.error?.message ?? 'Erro ao atualizar estoque.';
        if (status === 403) msg = 'Acesso negado. Apenas administradores podem alterar o estoque.';
        if (status === 401) msg = 'Sessão expirada. Faça login novamente.';
        if (status === 404) msg = 'Produto ou inventário não encontrado.';
        if (status === 405) msg = 'Operação não suportada pelo servidor. Reinicie o backend com a versão atual.';
        if (status === 409) msg = err?.error?.message ?? 'Estoque insuficiente para diminuir.';
        this.snackBar.open(msg, 'Fechar', { duration: 5000 });
        return;
      }

      const updatedQuantity = result.quantity as number;
      // Optimistic UI from API response (backend is source of truth)
      this.dataSource.data = this.dataSource.data.map(p =>
        p.id === product.id ? { ...p, stockQuantity: updatedQuantity } : p
      );

      this.snackBar.open(
        `Estoque de "${product.name}" atualizado para ${updatedQuantity}.`,
        'OK',
        { duration: 3000 }
      );
      // Reload from backend to stay consistent with Inventory
      this.loadProducts();
    });
  }
}
