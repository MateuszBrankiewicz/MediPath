import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardModule } from 'primeng/card';

export type StatsCardVariant = 'default' | 'primary' | 'gradient';

export interface StatsCardData {
  title: string;
  value: string | number;
  subtitle?: string;
  icon?: string;
  variant?: StatsCardVariant;
}

@Component({
  selector: 'app-stats-card',
  imports: [CommonModule, CardModule],
  template: `
    <p-card class="stats-card" [class]="'stats-card--' + data().variant">
      <div class="stats-content">
        @if (data().icon) {
          <div class="stats-icon">
            <i [class]="data().icon"></i>
          </div>
        }
        <div class="stats-text">
          <h3 class="stats-title">{{ data().title }}</h3>
          <div class="stats-value-container">
            <span class="stats-value">{{ data().value }}</span>
            @if (data().subtitle) {
              <span class="stats-subtitle">{{ data().subtitle }}</span>
            }
          </div>
        </div>
      </div>
    </p-card>
  `,
  styleUrl: './stats-card.scss',
})
export class StatsCard {
  readonly data = input.required<StatsCardData>();
}
