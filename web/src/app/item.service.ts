import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface Item {
  id: number;
  sku: string;
  name: string;
  quantity: number;
  price: number;
}

export type ItemInput = Omit<Item, 'id'>;

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ApiError {
  status: number;
  message: string;
  fieldErrors: Record<string, string>;
}

@Injectable({ providedIn: 'root' })
export class ItemService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/items`;

  list(page: number, size: number): Observable<Page<Item>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<Item>>(this.url, { params });
  }

  create(item: ItemInput): Observable<Item> {
    return this.http.post<Item>(this.url, item);
  }

  update(id: number, item: ItemInput): Observable<Item> {
    return this.http.put<Item>(`${this.url}/${id}`, item);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
