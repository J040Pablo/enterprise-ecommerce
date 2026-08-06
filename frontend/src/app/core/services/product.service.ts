import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Product, ProductFilter, ProductPage } from '../models/product.model';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = `${environment.apiUrl}/products`;

  /**
   * GET /api/v1/products
   * Retorna Page<ProductResponse> do Spring Data.
   * Parâmetros de paginação: page, size, sort (Spring Pageable convention).
   * Parâmetros de filtro mapeiam para ProductFilterRequest.
   */
  getProducts(
    filter: ProductFilter = {},
    page = 0,
    size = 12,
    sort = 'name,asc'
  ): Observable<ProductPage> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

    if (filter.name) {
      params = params.set('name', filter.name);
    }
    if (filter.minPrice != null) {
      params = params.set('minPrice', filter.minPrice.toString());
    }
    if (filter.maxPrice != null) {
      params = params.set('maxPrice', filter.maxPrice.toString());
    }
    if (filter.active != null) {
      params = params.set('active', filter.active.toString());
    }
    if (filter.categoryId) {
      params = params.set('categoryId', filter.categoryId);
    }

    return this.http.get<ProductPage>(this.API_URL, { params });
  }

  /**
   * GET /api/v1/products/{id}
   */
  getProductById(id: string): Observable<Product> {
    return this.http.get<Product>(`${this.API_URL}/${id}`);
  }
}
