import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../../core/services/auth.service';

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

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');
    const refreshToken = this.route.snapshot.queryParamMap.get('refreshToken');
    // userId adicionado ao redirect pelo OAuth2SuccessHandler para garantir que
    // user.id seja o UUID real do banco, não o e-mail lido do JWT sub
    const userId = this.route.snapshot.queryParamMap.get('userId') ?? undefined;

    if (token && refreshToken) {
      this.authService.handleOAuthCallback(token, refreshToken, userId);
      this.router.navigate(['/']);
    } else {
      this.router.navigate(['/auth/login'], {
        queryParams: { error: 'google_auth_failed' }
      });
    }
  }
}
