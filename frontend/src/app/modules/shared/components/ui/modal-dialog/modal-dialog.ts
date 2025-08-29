import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
  output,
} from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';

export type DialogType = 'success' | 'error';

@Component({
  selector: 'app-modal-dialog',
  templateUrl: './modal-dialog.html',
  styleUrls: ['./modal-dialog.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    '[class.success-dialog]': 'type() === "success"',
    '[class.error-dialog]': 'type() === "error"',
  },
  imports: [DialogModule, ButtonModule],
})
export class ModalDialogComponent {
  public readonly visible = input<boolean>(false);
  public readonly type = input<DialogType>('success');
  public readonly title = input<string>('');
  public readonly errorMessage = input<string>('');
  public readonly messages = input<string[]>([]);
  public readonly buttonLabel = input<string>('Ok');
  public readonly buttonIcon = input<string>('');
  public readonly buttonIconPos = input<'left' | 'right'>('left');
  public readonly width = input<string>('30rem');
  public readonly height = input<string>('20rem');

  public readonly buttonClick = output<void>();
  public readonly visibleChange = output<boolean>();

  public readonly dialogStyle = computed(() => ({
    width: this.width(),
    height: this.type() === 'error' ? '16rem' : this.height(),
  }));

  get isVisible() {
    return this.visible();
  }

  set isVisible(value: boolean) {
    this.visibleChange.emit(value);
  }

  onButtonClick() {
    this.buttonClick.emit();
  }

  onVisibleChange(visible: boolean) {
    this.visibleChange.emit(visible);
  }
}
