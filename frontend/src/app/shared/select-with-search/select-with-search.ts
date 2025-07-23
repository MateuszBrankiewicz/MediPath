import { Component, input, computed, signal, effect, inject } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { TranslationService } from '../../services/translation.service';
import { debounceTime, distinctUntilChanged, of, Subject, switchMap } from 'rxjs';

export interface SelectOption {
  name: string;
  
}

@Component({
  selector: 'app-select-with-search',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './select-with-search.html',
  styleUrl: './select-with-search.scss'
})
export class SelectWithSearch {
  public control = input.required<FormControl>();
  public options = input<SelectOption[]>([]);
  public label = input<string>('');
  public labelKey = input<string>('');
  public placeholder = input<string>('');
  public placeholderKey = input<string>('');
  public name = input<string>('');
  public loadDataFunction = input<((searchTerm: string) => any) | null>(null);


  public searchTerm = signal<string>('');
  public isDropdownOpen = signal<boolean>(false);
  public selectedOption = signal<SelectOption | null>(null);
  public dynamicOptions = signal<SelectOption[]>([]);
  public isLoading = signal<boolean>(false);

  private searchSubject = new Subject<string>();


  private translationService = inject(TranslationService);

  public filteredOptions = computed(() => {
    const allOptions = this.loadDataFunction() ? this.dynamicOptions() : this.options();
    const term = this.searchTerm().toLowerCase();
    return allOptions.filter(option => 
      option.name.toLowerCase().includes(term)
    );
  });

  public hasError = computed(() => {
    const control = this.control();
    return !!(control.invalid && (control.dirty || control.touched));
  });

  constructor() {

    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(term => {
        const loadFn = this.loadDataFunction();
        if(loadFn && term.length >= 0){
          this.isLoading.set(true);
          return loadFn(term);
        }
        return of([])
      })
    ).subscribe({
      next: (options) => {
        console.log(options)
        this.dynamicOptions.set(options as SelectOption[] || []);
        this.isLoading.set(false);
      },
      error:() => {
        this.dynamicOptions.set([]);
        this.isLoading.set(false)
      }
    })
    effect(() => {
      const controlValue = this.control().value;
      const allOptions = this.loadDataFunction() ? this.dynamicOptions() : this.options();
      const option = allOptions.find(opt => opt.name === controlValue);
      this.selectedOption.set(option || null);
      if (option) {
        this.searchTerm.set(option.name);
      }
    });
  }

  public getTranslatedLabel(): string {
    if (this.labelKey()) {
      return this.translationService.translate(this.labelKey());
    }
    return this.label();
  }

  public getTranslatedPlaceholder(): string {
    if (this.placeholderKey()) {
      return this.translationService.translate(this.placeholderKey());
    }
    return this.placeholder();
  }

  public getErrorMessage(): string {
    const control = this.control();
    
    if (control.hasError('required')) {
      return this.translationService.translate('validation.required')
        .replace('{field}', this.getTranslatedLabel());
    }
    
    return '';
  }

  public onInputChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    const value = target.value;
    this.searchTerm.set(value);
    
    if (this.loadDataFunction()) {
      this.searchSubject.next(value);
    }
    
    const allOptions = this.loadDataFunction() ? this.dynamicOptions() : this.options();
    const exactMatch = allOptions.find(opt => opt.name === value);
    if (!exactMatch) {
      this.control().setValue(null);
      this.selectedOption.set(null);
    }
    
    this.isDropdownOpen.set(true);
  }

  public onInputFocus(): void {
    this.isDropdownOpen.set(true);
  }

  public onInputBlur(): void {
    setTimeout(() => {
      this.isDropdownOpen.set(false);
      
      if (!this.selectedOption()) {
        this.searchTerm.set('');
      }
    }, 200);
  }

  public onOptionSelect(option: SelectOption): void {
    
    this.selectedOption.set(option);
    this.searchTerm.set(option.name);
    this.control().setValue(option.name);
    this.isDropdownOpen.set(false);
  }

  public onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      this.isDropdownOpen.set(false);
    }
  }
}