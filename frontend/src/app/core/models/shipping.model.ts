import { ShippingStatus } from './order.model';

export interface ShippingResponse {
  id: string;
  orderId: string;
  trackingCode: string;
  carrier: string;
  status: ShippingStatus;
  estimatedDelivery?: string;
  shippedAt?: string;
  deliveredAt?: string;
}
