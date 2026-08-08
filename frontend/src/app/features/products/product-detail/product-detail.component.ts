import { Component, OnInit, OnDestroy, inject, signal, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';

import { ProductService } from '../../../core/services/product.service';
import { CartService } from '../../../core/services/cart.service';
import { Product } from '../../../core/models/product.model';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDividerModule,
  ],
  templateUrl: './product-detail.component.html',
  styleUrl: './product-detail.component.scss',
})
export class ProductDetailComponent implements OnInit, OnDestroy {
  @Input() id!: string; // injectado pelo withComponentInputBinding() via :id na rota

  private readonly productService = inject(ProductService);
  private readonly cartService = inject(CartService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);
  private readonly destroy$ = new Subject<void>();

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly product = signal<Product | null>(null);
  readonly imageBroken = signal(false);
  quantity = 1;

  ngOnInit(): void {
    this.loadProduct();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadProduct(): void {
    if (!this.id) return;
    this.loading.set(true);
    this.error.set(null);
    this.imageBroken.set(false);

    this.productService.getProductById(this.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (p) => {
          this.product.set(p);
          this.quantity = 1;
          this.loading.set(false);
        },
        error: () => {
          this.error.set('Produto não encontrado ou indisponível.');
          this.loading.set(false);
        },
      });
  }

  stockQuantity(): number {
    return this.product()?.stockQuantity ?? 0;
  }

  maxQuantity(): number {
    return Math.max(this.stockQuantity(), 0);
  }

  isOutOfStock(): boolean {
    return this.stockQuantity() <= 0;
  }

  isLowStock(): boolean {
    const qty = this.stockQuantity();
    return qty > 0 && qty <= 5;
  }

  stockLabel(): string {
    const qty = this.stockQuantity();
    if (qty <= 0) return 'Estoque: 0 — Fora de estoque';
    if (qty <= 5) return `Estoque: ${qty} — Baixo`;
    return `Estoque: ${qty}`;
  }

  onImageError(): void {
    this.imageBroken.set(true);
  }

  incrementQty(): void {
    if (this.quantity < this.maxQuantity()) {
      this.quantity++;
    }
  }

  decrementQty(): void {
    if (this.quantity > 1) this.quantity--;
  }

  addToCart(): void {
    const p = this.product();
    if (!p) return;

    if (this.isOutOfStock() || !p.active) {
      this.snackBar.open('Produto indisponível ou sem estoque.', 'Fechar', {
        duration: 4000,
        panelClass: ['snack-error'],
      });
      return;
    }

    if (this.quantity > this.maxQuantity()) {
      this.snackBar.open(`Quantidade máxima disponível: ${this.maxQuantity()}`, 'Fechar', {
        duration: 4000,
        panelClass: ['snack-error'],
      });
      return;
    }

    this.cartService.addItem(p, this.quantity).subscribe({
      next: () => {
        this.snackBar.open(
          `${this.quantity}x "${p.name}" adicionado ao carrinho`,
          'Ver Carrinho',
          { duration: 3500, panelClass: ['snack-success'] }
        ).onAction().subscribe(() => this.router.navigate(['/cart']));
      },
      error: (err) => {
        const msg = err?.error?.message ?? 'Não foi possível adicionar ao carrinho. Verifique o estoque.';
        this.snackBar.open(msg, 'Fechar', { duration: 5000, panelClass: ['snack-error'] });
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/products']);
  }

  formatPrice(value: number): string {
    return value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }
}
