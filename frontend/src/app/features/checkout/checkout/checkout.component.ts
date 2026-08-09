import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { CartService } from '../../../core/services/cart.service';
import { AuthService } from '../../../core/services/auth.service';
import { OrderService } from '../../../core/services/order.service';
import { CreateOrderRequest } from '../../../core/models/order.model';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatDividerModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './checkout.component.html',
  styleUrl: './checkout.component.scss',
})
export class CheckoutComponent implements OnInit {
  readonly cartService = inject(CartService);
  private readonly authService = inject(AuthService);
  private readonly orderService = inject(OrderService);
  private readonly router = inject(Router);

  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    // Redireciona se carrinho vazio
    if (this.cartService.items().length === 0) {
      this.router.navigate(['/cart']);
    }
  }

  formatPrice(value: number): string {
    return value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }

  placeOrder(): void {
    const user = this.authService.currentUser();
    if (!user) {
      this.router.navigate(['/auth/login'], { queryParams: { returnUrl: '/checkout' } });
      return;
    }

    // Bloqueia sessão antiga onde user.id não é UUID (ex.: e-mail salvo após OAuth)
    const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
    if (!UUID_REGEX.test(user.id)) {
      this.error.set(
        'Sessão inválida: faça logout e login novamente para continuar.'
      );
      return;
    }

    const items = this.cartService.items().map((i) => ({
      productId: i.productId,
      quantity: i.quantity,
    }));

    const payload: CreateOrderRequest = {
      userId: user.id,
      items,
    };

    this.submitting.set(true);
    this.error.set(null);

    this.orderService.createOrder(payload).subscribe({
      next: (order) => {
        this.cartService.clearCart().subscribe({
          next: () => {
            this.router.navigate(['/checkout/confirmation', order.id]);
          },
          error: () => {
            // Mesmo se limpar o carrinho falhar, navegue para confirmação
            this.router.navigate(['/checkout/confirmation', order.id]);
          }
        });
      },
      error: (err) => {
        const msg =
          err?.error?.message ??
          'Não foi possível processar o pedido. Tente novamente.';
        this.error.set(msg);
        this.submitting.set(false);
      },
    });
  }
}
