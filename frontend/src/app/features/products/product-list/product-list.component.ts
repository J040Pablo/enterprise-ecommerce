import { Component, OnInit, OnDestroy, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormControl } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, takeUntil } from 'rxjs/operators';

import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';

import { ProductService } from '../../../core/services/product.service';
import { CartService } from '../../../core/services/cart.service';
import { Product, ProductPage } from '../../../core/models/product.model';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatChipsModule,
    MatTooltipModule,
  ],
  templateUrl: './product-list.component.html',
  styleUrl: './product-list.component.scss',
})
export class ProductListComponent implements OnInit, OnDestroy {
  private readonly productService = inject(ProductService);
  private readonly cartService = inject(CartService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);
  private readonly destroy$ = new Subject<void>();

  readonly searchControl = new FormControl('');

  // Estado reativo com signals
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly productPage = signal<ProductPage | null>(null);

  readonly products = computed(() => this.productPage()?.content ?? []);
  readonly totalElements = computed(() => this.productPage()?.totalElements ?? 0);

  // Paginação
  pageIndex = 0;
  pageSize = 12;
  currentSearch = '';

  ngOnInit(): void {
    this.loadProducts();

    this.searchControl.valueChanges.pipe(
      debounceTime(400),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(value => {
      this.currentSearch = value ?? '';
      this.pageIndex = 0;
      this.loadProducts();
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadProducts(): void {
    this.loading.set(true);
    this.error.set(null);

    const filter = this.currentSearch ? { name: this.currentSearch } : {};

    this.productService.getProducts(filter, this.pageIndex, this.pageSize)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (page) => {
          this.productPage.set(page);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('Não foi possível carregar os produtos. Verifique a conexão.');
          this.loading.set(false);
        },
      });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadProducts();
  }

  addToCart(product: Product): void {
    this.cartService.addItem(product, 1).subscribe({
      next: () => {
        this.snackBar.open(`"${product.name}" adicionado ao carrinho`, 'Ver Carrinho', {
          duration: 3000,
          panelClass: ['snack-success'],
        }).onAction().subscribe(() => {
          this.router.navigate(['/cart']);
        });
      },
      error: (err) => {
        const msg = err?.error?.message ?? 'Não foi possível adicionar ao carrinho. Verifique o estoque.';
        this.snackBar.open(msg, 'Fechar', { duration: 5000, panelClass: ['snack-error'] });
      }
    });
  }

  viewDetail(id: string): void {
    this.router.navigate(['/products', id]);
  }

  formatPrice(value: number): string {
    return value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }
}
