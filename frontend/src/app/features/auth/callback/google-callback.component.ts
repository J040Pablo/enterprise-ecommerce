import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../../core/services/auth.service';
import { CartService } from '../../../core/services/cart.service';

@Component({
  selector: 'app-google-callback',
  standalone: true,
  imports: [CommonModule, MatProgressSpinnerModule],
  templateUrl: './google-callback.component.html',
  styleUrl: './google-callback.component.scss'
})
export class GoogleCallbackComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private authService = inject(AuthService);
  private cartService = inject(CartService);

  ngOnInit(): void {
    const code = this.route.snapshot.queryParamMap.get('code');

    if (!code) {
      this.router.navigate(['/auth/login'], {
        queryParams: { error: 'google_auth_failed' }
      });
      return;
    }

    this.authService.exchangeOAuthCode(code).subscribe({
      next: () => {
        this.cartService.loadCart().subscribe({
          next: () => this.router.navigate(['/']),
          error: () => this.router.navigate(['/'])
        });
      },
      error: () => {
        this.router.navigate(['/auth/login'], {
          queryParams: { error: 'google_auth_failed' }
        });
      }
    });
  }
}
