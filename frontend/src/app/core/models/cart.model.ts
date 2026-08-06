/**
 * Carrinho exclusivamente client-side (Signals + localStorage).
 * O backend não possui CartController.
 */
export interface CartItem {
  productId: string;
  productName: string;
  price: number;
  quantity: number;
}

export interface Cart {
  items: CartItem[];
}
