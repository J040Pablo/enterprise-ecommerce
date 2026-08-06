import { Component, OnInit, ViewChild, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormControl } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSortModule, MatSort } from '@angular/material/sort';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { CategoryService, CategoryResponse, CategoryRequest } from '../../../core/services/category.service';
import { CategoryFormDialogComponent, CategoryFormDialogData } from './category-form-dialog/category-form-dialog.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-admin-categories',
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
    MatSnackBarModule,
    MatDialogModule,
    MatTooltipModule
  ],
  templateUrl: './admin-categories.component.html',
  styleUrl: './admin-categories.component.scss'
})
export class AdminCategoriesComponent implements OnInit {
  private categoryService = inject(CategoryService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  dataSource = new MatTableDataSource<CategoryResponse>([]);
  /** Full list kept locally for client-side search */
  private allCategories: CategoryResponse[] = [];

  totalElements = signal<number>(0);
  loading = signal<boolean>(true);
  error = signal<string | null>(null);
  actionLoadingId = signal<string | null>(null);

  searchControl = new FormControl('');

  displayedColumns: string[] = ['name', 'description', 'active', 'actions'];

  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  ngOnInit(): void {
    this.loadCategories();

    this.searchControl.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe(term => this.applyFilter(term ?? ''));
  }

  loadCategories(): void {
    this.loading.set(true);
    this.error.set(null);

    this.categoryService.getCategories(0, 200).subscribe({
      next: (page) => {
        this.allCategories = page.content || [];
        this.applyFilter(this.searchControl.value ?? '');
        this.totalElements.set(page.totalElements || 0);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Não foi possível carregar as categorias.');
        this.loading.set(false);
      }
    });
  }

  private applyFilter(term: string): void {
    const lower = term.trim().toLowerCase();
    const filtered = lower
      ? this.allCategories.filter(c =>
          c.name.toLowerCase().includes(lower) ||
          (c.description ?? '').toLowerCase().includes(lower))
      : [...this.allCategories];

    this.dataSource.data = filtered;

    if (this.sort) {
      this.dataSource.sort = this.sort;
    }
    if (this.paginator) {
      this.dataSource.paginator = this.paginator;
      this.paginator.firstPage();
    }
  }

  openCreateDialog(): void {
    const data: CategoryFormDialogData = { isEdit: false };
    this.dialog.open(CategoryFormDialogComponent, { width: '480px', data })
      .afterClosed()
      .subscribe((result: CategoryRequest | null) => {
        if (!result) return;
        this.loading.set(true);
        this.categoryService.createCategory(result).subscribe({
          next: () => {
            this.snackBar.open('Categoria criada com sucesso.', 'OK', { duration: 3000 });
            this.loadCategories();
          },
          error: (err) => {
            const msg = err?.error?.message ?? 'Erro ao criar categoria.';
            this.snackBar.open(msg, 'Fechar', { duration: 4000 });
            this.loading.set(false);
          }
        });
      });
  }

  openEditDialog(category: CategoryResponse): void {
    const data: CategoryFormDialogData = { isEdit: true, category };
    this.dialog.open(CategoryFormDialogComponent, { width: '480px', data })
      .afterClosed()
      .subscribe((result: CategoryRequest | null) => {
        if (!result) return;
        this.actionLoadingId.set(category.id);
        this.categoryService.updateCategory(category.id, result).subscribe({
          next: () => {
            this.snackBar.open('Categoria atualizada com sucesso.', 'OK', { duration: 3000 });
            this.actionLoadingId.set(null);
            this.loadCategories();
          },
          error: (err) => {
            const msg = err?.error?.message ?? 'Erro ao atualizar categoria.';
            this.snackBar.open(msg, 'Fechar', { duration: 4000 });
            this.actionLoadingId.set(null);
          }
        });
      });
  }

  confirmDelete(category: CategoryResponse): void {
    this.dialog.open(ConfirmDialogComponent, {
      width: '420px',
      data: {
        title: 'Excluir Categoria',
        message: `Tem certeza que deseja excluir a categoria "${category.name}"? Esta ação não pode ser desfeita.`,
        confirmText: 'Excluir',
        cancelText: 'Cancelar',
        confirmColor: 'warn'
      }
    }).afterClosed().subscribe((confirmed: boolean) => {
      if (!confirmed) return;
      this.actionLoadingId.set(category.id);
      this.categoryService.deleteCategory(category.id).subscribe({
        next: () => {
          this.snackBar.open('Categoria excluída com sucesso.', 'OK', { duration: 3000 });
          this.actionLoadingId.set(null);
          this.loadCategories();
        },
        error: (err) => {
          const msg = err?.error?.message ?? 'Erro ao excluir categoria.';
          this.snackBar.open(msg, 'Fechar', { duration: 4000 });
          this.actionLoadingId.set(null);
        }
      });
    });
  }

  clearSearch(): void {
    this.searchControl.setValue('');
  }
}
