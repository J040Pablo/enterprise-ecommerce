import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { InventoryResponse } from '../models/inventory.model';

@Injectable({
  providedIn: 'root'
})
export class InventoryService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = `${environment.apiUrl.replace('/api/v1', '/api')}/inventory`;

  getInventoryByProductId(productId: string): Observable<InventoryResponse> {
    return this.http.get<InventoryResponse>(`${this.API_URL}/${productId}`).pipe(
      catchError(() => of({ id: '', productId, quantity: 0 }))
    );
  }
}
