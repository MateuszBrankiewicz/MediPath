import { Component, input } from '@angular/core';

@Component({
  selector: 'app-detail-item',
  imports: [],
  template: `
    <div class="detail-item">
      <span class="detail-label">
        <i [class]="'pi ' + icon()"></i>
        {{ label() }}
      </span>
      <span class="detail-value">
        <ng-content />
      </span>
    </div>
  `,
  styles: `
    .detail-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1rem;
      background-color: var(--surface-0);
      border-radius: 0.5rem;
      transition: all 0.2s ease;

      &:hover {
        background-color: var(--surface-50);
        padding-left: 1.25rem;
      }

      .detail-label {
        display: flex;
        align-items: center;
        gap: 0.625rem;
        font-weight: 600;
        color: var(--text-color-secondary);
        font-size: 0.95rem;

        i {
          font-size: 1.125rem;
          color: var(--primary-color);
          opacity: 0.8;
        }
      }

      .detail-value {
        font-weight: 500;
        color: var(--text-color);
        font-size: 1rem;
        display: flex;
        align-items: center;
        gap: 0.5rem;
      }

      @media (max-width: 768px) {
        flex-direction: column;
        align-items: flex-start;
        gap: 0.5rem;
      }
    }
  `,
})
export class DetailItem {
  label = input.required<string>();
  icon = input.required<string>();
}
