/**
 * Espelha exatamente ProductResponse.java do backend.
 * BigDecimal → number  |  UUID → string
 */
export interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  active: boolean;
  categoryId: string;
  categoryName: string;
}

/**
 * Estrutura da paginação do Spring Data (Page<ProductResponse>).
 */
export interface ProductPage {
  content: Product[];
  totalElements: number;
  totalPages: number;
  number: number;   // zero-based page index
  size: number;
  first: boolean;
  last: boolean;
}

/**
 * Espelha ProductFilterRequest.java — todos opcionais.
 */
export interface ProductFilter {
  name?: string;
  minPrice?: number;
  maxPrice?: number;
  active?: boolean;
  categoryId?: string;
}
