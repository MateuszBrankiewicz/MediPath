import { Component, input, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-input-for-auth',
  imports: [ReactiveFormsModule],
  templateUrl: './input-for-auth.html',
  styleUrl: './input-for-auth.scss'
})
export class InputForAuth {
  protected readonly label = input("");

  protected readonly formControll = input('');
  
  protected readonly type = signal<'text'|'password'|'email'|number>('text');
  
  protected readonly placeholder = signal('');
  
  protected readonly name = signal('')
}
