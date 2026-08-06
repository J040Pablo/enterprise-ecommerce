import { Component, OnInit, ViewChild, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormControl } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, MatSort } from '@angular/material/sort';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ProductService, CreateProductPayload, UpdateProductPayload } from '../../../core/services/product.service';
import { Product } from '../../../core/models/product.model';
import { CopyUuidComponent } from '../../../shared/components/copy-uuid/copy-uuid.component';
import { ProductFormDialogComponent } from './product-form-dialog/product-form-dialog.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-admin-products',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatInputModule,
    MatFormFieldModule,
    MatIconModule,
    MatButtonModule,
    MatChipsModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDialogModule,
    CopyUuidComponent
  ],
  templateUrl: './admin-products.component.html',
  styleUrl: './admin-products.component.scss'
})
export class AdminProductsComponent implements OnInit {
  private productService = inject(ProductService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  dataSource = new MatTableDataSource<Product>([]);
  totalElements = signal<number>(0);
  pageSize = signal<number>(10);
  pageIndex = signal<number>(0);
  loading = signal<boolean>(true);
  error = signal<string | null>(null);
  actionLoadingId = signal<string | null>(null);

  searchControl = new FormControl('');
  displayedColumns: string[] = ['id', 'name', 'categoryName', 'price', 'active', 'actions'];

  @ViewChild(MatSort) sort!: MatSort;

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
      .getProducts(
        { name: filterName },
        this.pageIndex(),
        this.pageSize()
      )
      .subscribe({
        next: (page) => {
          this.dataSource.data = page.content || [];
          if (this.sort) {
            this.dataSource.sort = this.sort;
          }
          this.totalElements.set(page.totalElements || 0);
          this.loading.set(false);
        },
        error: (err) => {
          console.error('Erro ao carregar produtos:', err);
          this.error.set('Não foi possível carregar a lista de produtos.');
          this.loading.set(false);
        }
      });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadProducts();
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(ProductFormDialogComponent, {
      width: '540px',
      data: { isEdit: false }
    });

    dialogRef.afterClosed().subscribe((result: CreateProductPayload | null) => {
      if (!result) return;

      this.loading.set(true);
      this.productService.createProduct(result).subscribe({
        next: () => {
          this.snackBar.open('Produto criado com sucesso!', 'OK', { duration: 3000 });
          // Reset to page 0 on create so the new item is visible
          this.pageIndex.set(0);
          this.loadProducts();
        },
        error: (err) => {
          const msg = err?.error?.message ?? 'Erro ao criar produto. Verifique os dados.';
          this.snackBar.open(msg, 'Fechar', { duration: 4000 });
          this.loading.set(false);
        }
      });
    });
  }

  openEditDialog(product: Product): void {
    const dialogRef = this.dialog.open(ProductFormDialogComponent, {
      width: '540px',
      data: { isEdit: true, product }
    });

    dialogRef.afterClosed().subscribe((result: UpdateProductPayload | null) => {
      if (!result) return;

      this.actionLoadingId.set(product.id);
      this.productService.updateProduct(product.id, result).subscribe({
        next: () => {
          this.snackBar.open('Produto atualizado com sucesso!', 'OK', { duration: 3000 });
          this.actionLoadingId.set(null);
          // Preserve current pageIndex on edit
          this.loadProducts();
        },
        error: (err) => {
          console.error('[AdminProducts] HTTP', err?.status);
          const msg = err?.error?.message ?? 'Erro ao atualizar produto.';
          this.snackBar.open(msg, 'Fechar', { duration: 4000 });
          this.actionLoadingId.set(null);
        }
      });
    });
  }

  confirmDelete(product: Product): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '420px',
      data: {
        title: 'Excluir Produto',
        message: `Tem certeza que deseja excluir o produto "${product.name}"? Esta ação não pode ser desfeita.`,
        confirmText: 'Excluir',
        cancelText: 'Cancelar',
        confirmColor: 'warn'
      }
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (!confirmed) return;

      this.actionLoadingId.set(product.id);
      this.productService.deleteProduct(product.id).subscribe({
        next: () => {
          this.snackBar.open('Produto excluído com sucesso.', 'OK', { duration: 3000 });
          this.actionLoadingId.set(null);
          // Preserve current pageIndex on delete
          this.loadProducts();
        },
        error: (err) => {
          console.error('[AdminProducts] HTTP', err?.status);
          const msg = err?.error?.message ?? 'Erro ao excluir produto.';
          this.snackBar.open(msg, 'Fechar', { duration: 4000 });
          this.actionLoadingId.set(null);
        }
      });
    });
  }
}
