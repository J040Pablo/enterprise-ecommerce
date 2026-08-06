import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ShippingResponse } from '../models/shipping.model';

@Injectable({ providedIn: 'root' })
export class ShippingService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = `${environment.apiUrl}/shippings`;

  getAllShippings(): Observable<ShippingResponse[]> {
    return this.http.get<ShippingResponse[]>(this.API_URL);
  }

  getShippingById(id: string): Observable<ShippingResponse> {
    return this.http.get<ShippingResponse>(`${this.API_URL}/${id}`);
  }

  getShippingByOrderId(orderId: string): Observable<ShippingResponse> {
    return this.http.get<ShippingResponse>(`${this.API_URL}/order/${orderId}`);
  }

  markAsShipped(id: string): Observable<ShippingResponse> {
    return this.http.patch<ShippingResponse>(`${this.API_URL}/${id}/ship`, {});
  }

  markAsOutForDelivery(id: string): Observable<ShippingResponse> {
    return this.http.patch<ShippingResponse>(`${this.API_URL}/${id}/out-for-delivery`, {});
  }

  markAsDelivered(id: string): Observable<ShippingResponse> {
    return this.http.patch<ShippingResponse>(`${this.API_URL}/${id}/deliver`, {});
  }
}
