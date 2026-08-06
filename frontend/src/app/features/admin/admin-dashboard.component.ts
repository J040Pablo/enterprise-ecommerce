import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  template: `
    <div class="admin-container">
      <header class="page-header">
        <h1>Painel Administrativo</h1>
        <p>Gerenciamento de produtos, estoques e pedidos.</p>
      </header>

      <div class="stats-grid">
        <div class="stat-card">
          <mat-icon class="blue">inventory_2</mat-icon>
          <div class="info">
            <span class="value">128</span>
            <span class="label">Produtos Ativos</span>
          </div>
        </div>
        <div class="stat-card">
          <mat-icon class="green">shopping_bag</mat-icon>
          <div class="info">
            <span class="value">42</span>
            <span class="label">Pedidos Hoje</span>
          </div>
        </div>
        <div class="stat-card">
          <mat-icon class="purple">people</mat-icon>
          <div class="info">
            <span class="value">1,250</span>
            <span class="label">Clientes Registrados</span>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .admin-container {
      max-width: 1100px;
      margin: 2rem auto;
      padding: 0 1.5rem;
    }
    .page-header {
      margin-bottom: 2rem;
      h1 { font-size: 1.8rem; color: #0f172a; }
      p { color: #64748b; }
    }
    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
      gap: 1.5rem;
    }
    .stat-card {
      background: white;
      border-radius: 12px;
      padding: 1.5rem;
      display: flex;
      align-items: center;
      gap: 1.25rem;
      box-shadow: 0 4px 12px rgba(0,0,0,0.05);
      mat-icon {
        font-size: 40px;
        width: 40px;
        height: 40px;
        &.blue { color: #2563eb; }
        &.green { color: #16a34a; }
        &.purple { color: #9333ea; }
      }
      .info {
        display: flex;
        flex-direction: column;
        .value { font-size: 1.5rem; font-weight: 700; color: #1e293b; }
        .label { font-size: 0.875rem; color: #64748b; }
      }
    }
  `]
})
export class AdminDashboardComponent {}
