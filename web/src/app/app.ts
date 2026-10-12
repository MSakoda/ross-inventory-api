import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiError, Item, ItemInput, ItemService, Page } from './item.service';

const EMPTY: ItemInput = { sku: '', name: '', quantity: 0, price: 0 };

@Component({
  selector: 'app-root',
  imports: [FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private readonly items = inject(ItemService);

  protected readonly pageData = signal<Page<Item> | null>(null);
  protected readonly pageIndex = signal(0);
  protected readonly error = signal<string | null>(null);
  protected readonly fieldErrors = signal<Record<string, string>>({});
  protected readonly editingId = signal<number | null>(null);
  protected form: ItemInput = { ...EMPTY };
  protected readonly pageSize = 5;

  constructor() {
    this.load();
  }

  protected load(page = this.pageIndex()): void {
    this.items.list(page, this.pageSize).subscribe({
      next: (data) => {
        this.pageData.set(data);
        this.pageIndex.set(data.page);
        this.error.set(null);
      },
      error: (e) => this.fail(e),
    });
  }

  protected save(): void {
    const id = this.editingId();
    const request = id === null ? this.items.create(this.form) : this.items.update(id, this.form);
    request.subscribe({
      next: () => {
        this.resetForm();
        this.load();
      },
      error: (e) => this.fail(e),
    });
  }

  protected edit(item: Item): void {
    this.editingId.set(item.id);
    this.form = { sku: item.sku, name: item.name, quantity: item.quantity, price: item.price };
    this.fieldErrors.set({});
  }

  protected remove(item: Item): void {
    this.items.delete(item.id).subscribe({
      next: () => {
        // Deleting the last row of a page would leave us on an empty page, so step back.
        const onlyRow = this.pageData()?.content.length === 1;
        this.load(onlyRow && this.pageIndex() > 0 ? this.pageIndex() - 1 : this.pageIndex());
      },
      error: (e) => this.fail(e),
    });
  }

  protected resetForm(): void {
    this.editingId.set(null);
    this.form = { ...EMPTY };
    this.fieldErrors.set({});
    this.error.set(null);
  }

  private fail(e: HttpErrorResponse): void {
    const body = e.error as Partial<ApiError> | null;
    this.fieldErrors.set(body?.fieldErrors ?? {});
    this.error.set(body?.message ?? 'Could not reach the API');
  }
}
