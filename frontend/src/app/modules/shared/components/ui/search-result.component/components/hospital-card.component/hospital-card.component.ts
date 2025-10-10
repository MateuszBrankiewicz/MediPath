import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  input,
  output,
} from '@angular/core';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { ButtonModule } from 'primeng/button';
import { TranslationService } from '../../../../../../../core/services/translation/translation.service';
import { AddressFormatPipe } from '../../../../../../../core/pipes/address-format-pipe';

export interface Hospital {
  id: string;
  name: string;
  address: string;
  specialisation: string[];
  isPublic: boolean;
  imageUrl: string;
  image?: string;
}

@Component({
  selector: 'app-hospital-card',
  imports: [ButtonModule, AddressFormatPipe],
  templateUrl: './hospital-card.component.html',
  styleUrl: './hospital-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HospitalCardComponent {
  protected readonly translationService = inject(TranslationService);
  private readonly sanitizer = inject(DomSanitizer);

  public readonly hospital = input.required<Hospital>();
  public readonly editClicked = output<Hospital>();
  public readonly disable = output<Hospital>();
  public readonly canEdit = input(false);
  public readonly canDisabled = input(false);
  public readonly institutionClicked = output<Hospital>();
  public readonly showInfoButton = input(true);

  protected readonly imageUrl = computed<SafeUrl | null>(() => {
    const rawImageUrl = this.hospital().imageUrl;
    if (rawImageUrl) {
      return this.sanitizer.bypassSecurityTrustResourceUrl(rawImageUrl);
    }
    return null;
  });

  protected readonly hasImage = computed(() => this.imageUrl() !== null);

  public onCardClick(): void {
    this.institutionClicked.emit(this.hospital());
  }

  public onEditDetails(): void {
    this.editClicked.emit(this.hospital());
  }

  public onDisable(): void {
    this.disable.emit(this.hospital());
  }

  protected getSpecialisationText(): string {
    if (
      !this.hospital().specialisation ||
      this.hospital().specialisation.length === 0
    ) {
      return 'None';
    }
    return this.hospital().specialisation?.join(', ') ?? '';
  }
  constructor() {
    effect(() => {
      console.log('hospital imageUrl:', this.hospital().imageUrl);
    });
  }
  protected onImageLoad(event: Event): void {
    console.log('Image loaded successfully:', event);
  }
}
