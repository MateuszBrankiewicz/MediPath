import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardModule } from 'primeng/card';

export interface WelcomeCardData {
  userName: string;
  welcomeMessage: string;
  subtitle: string;
  variant?: 'default' | 'gradient';
}

@Component({
  selector: 'app-welcome-card',
  imports: [CommonModule, CardModule],
  template: `
    <p-card
      class="welcome-card"
      [class]="'welcome-card--' + (data().variant || 'default')"
    >
      <div class="welcome-content">
        <h2 class="welcome-title">
          {{ data().welcomeMessage }} {{ data().userName }}!
        </h2>
        <p class="welcome-subtitle">{{ data().subtitle }}</p>
      </div>
    </p-card>
  `,
  styleUrl: './welcome-card.scss',
})
export class WelcomeCard {
  readonly data = input.required<WelcomeCardData>();
}
