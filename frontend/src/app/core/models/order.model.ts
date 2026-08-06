/**
 * Espelha OrderStatus.java
 */
export type OrderStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'PROCESSING'
  | 'SHIPPED'
  | 'DELIVERED'
  | 'CANCELLED';

/**
 * Espelha PaymentStatus.java
 */
export type PaymentStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'REFUNDED';

/**
 * Espelha ShippingStatus.java
 */
export type ShippingStatus =
  | 'PROCESSING'
  | 'SHIPPED'
  | 'OUT_FOR_DELIVERY'
  | 'DELIVERED'
  | 'CANCELLED';

// ── Requests ──────────────────────────────────────────────────────────────────

/**
 * Espelha OrderItemRequest.java
 */
export interface OrderItemRequest {
  productId: string;   // UUID
  quantity: number;    // @Min(1)
}

/**
 * Espelha CreateOrderRequest.java
 */
export interface CreateOrderRequest {
  userId: string;              // UUID — @NotNull
  items: OrderItemRequest[];   // @NotEmpty
}

// ── Responses ─────────────────────────────────────────────────────────────────

/**
 * Espelha OrderItemResponse.java
 * BigDecimal → number
 */
export interface OrderItemResponse {
  productId: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

/**
 * Espelha PaymentSummaryResponse.java (embedded in OrderResponse)
 */
export interface PaymentSummaryResponse {
  id: string;
  status: PaymentStatus;
}

/**
 * Espelha ShippingSummaryResponse.java (embedded in OrderResponse)
 */
export interface ShippingSummaryResponse {
  id: string;
  trackingCode: string;
  status: ShippingStatus;
}

/**
 * Espelha OrderResponse.java
 * BigDecimal → number  |  UUID → string
 */
export interface OrderResponse {
  id: string;
  userId: string;
  status: OrderStatus;
  items: OrderItemResponse[];
  totalAmount: number;
  payment: PaymentSummaryResponse | null;
  shipping: ShippingSummaryResponse | null;
}
