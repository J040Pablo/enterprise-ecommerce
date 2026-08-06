import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="feature-container">
      <div class="placeholder-card">
        <h2>Autenticação</h2>
        <p>Tela de Login em desenvolvimento (Auth Module).</p>
      </div>
    </div>
  `,
  styles: [`
    .feature-container {
      padding: 2rem;
      display: flex;
      justify-content: center;
    }
    .placeholder-card {
      background: white;
      border-radius: 12px;
      padding: 2.5rem;
      box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05);
      text-align: center;
      max-width: 450px;
      width: 100%;
    }
  `]
})
export class LoginComponent {}
