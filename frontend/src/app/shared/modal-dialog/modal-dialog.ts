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
  imports: [DialogModule, ButtonModule],
})
export class ModalDialogComponent {
  visible = input<boolean>(false);
  type = input<DialogType>('success');
  title = input<string>('');
  errorMessage = input<string>('');
  messages = input<string[]>([]);
  buttonLabel = input<string>('Ok');
  buttonIcon = input<string>('');
  buttonIconPos = input<'left' | 'right'>('left');
  width = input<string>('30rem');
  height = input<string>('20rem');

  buttonClick = output<void>();
  visibleChange = output<boolean>();

  dialogStyle = computed(() => ({
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
