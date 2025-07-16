import { Component, computed, input, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-input-for-auth',
  imports: [ReactiveFormsModule],
  templateUrl: './input-for-auth.html',
  styleUrl: './input-for-auth.scss'
})
export class InputForAuth {
  public readonly label = input("");

  public control = input.required<FormControl>();
  
  public width = input<number>(100);

  public readonly type = signal<'text'|'password'|'email'|number>('text');
  
  public readonly placeholder = signal('');
  
  public readonly name = signal('')

  calculatedWidth = computed(() => ({
  width: `${this.width()}%`
}));
}
