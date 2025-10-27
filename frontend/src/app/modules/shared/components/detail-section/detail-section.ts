import { Component, input } from '@angular/core';
import { DividerModule } from 'primeng/divider';

@Component({
  selector: 'app-detail-section',
  imports: [DividerModule],
  templateUrl: './detail-section.html',
  styleUrl: './detail-section.scss',
})
export class DetailSection {
  title = input.required<string>();
  icon = input.required<string>();
  showDivider = input<boolean>(true);
}
