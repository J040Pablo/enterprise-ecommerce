import { Component, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Clipboard, ClipboardModule } from '@angular/cdk/clipboard';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

@Component({
  selector: 'app-copy-uuid',
  standalone: true,
  imports: [
    CommonModule,
    ClipboardModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
    MatSnackBarModule
  ],
  template: `
    <div class="uuid-wrapper" *ngIf="uuid">
      <span
        class="uuid-text"
        [matTooltip]="uuid"
        matTooltipPosition="above">
        {{ uuid | slice:0:8 }}...{{ uuid | slice:-4 }}
      </span>
      <button
        mat-icon-button
        class="copy-btn"
        [matTooltip]="'Copiar UUID completo'"
        (click)="copyToClipboard($event)"
        aria-label="Copiar UUID">
        <mat-icon>content_copy</mat-icon>
      </button>
    </div>
    <span *ngIf="!uuid" class="text-muted">-</span>
  `,
  styles: [`
    .uuid-wrapper {
      display: inline-flex;
      align-items: center;
      gap: 0.25rem;
      font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
      white-space: nowrap;
    }

    .uuid-text {
      font-size: 0.825rem;
      color: #475569;
      font-weight: 500;
      cursor: help;
    }

    .copy-btn {
      width: 24px !important;
      height: 24px !important;
      line-height: 24px !important;

      mat-icon {
        font-size: 14px !important;
        width: 14px !important;
        height: 14px !important;
        color: #94a3b8;
        transition: color 0.15s ease;
      }

      &:hover mat-icon {
        color: #2563eb;
      }
    }

    .text-muted {
      color: #94a3b8;
    }
  `]
})
export class CopyUuidComponent {
  @Input() uuid: string | undefined | null = '';

  private clipboard = inject(Clipboard);
  private snackBar = inject(MatSnackBar);

  copyToClipboard(event: MouseEvent): void {
    event.stopPropagation();
    if (!this.uuid) return;

    const success = this.clipboard.copy(this.uuid);
    if (success) {
      this.snackBar.open('UUID copiado.', 'OK', {
        duration: 2500,
        panelClass: ['snackbar-success']
      });
    }
  }
}
