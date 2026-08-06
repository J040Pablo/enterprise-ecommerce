import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-cart-view',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, RouterLink],
  template: `
    <div class="cart-container">
      <div class="empty-cart-card">
        <mat-icon class="cart-icon">shopping_cart</mat-icon>
        <h2>Seu carrinho está vazio</h2>
        <p>Explore nosso catálogo e encontre os melhores produtos para você.</p>
        <button mat-raised-button color="primary" routerLink="/products">
          <mat-icon>storefront</mat-icon>
          Ver Produtos
        </button>
      </div>
    </div>
  `,
  styles: [`
    .cart-container {
      max-width: 800px;
      margin: 3rem auto;
      padding: 0 1.5rem;
      display: flex;
      justify-content: center;
    }
    .empty-cart-card {
      background: #ffffff;
      border-radius: 16px;
      padding: 3rem;
      text-align: center;
      box-shadow: 0 10px 30px rgba(0,0,0,0.05);
      width: 100%;
      .cart-icon {
        font-size: 72px;
        width: 72px;
        height: 72px;
        color: #94a3b8;
        margin-bottom: 1rem;
      }
      h2 {
        font-size: 1.5rem;
        color: #1e293b;
        margin-bottom: 0.5rem;
      }
      p {
        color: #64748b;
        margin-bottom: 2rem;
      }
    }
  `]
})
export class CartViewComponent {}
