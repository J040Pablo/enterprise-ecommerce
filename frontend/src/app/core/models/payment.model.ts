import { PaymentStatus } from './order.model';

export interface PaymentResponse {
  id: string;
  orderId: string;
  amount: number;
  status: PaymentStatus;
  paymentMethod: string;
}
