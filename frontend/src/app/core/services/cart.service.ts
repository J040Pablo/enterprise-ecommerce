import { Injectable, computed, signal } from '@angular/core';
import { Cart, CartItem } from '../models/cart.model';
import { Product } from '../models/product.model';

const CART_STORAGE_KEY = 'enterprise_cart';

@Injectable({ providedIn: 'root' })
export class CartService {
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
   * Adiciona um produto. Se já existir, incrementa a quantidade.
   */
  addItem(product: Product, quantity = 1): void {
    const current = this.cartState();
    const idx = current.items.findIndex(i => i.productId === product.id);

    let newItems: CartItem[];
    if (idx >= 0) {
      newItems = current.items.map((item, i) =>
        i === idx ? { ...item, quantity: item.quantity + quantity } : item
      );
    } else {
      const newItem: CartItem = {
        productId: product.id,
        productName: product.name,
        price: product.price,
        quantity,
      };
      newItems = [...current.items, newItem];
    }
    this.persist({ items: newItems });
  }

  /**
   * Remove um item pelo productId.
   */
  removeItem(productId: string): void {
    const newItems = this.cartState().items.filter(i => i.productId !== productId);
    this.persist({ items: newItems });
  }

  /**
   * Altera a quantidade de um item.
   * Se quantity < 1, remove o item.
   */
  updateQuantity(productId: string, quantity: number): void {
    if (quantity < 1) {
      this.removeItem(productId);
      return;
    }
    const newItems = this.cartState().items.map(i =>
      i.productId === productId ? { ...i, quantity } : i
    );
    this.persist({ items: newItems });
  }

  /**
   * Limpa todos os itens (chamado após pedido criado com sucesso).
   */
  clearCart(): void {
    this.persist({ items: [] });
  }

  // ── Private ────────────────────────────────────────────────────────────────

  private persist(cart: Cart): void {
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
}
