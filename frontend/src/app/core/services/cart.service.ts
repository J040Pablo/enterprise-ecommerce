import { Injectable, computed, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Cart, CartItem, CartResponse, CartItemResponse } from '../models/cart.model';
import { Product } from '../models/product.model';
import { environment } from '../../../environments/environment';
import { Observable, of, tap, map } from 'rxjs';
import { AuthService } from './auth.service';

const CART_STORAGE_KEY = 'enterprise_cart';

@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);
  private readonly API_URL = `${environment.apiUrl}/cart`;

  private readonly cartState = signal<Cart>(this.loadFromStorage());

  /** Computed signal — lista de itens reativos */
  readonly items = computed(() => this.cartState().items);

  /** Computed signal — total de unidades no carrinho (badge do header) */
  readonly cartCount = computed(() =>
    this.cartState().items.reduce((sum, i) => sum + i.quantity, 0)
  );

  /** Computed signal — valor total do carrinho */
  readonly cartTotal = computed(() =>
    this.cartState().items.reduce((sum, i) => sum + i.price * i.quantity, 0)
  );

  /**
   * Carrega o carrinho do servidor na primeira vez ou quando explicitamente solicitado
   */
  loadCart(): Observable<Cart> {
    if (!this.authService.isAuthenticated()) {
      return of(this.cartState());
    }

    return this.http.get<CartResponse>(`${this.API_URL}`).pipe(
      map(response => this.mapResponseToCart(response)),
      tap(cart => this.updateLocalState(cart))
    );
  }

  /**
   * Adiciona um produto. Se já existir no servidor, incrementa a quantidade.
   * Valida stock no servidor.
   */
  addItem(product: Product, quantity = 1): Observable<Cart> {
    if (!this.authService.isAuthenticated()) {
      const current = this.cartState();
      const idx = current.items.findIndex(item => item.productId === product.id);

      let newItems: CartItem[];
      if (idx >= 0) {
        newItems = current.items.map((item, itemIndex) =>
          itemIndex === idx ? { ...item, quantity: item.quantity + quantity } : item
        );
      } else {
        newItems = [...current.items, {
          productId: product.id,
          productName: product.name,
          price: product.price,
          quantity
        }];
      }

      const cart = { items: newItems };
      this.updateLocalState(cart);
      return of(cart);
    }

    const request = {
      productId: product.id,
      quantity
    };
    return this.http.post<CartResponse>(`${this.API_URL}/items`, request).pipe(
      map(response => this.mapResponseToCart(response)),
      tap(cart => this.updateLocalState(cart))
    );
  }

  /**
   * Remove um item pelo productId.
   */
  removeItem(productId: string): Observable<Cart> {
    if (!this.authService.isAuthenticated()) {
      const cart = {
        items: this.cartState().items.filter(item => item.productId !== productId)
      };
      this.updateLocalState(cart);
      return of(cart);
    }

    return this.http.delete<CartResponse>(`${this.API_URL}/items/${productId}`).pipe(
      map(response => this.mapResponseToCart(response)),
      tap(cart => this.updateLocalState(cart))
    );
  }

  /**
   * Altera a quantidade de um item.
   */
  updateQuantity(productId: string, quantity: number): Observable<Cart> {
    if (!this.authService.isAuthenticated()) {
      if (quantity < 1) {
        return this.removeItem(productId);
      }

      const cart = {
        items: this.cartState().items.map(item =>
          item.productId === productId ? { ...item, quantity } : item
        )
      };
      this.updateLocalState(cart);
      return of(cart);
    }

    const request = { quantity };
    return this.http.patch<CartResponse>(`${this.API_URL}/items/${productId}`, request).pipe(
      map(response => this.mapResponseToCart(response)),
      tap(cart => this.updateLocalState(cart))
    );
  }

  /**
   * Limpa todos os itens do carrinho.
   */
  clearCart(): Observable<void> {
    if (!this.authService.isAuthenticated()) {
      this.updateLocalState({ items: [] });
      return of(void 0);
    }

    return this.http.delete<void>(`${this.API_URL}`).pipe(
      tap(() => {
        this.updateLocalState({ items: [] });
      })
    );
  }

  // ── Private ────────────────────────────────────────────────────────────────

  private updateLocalState(cart: Cart): void {
    this.cartState.set(cart);
    try {
      localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(cart));
    } catch {
      // localStorage pode não estar disponível em SSR ou modo privado
    }
  }

  private loadFromStorage(): Cart {
    try {
      const raw = localStorage.getItem(CART_STORAGE_KEY);
      if (raw) {
        return JSON.parse(raw) as Cart;
      }
    } catch {
      // JSON inválido ou localStorage bloqueado
    }
    return { items: [] };
  }

  private mapResponseToCart(response: CartResponse): Cart {
    return {
      items: response.items.map((item: CartItemResponse) => ({
        productId: item.productId,
        productName: item.productName,
        price: item.productPrice,
        quantity: item.quantity
      }))
    };
  }
}
