import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, input } from '@angular/core';

export interface DashboardConfig {
  title: string;
  showSearch?: boolean;
  showNotifications?: boolean;
  userDisplayName?: string;
  userRole?: string;
}

@Component({
  selector: 'app-dashboard-layout',
  template: `
    <div class="dashboard-container">
      <!-- Header -->

      <!-- Main Content Area -->
      <main class="dashboard-main">
        <!-- Primary Content (left side) -->
        <div class="primary-content">
          <ng-content select="[slot=primary]" />
        </div>

        <!-- Secondary Content (right side) -->
        @if (hasSecondaryContent()) {
          <aside class="secondary-content">
            <ng-content select="[slot=secondary]" />
          </aside>
        }
      </main>

      <!-- Bottom Section -->
      <section class="dashboard-bottom">
        <ng-content select="[slot=bottom]" />
      </section>
    </div>
  `,
  styleUrl: './dashboard-layout-component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
})
export class DashboardLayoutComponent {
  readonly config = input.required<DashboardConfig>();

  protected hasSecondaryContent(): boolean {
    // Sprawdź czy jest zawartość w secondary slot
    return true; // Można to ulepszyć sprawdzając rzeczywistą zawartość
  }
}
