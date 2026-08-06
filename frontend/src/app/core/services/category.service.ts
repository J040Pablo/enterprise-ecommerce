import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface CategoryResponse {
  id: string;
  name: string;
  description?: string;
  active: boolean;
}

export interface CategoryPage {
  content: CategoryResponse[];
  totalElements: number;
}

export interface CategoryRequest {
  name: string;
  description?: string;
  active?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = `${environment.apiUrl}/categories`;

  getCategories(page = 0, size = 100): Observable<CategoryPage> {
    return this.http.get<CategoryPage>(`${this.API_URL}?page=${page}&size=${size}`);
  }

  createCategory(payload: CategoryRequest): Observable<CategoryResponse> {
    return this.http.post<CategoryResponse>(this.API_URL, payload);
  }

  updateCategory(id: string, payload: CategoryRequest): Observable<CategoryResponse> {
    return this.http.put<CategoryResponse>(`${this.API_URL}/${id}`, payload);
  }

  deleteCategory(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}
