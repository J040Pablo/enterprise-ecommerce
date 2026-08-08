import { Component, Inject, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef, MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Product } from '../../../../core/models/product.model';
import { CategoryService, CategoryResponse, CategoryRequest } from '../../../../core/services/category.service';
import { CategoryFormDialogComponent, CategoryFormDialogData } from '../../admin-categories/category-form-dialog/category-form-dialog.component';

export interface ProductFormDialogData {
  product?: Product;
  isEdit: boolean;
}

@Component({
  selector: 'app-product-form-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTooltipModule
  ],
  templateUrl: './product-form-dialog.component.html',
  styleUrl: './product-form-dialog.component.scss'
})
export class ProductFormDialogComponent implements OnInit {
  private fb = inject(FormBuilder);
  private categoryService = inject(CategoryService);
  private matDialog = inject(MatDialog);
  public dialogRef = inject(MatDialogRef<ProductFormDialogComponent>);

  @Inject(MAT_DIALOG_DATA) public data!: ProductFormDialogData;

  form!: FormGroup;
  categories = signal<CategoryResponse[]>([]);
  loadingCategories = signal<boolean>(true);
  submitting = signal<boolean>(false);

  constructor(@Inject(MAT_DIALOG_DATA) dataInput: ProductFormDialogData) {
    this.data = dataInput;
  }

  ngOnInit(): void {
    this.initForm();
    this.loadCategories();
  }

  private initForm(): void {
    const isEdit = this.data?.isEdit;
    const p = this.data?.product;

    this.form = this.fb.group({
      name: [p?.name || '', [Validators.required, Validators.maxLength(255)]],
      description: [p?.description || '', [Validators.maxLength(2000)]],
      price: [p?.price || '', [Validators.required, Validators.min(0.01)]],
      initialQuantity: [0, isEdit ? [] : [Validators.required, Validators.min(0)]],
      imageUrl: [p?.imageUrl || '', [Validators.maxLength(512)]],
      categoryId: [p?.categoryId || '', [Validators.required]],
      active: [p?.active ?? true]
    });
  }

  get imagePreviewUrl(): string | null {
    const value = this.form?.get('imageUrl')?.value;
    if (typeof value !== 'string' || !value.trim()) {
      return null;
    }
    return value.trim();
  }

  clearImageUrl(): void {
    this.form.get('imageUrl')?.setValue('');
  }

  onPreviewError(event: Event): void {
    const img = event.target as HTMLImageElement | null;
    if (img) {
      img.style.display = 'none';
    }
  }

  loadCategories(selectId?: string): void {
    this.loadingCategories.set(true);
    this.categoryService.getCategories(0, 200).subscribe({
      next: (page) => {
        const list = (page.content || []).filter(c => c.active);
        this.categories.set(list);
        this.loadingCategories.set(false);
        if (selectId) {
          this.form.get('categoryId')?.setValue(selectId);
        }
      },
      error: () => {
        this.loadingCategories.set(false);
      }
    });
  }

  openNewCategoryDialog(): void {
    const data: CategoryFormDialogData = { isEdit: false };
    this.matDialog
      .open(CategoryFormDialogComponent, { width: '480px', data })
      .afterClosed()
      .subscribe((result: CategoryRequest | null) => {
        if (!result) return;
        this.categoryService.createCategory(result).subscribe({
          next: (created) => {
            // Reload list and auto-select the new category
            this.loadCategories(created.id);
          }
        });
      });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.dialogRef.close(this.form.value);
  }

  onCancel(): void {
    this.dialogRef.close(null);
  }
}
