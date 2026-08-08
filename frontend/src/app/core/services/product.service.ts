import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Product, ProductFilter, ProductPage } from '../models/product.model';

export interface CreateProductPayload {
  name: string;
  description?: string;
  price: number;
  initialQuantity: number;
  imageUrl?: string | null;
  categoryId: string;
}

export interface UpdateProductPayload {
  name?: string;
  description?: string;
  price?: number;
  active?: boolean;
  /** Send empty string to clear the image. */
  imageUrl?: string | null;
  categoryId?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = `${environment.apiUrl}/products`;

  getProducts(filter?: ProductFilter, page = 0, size = 10): Observable<ProductPage> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (filter?.name) {
      params = params.set('name', filter.name);
    }
    if (filter?.categoryId) {
      params = params.set('categoryId', filter.categoryId);
    }

    return this.http.get<ProductPage>(this.API_URL, { params });
  }

  getProductById(id: string): Observable<Product> {
    return this.http.get<Product>(`${this.API_URL}/${id}`);
  }

  createProduct(payload: CreateProductPayload): Observable<Product> {
    return this.http.post<Product>(this.API_URL, payload);
  }

  updateProduct(id: string, payload: UpdateProductPayload): Observable<Product> {
    return this.http.put<Product>(`${this.API_URL}/${id}`, payload);
  }

  deleteProduct(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}
