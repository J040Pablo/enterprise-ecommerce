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

    // [DEBUG] Valida que user.id é um UUID real antes de enviar ao backend
    // Se não for UUID válido (e.g.: e-mail salvo de sessão OAuth antiga), bloqueia
    const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
    if (!UUID_REGEX.test(user.id)) {
      console.error('[ORDER-DEBUG] user.id inválido (não é UUID):', user.id);
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

    // [DEBUG] Log do payload exato enviado ao backend — compare com CreateOrderRequest.java
    console.info('[ORDER-DEBUG] POST /api/v1/orders — payload enviado:', JSON.stringify(payload, null, 2));

    this.submitting.set(true);
    this.error.set(null);

    this.orderService.createOrder(payload).subscribe({
      next: (order) => {
        console.info('[ORDER-DEBUG] Pedido criado com sucesso:', order.id);
        this.cartService.clearCart();
        this.router.navigate(['/checkout/confirmation', order.id]);
      },
      error: (err) => {
        console.error('[ORDER-DEBUG] Erro na criação do pedido:', err);
        const msg =
          err?.error?.message ??
          'Não foi possível processar o pedido. Tente novamente.';
        this.error.set(msg);
        this.submitting.set(false);
      },
    });
  }
}
