import { Component, input } from '@angular/core';
import { LanguageSwitcher } from '../language-switcher/language-switcher';

@Component({
  selector: 'app-image-for-auth',
  imports: [LanguageSwitcher],
  templateUrl: './image-for-auth.html',
  styleUrl: './image-for-auth.scss',
})
export class ImageForAuth {
  public readonly imageSrc = input<string>();
  public readonly title = input<string>();
  public readonly subtitle = input<string>();
  public readonly homeLabel = input<string>();
  public readonly aboutLabel = input<string>();
}
