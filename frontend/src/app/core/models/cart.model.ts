/**
 * Local cart state for UI state management
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

/**
 * Backend cart API response
 * Maps to CartResponse.java in backend
 */
export interface CartItemResponse {
  id: string;
  productId: string;
  productName: string;
  productPrice: number;
  quantity: number;
  subtotal: number;
}

export interface CartResponse {
  id: string;
  userId: string;
  items: CartItemResponse[];
  total: number;
}
