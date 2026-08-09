import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CreateOrderRequest, OrderResponse, OrderStatus } from '../models/order.model';

@Injectable({ providedIn: 'root' })
export class OrderService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = `${environment.apiUrl}/orders`;

  /**
   * POST /api/v1/orders
   * Retorna 201 Created com OrderResponse.
   */
  createOrder(request: CreateOrderRequest): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(this.API_URL, request);
  }

  /**
   * GET /api/v1/orders
   * Lista todos os pedidos no sistema (Admin).
   */
  getAllOrders(): Observable<OrderResponse[]> {
    return this.http.get<OrderResponse[]>(this.API_URL);
  }

  /**
   * GET /api/v1/orders/user/{userId}
   * Lista todos os pedidos de um usuário.
   */
  getOrdersByUser(userId: string): Observable<OrderResponse[]> {
    return this.http.get<OrderResponse[]>(`${this.API_URL}/user/${userId}`);
  }

  /**
   * GET /api/v1/orders/{id}
   * Detalhe de um pedido específico.
   */
  getOrderById(id: string): Observable<OrderResponse> {
    return this.http.get<OrderResponse>(`${this.API_URL}/${id}`);
  }

  /**
   * PATCH /api/v1/orders/{id}/status
   * Atualiza o status do pedido.
   */
  updateStatus(id: string, status: OrderStatus): Observable<OrderResponse> {
    return this.http.patch<OrderResponse>(`${this.API_URL}/${id}/status`, { status });
  }

  /**
   * PATCH /api/v1/orders/{id}/cancel
   * Cancela o pedido.
   */
  cancelOrder(id: string): Observable<OrderResponse> {
    return this.http.patch<OrderResponse>(`${this.API_URL}/${id}/cancel`, {});
  }
}
