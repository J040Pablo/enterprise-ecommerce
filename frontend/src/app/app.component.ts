import { Component, inject, signal } from '@angular/core';
import { Router, RouterOutlet, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { filter } from 'rxjs/operators';
import { HeaderComponent } from './shared/layout/header/header.component';
import { FooterComponent } from './shared/layout/footer/footer.component';
import { AuthService } from './core/services/auth.service';
import { CartService } from './core/services/cart.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, HeaderComponent, FooterComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'enterprise-ecommerce-frontend';
  private router = inject(Router);
  private authService = inject(AuthService);
  private cartService = inject(CartService);
  isAdminRoute = signal<boolean>(false);

  constructor() {
    if (this.authService.isAuthenticated()) {
      this.cartService.loadCart().subscribe();
    }

    this.checkRoute(this.router.url);
    this.router.events
      .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe((event) => {
        this.checkRoute(event.urlAfterRedirects);
      });
  }

  private checkRoute(url: string): void {
    this.isAdminRoute.set(url.startsWith('/admin'));
  }
}
