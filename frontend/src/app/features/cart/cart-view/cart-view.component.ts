import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { CartService } from '../../../core/services/cart.service';
import { CartItem } from '../../../core/models/cart.model';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-cart-view',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatDividerModule,
    MatSnackBarModule,
  ],
  templateUrl: './cart-view.component.html',
  styleUrl: './cart-view.component.scss',
})
export class CartViewComponent {
  readonly cartService = inject(CartService);
  readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  increment(item: CartItem): void {
    this.cartService.updateQuantity(item.productId, item.quantity + 1).subscribe({
      error: (err) => {
        const msg = err?.error?.message ?? 'Não foi possível atualizar a quantidade.';
        this.snackBar.open(msg, 'Fechar', { duration: 5000, panelClass: ['snack-error'] });
      }
    });
  }

  decrement(item: CartItem): void {
    if (item.quantity > 1) {
      this.cartService.updateQuantity(item.productId, item.quantity - 1).subscribe({
        error: (err) => {
          const msg = err?.error?.message ?? 'Não foi possível atualizar a quantidade.';
          this.snackBar.open(msg, 'Fechar', { duration: 5000, panelClass: ['snack-error'] });
        }
      });
    }
  }

  remove(productId: string): void {
    this.cartService.removeItem(productId).subscribe({
      error: (err) => {
        const msg = err?.error?.message ?? 'Não foi possível remover o item.';
        this.snackBar.open(msg, 'Fechar', { duration: 5000, panelClass: ['snack-error'] });
      }
    });
  }

  clearCart(): void {
    this.cartService.clearCart().subscribe({
      error: (err) => {
        const msg = err?.error?.message ?? 'Não foi possível limpar o carrinho.';
        this.snackBar.open(msg, 'Fechar', { duration: 5000, panelClass: ['snack-error'] });
      }
    });
  }

  checkout(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/auth/login'], {
        queryParams: { returnUrl: '/checkout' },
      });
      return;
    }
    this.router.navigate(['/checkout']);
  }

  formatPrice(value: number): string {
    return value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }
}
