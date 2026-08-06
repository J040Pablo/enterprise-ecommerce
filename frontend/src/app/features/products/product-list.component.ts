import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule],
  template: `
    <div class="products-container">
      <header class="page-header">
        <h1>Catálogo de Produtos</h1>
        <p>Explore os melhores produtos tecnológicos do Enterprise E-Commerce.</p>
      </header>

      <div class="product-grid">
        <mat-card class="product-card" *ngFor="let item of dummyProducts">
          <div class="card-image-placeholder">
            <mat-icon>{{ item.icon }}</mat-icon>
          </div>
          <mat-card-header>
            <mat-card-title>{{ item.title }}</mat-card-title>
            <mat-card-subtitle>{{ item.category }}</mat-card-subtitle>
          </mat-card-header>
          <mat-card-content>
            <p>{{ item.description }}</p>
            <div class="price-tag">R$ {{ item.price.toFixed(2) }}</div>
          </mat-card-content>
          <mat-card-actions class="card-actions">
            <button mat-flat-button color="primary">
              <mat-icon>add_shopping_cart</mat-icon>
              Adicionar
            </button>
          </mat-card-actions>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .products-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 2rem 1.5rem;
    }
    .page-header {
      margin-bottom: 2rem;
      h1 {
        font-size: 2rem;
        font-weight: 700;
        color: #1a1a2e;
        margin-bottom: 0.5rem;
      }
      p {
        color: #666;
        font-size: 1rem;
      }
    }
    .product-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
      gap: 1.5rem;
    }
    .product-card {
      border-radius: 12px;
      overflow: hidden;
      transition: transform 0.2s ease, box-shadow 0.2s ease;
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 12px 24px rgba(0,0,0,0.1);
      }
    }
    .card-image-placeholder {
      height: 160px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      mat-icon {
        font-size: 64px;
        width: 64px;
        height: 64px;
        color: rgba(255, 255, 255, 0.9);
      }
    }
    mat-card-header {
      padding-top: 1rem;
    }
    .price-tag {
      font-size: 1.25rem;
      font-weight: 700;
      color: #2b6cb0;
      margin-top: 0.5rem;
    }
    .card-actions {
      padding: 1rem;
      display: flex;
      justify-content: flex-end;
    }
  `]
})
export class ProductListComponent {
  dummyProducts = [
    { title: 'Notebook Pro M3', category: 'Informática', description: 'Processador de altíssimo desempenho para profissionais.', price: 8999.90, icon: 'laptop' },
    { title: 'Smartphone Ultra 5G', category: 'Celulares', description: 'Câmera de 200MP e bateria de longa duração.', price: 5499.00, icon: 'smartphone' },
    { title: 'Fone Wireless Noise Cancel', category: 'Áudio', description: 'Cancelamento ativo de ruído inteligente.', price: 1299.00, icon: 'headphones' },
    { title: 'Monitor Curved 4K 144Hz', category: 'Monitores', description: 'Imersão completa com painel OLED.', price: 3499.00, icon: 'desktop_windows' }
  ];
}
