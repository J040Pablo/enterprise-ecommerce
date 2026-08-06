import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PaymentResponse } from '../models/payment.model';

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = `${environment.apiUrl}/payments`;

  getPaymentById(id: string): Observable<PaymentResponse> {
    return this.http.get<PaymentResponse>(`${this.API_URL}/${id}`);
  }

  approvePayment(id: string): Observable<PaymentResponse> {
    return this.http.patch<PaymentResponse>(`${this.API_URL}/${id}/approve`, {});
  }

  rejectPayment(id: string): Observable<PaymentResponse> {
    return this.http.patch<PaymentResponse>(`${this.API_URL}/${id}/reject`, {});
  }

  refundPayment(id: string): Observable<PaymentResponse> {
    return this.http.patch<PaymentResponse>(`${this.API_URL}/${id}/refund`, {});
  }
}
