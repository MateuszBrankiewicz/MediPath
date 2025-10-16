import { Component, input } from '@angular/core';
import { CardModule } from 'primeng/card';

@Component({
  selector: 'app-info-card',
  imports: [CardModule],
  template: `
    <p-card class="info-card">
      <div class="card-content">
        @if (imageUrl()) {
          <div class="card-image">
            <img [src]="imageUrl()!" [alt]="imageAlt() || ''" />
          </div>
        }
        <div class="card-details">
          <ng-content />
        </div>
      </div>
    </p-card>
  `,
  styles: `
    ::ng-deep .info-card {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      border: 1px solid var(--surface-border);
      transition: all 0.3s ease;

      &:hover {
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
      }

      .p-card-body {
        padding: 2.5rem;

        @media (max-width: 768px) {
          padding: 1.5rem;
        }
      }

      .p-card-content {
        padding: 0;
      }
    }

    .card-content {
      display: flex;
      flex-direction: column;
      gap: 2rem;
    }

    .card-image {
      width: 100%;
      max-width: 600px;
      margin: 0 auto;
      border-radius: 0.75rem;
      overflow: hidden;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      border: 2px solid var(--surface-border);

      img {
        width: 100%;
        height: auto;
        display: block;
      }
    }

    .card-details {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }
  `,
})
export class InfoCard {
  imageUrl = input<string>();
  imageAlt = input<string>();
}
