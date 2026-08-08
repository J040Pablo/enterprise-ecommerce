import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, of, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  LoginRequest,
  LoginResponse,
  CreateUserRequest,
  User,
  TokenRefreshResponse,
  RefreshTokenRequest,
  LogoutRequest
} from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private readonly API_URL = `${environment.apiUrl}/auth`;
  private readonly TOKEN_KEY = 'access_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';
  private readonly USER_KEY = 'user_data';

  // Reactive state using Angular Signals
  currentUser = signal<User | null>(this.getStoredUser());
  isAuthenticated = computed(() => !!this.currentUser() && !!this.getToken());

  constructor() {}

  register(data: CreateUserRequest): Observable<User> {
    return this.http.post<User>(`${this.API_URL}/register`, data);
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API_URL}/login`, credentials).pipe(
      tap((response) => {
        this.saveAuthData(response.token, response.refreshToken, response.user);
      })
    );
  }

  refreshToken(): Observable<TokenRefreshResponse> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) {
      this.clearSession();
      return throwError(() => new Error('No refresh token available'));
    }

    const payload: RefreshTokenRequest = { refreshToken };
    return this.http.post<TokenRefreshResponse>(`${this.API_URL}/refresh`, payload).pipe(
      tap((response) => {
        this.updateTokens(response.accessToken, response.refreshToken);
      }),
      catchError((error) => {
        this.clearSession();
        return throwError(() => error);
      })
    );
  }

  logout(): Observable<void> {
    const refreshToken = this.getRefreshToken();
    const logout$ = refreshToken
      ? this.http.post<void>(`${this.API_URL}/logout`, { refreshToken } as LogoutRequest)
      : of(void 0);

    return logout$.pipe(
      catchError(() => of(void 0)),
      tap(() => {
        this.clearSession();
        this.router.navigate(['/auth/login']);
      })
    );
  }

  loginWithGoogle(): void {
    window.location.href = `${this.API_URL}/google`;
  }

  /**
   * Chamado pelo GoogleCallbackComponent após redirect OAuth2.
   * @param userId UUID real do usuário devolvido pelo OAuth2SuccessHandler no redirect.
   *               Garante que user.id seja um UUID válido e não o e-mail do JWT sub.
   */
  handleOAuthCallback(token: string, refreshToken: string, userId?: string): void {
    const user = this.decodeUserFromToken(token);
    // Sobrescreve o id apenas se o UUID real foi fornecido pelo backend no redirect
    if (userId) {
      user.id = userId;
    }
    this.saveAuthData(token, refreshToken, user);
  }

  /**
   * Extrai dados básicos do JWT para preencher o objeto User.
   * ATENÇÃO: o claim `sub` contém o e-mail, NÃO o UUID do usuário.
   * O UUID vem do claim `userId` (emitido por JwtService) e/ou do query param
   * `userId` no redirect OAuth (preferido em handleOAuthCallback).
   */
  private decodeUserFromToken(token: string): User {
    try {
      const payloadBase64 = token.split('.')[1];
      const decodedJson = atob(payloadBase64.replace(/-/g, '+').replace(/_/g, '/'));
      const payload = JSON.parse(decodedJson);

      const roles = Array.isArray(payload.roles)
        ? payload.roles
        : (payload.roles ? [payload.roles] : ['CUSTOMER']);

      // payload.sub = e-mail (não UUID) — NÃO usar como id
      return {
        id: payload.userId || '',
        email: payload.sub || payload.email || '',
        firstName: payload.firstName || payload.given_name || payload.name || 'Usuário',
        lastName: payload.lastName || payload.family_name || '',
        roles: roles
      };
    } catch {
      return {
        id: '',
        email: '',
        firstName: 'Usuário',
        lastName: '',
        roles: ['CUSTOMER']
      };
    }
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  private getStoredUser(): User | null {
    const userJson = localStorage.getItem(this.USER_KEY);
    if (!userJson) return null;
    try {
      return JSON.parse(userJson) as User;
    } catch {
      return null;
    }
  }

  private saveAuthData(token: string, refreshToken: string, user: User): void {
    localStorage.setItem(this.TOKEN_KEY, token);
    localStorage.setItem(this.REFRESH_TOKEN_KEY, refreshToken);
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    this.currentUser.set(user);
  }

  private updateTokens(accessToken: string, refreshToken: string): void {
    localStorage.setItem(this.TOKEN_KEY, accessToken);
    if (refreshToken) {
      localStorage.setItem(this.REFRESH_TOKEN_KEY, refreshToken);
    }
  }

  clearSession(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUser.set(null);
  }
}
