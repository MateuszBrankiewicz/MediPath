import { Component, input, output } from '@angular/core';
import { ButtonModule } from 'primeng/button';

export interface Hospital {
  id: string;
  name: string;
  address: string;
  specialisation: string[];
  isPublic: boolean;
  imageUrl: string;
}

@Component({
  selector: 'app-hospital-card',
  imports: [ButtonModule],
  templateUrl: './hospital-card.component.html',
  styleUrl: './hospital-card.component.scss',
})
export class HospitalCardComponent {
  public readonly hospital = input.required<Hospital>();
  public readonly editClicked? = output<Hospital>();
  public readonly disable? = output<Hospital>();
  public readonly canEdit = input(false);
  public readonly canDisabled = input(false);

  public onEditDetails() {
    if (this.editClicked) {
      this.editClicked.emit(this.hospital());
    }
  }

  public onDisable() {
    if (this.disable) {
      this.disable.emit(this.hospital());
    }
  }

  protected getSpecialisationText(): string {
    if (!this.hospital().specialisation) {
      return '';
    }
    return this.hospital().specialisation.join(', ');
  }
}
