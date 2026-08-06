import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  template: `
    <div class="orders-container">
      <header class="page-header">
        <h1>Meus Pedidos</h1>
        <p>Acompanhe o histórico de suas compras efetuadas.</p>
      </header>

      <div class="placeholder-box">
        <mat-icon>local_shipping</mat-icon>
        <h3>Nenhum pedido realizado ainda</h3>
        <p>Seus pedidos confirmados aparecerão aqui.</p>
      </div>
    </div>
  `,
  styles: [`
    .orders-container {
      max-width: 1000px;
      margin: 2rem auto;
      padding: 0 1.5rem;
    }
    .page-header {
      margin-bottom: 2rem;
      h1 { font-size: 1.8rem; color: #0f172a; }
      p { color: #64748b; }
    }
    .placeholder-box {
      background: white;
      border-radius: 12px;
      padding: 3rem;
      text-align: center;
      box-shadow: 0 4px 12px rgba(0,0,0,0.05);
      mat-icon {
        font-size: 56px;
        width: 56px;
        height: 56px;
        color: #3b82f6;
        margin-bottom: 1rem;
      }
    }
  `]
})
export class OrderListComponent {}
