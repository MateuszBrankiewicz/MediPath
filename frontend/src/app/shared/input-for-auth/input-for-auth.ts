
  
import { Component, computed, effect, input, signal } from '@angular/core';
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
  

  public readonly type = input<'text'|'password'|'email'|'number'>('text');
  
  public readonly placeholder = input('');
  
  public readonly name = input('')




  getErrorMessage = computed(() => {
    const control = this.control();
    if (control && control.errors && (control.touched || control.dirty)) {
      if (control.errors['required']) {
        return `${this.label()} is required`;
      }
      if (control.errors['email']) {
        return 'Please enter a valid email address';
      }
      if (control.errors['minlength']) {
        return `Password must be at least ${control.errors['minlength'].requiredLength} characters long`;
      }
      if (control.errors['pattern']) {
        const label = this.label().toLowerCase();
        if (label.includes('postal')) {
          return 'Postal code must be in format XX-XXX';
        }
        if (label.includes('phone')) {
          return 'Phone number must be 9 digits';
        }
      }
    }
    return '';
  });

   get hasError(): boolean {
    const control = this.control();
    return !!(control && control.errors && (control.touched || control.dirty));
  }

}
