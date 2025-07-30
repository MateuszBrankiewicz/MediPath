import { CommonModule } from '@angular/common';
import {
  Component,
  computed,
  effect,
  inject,
  input,
  signal,
} from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import {
  debounceTime,
  distinctUntilChanged,
  from,
  of,
  Subject,
  switchMap,
} from 'rxjs';
import { TranslationService } from '../../services/translation.service';

export interface SelectOption {
  name: string;
}

@Component({
  selector: 'app-select-with-search',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './select-with-search.html',
  styleUrl: './select-with-search.scss',
})
export class SelectWithSearch {
  public control = input.required<FormControl>();
  public options = input<SelectOption[]>([]);
  public label = input<string>('');
  public labelKey = input<string>('');
  public placeholder = input<string>('');
  public placeholderKey = input<string>('');
  public name = input<string>('');
  public loadDataFunction = input<((searchTerm: string) => unknown) | null>(
    null,
  );
  public showDropdownButton = input<boolean>(true);

  public searchTerm = signal<string>('');
  public isDropdownOpen = signal<boolean>(false);
  public selectedOption = signal<SelectOption | null>(null);
  public dynamicOptions = signal<SelectOption[]>([]);
  public isLoading = signal<boolean>(false);
  public highlightedIndex = signal<number>(-1);

  private searchSubject = new Subject<string>();

  private translationService = inject(TranslationService);

  public filteredOptions = computed(() => {
    const allOptions = this.loadDataFunction()
      ? this.dynamicOptions()
      : this.options();
    if (!Array.isArray(allOptions)) {
      return [];
    }
    const term = this.searchTerm().toLowerCase();
    return allOptions.filter((option) =>
      option.name.toLowerCase().includes(term),
    );
  });

  public hasError = signal<boolean>(false);

  constructor() {
    this.searchSubject
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((term) => {
          const loadFn = this.loadDataFunction();
          if (loadFn && term.length >= 0) {
            this.isLoading.set(true);
            return from(loadFn(term) as Promise<SelectOption[]>);
          }
          return of([]);
        }),
      )
      .subscribe({
        next: (options) => {
          console.log(options as SelectOption[]);
          this.dynamicOptions.set((options as SelectOption[]) || []);
          this.isLoading.set(false);
        },
        error: () => {
          this.dynamicOptions.set([]);
          this.isLoading.set(false);
        },
      });

    effect(() => {
      const control = this.control();

      const hasErrorValue = !!(
        control.invalid &&
        (control.dirty || control.touched)
      );

      this.hasError.set(hasErrorValue);
    });

    effect(() => {
      const controlValue = this.control().value;
      if (!controlValue) return;

      const allOptions = this.loadDataFunction()
        ? this.dynamicOptions()
        : this.options();
      const option = allOptions.find((opt) => opt.name === controlValue);

      if (option) {
        this.selectedOption.set(option);
        this.searchTerm.set(option.name);
      } else if (controlValue && !this.loadDataFunction()) {
        this.searchTerm.set(controlValue);
        this.selectedOption.set({ name: controlValue });
      } else if (controlValue && this.loadDataFunction()) {
        this.searchTerm.set(controlValue);
        this.selectedOption.set(null);
      }
    });
  }

  private updateErrorState(): void {
    const control = this.control();
    const hasErrorValue = !!(
      control.invalid &&
      (control.dirty || control.touched)
    );
    this.hasError.set(hasErrorValue);
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
      return this.translationService
        .translate('validation.required')
        .replace('{field}', this.getTranslatedLabel());
    }

    return '';
  }

  public onInputChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    const value = target.value;
    this.searchTerm.set(value);
    this.control().markAsDirty();
    this.highlightedIndex.set(-1);

    if (this.loadDataFunction()) {
      this.searchSubject.next(value);
    }

    const allOptions = this.loadDataFunction()
      ? this.dynamicOptions()
      : this.options();
    if (!Array.isArray(allOptions)) {
      return;
    }
    const exactMatch = allOptions.find((opt) => opt.name === value);

    if (exactMatch) {
      this.control().setValue(value);
      this.selectedOption.set(exactMatch);
    } else {
      this.control().setValue(value);
      this.selectedOption.set(null);
    }

    this.updateErrorState();

    this.isDropdownOpen.set(true);
  }

  public onInputFocus(): void {
    this.control().markAsDirty();
    this.control().markAsTouched();
    this.control().updateValueAndValidity();

    this.updateErrorState();

    this.isDropdownOpen.set(true);
    this.highlightedIndex.set(-1);
    if (this.loadDataFunction() && !this.searchTerm()) {
      this.searchSubject.next('');
    }
  }

  public onInputBlur(): void {
    this.control().markAsTouched();

    this.updateErrorState();

    setTimeout(() => {
      this.isDropdownOpen.set(false);

      if (!this.selectedOption() && !this.control().value) {
        this.searchTerm.set('');
        this.control().setValue(null);
        this.updateErrorState();
      }
    }, 200);
  }

  public onOptionSelect(option: SelectOption): void {
    this.selectedOption.set(option);
    this.searchTerm.set(option.name);
    this.control().setValue(option.name);
    this.control().markAsDirty();
    this.control().markAsTouched();

    this.updateErrorState();

    this.isDropdownOpen.set(false);
    this.highlightedIndex.set(-1);
  }

  public onDropdownButtonClick(): void {
    if (this.isDropdownOpen()) {
      this.isDropdownOpen.set(false);
      this.highlightedIndex.set(-1);
    } else {
      this.isDropdownOpen.set(true);
      this.highlightedIndex.set(-1);
      if (this.loadDataFunction() && !this.searchTerm()) {
        this.searchSubject.next('');
      }
    }
  }

  public onKeyDown(event: KeyboardEvent): void {
    const options = this.filteredOptions();

    if (event.key === 'Escape') {
      this.isDropdownOpen.set(false);
      this.highlightedIndex.set(-1);
      return;
    }

    if (!this.isDropdownOpen()) {
      if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
        event.preventDefault();
        this.isDropdownOpen.set(true);
        this.highlightedIndex.set(-1);
        if (this.loadDataFunction() && !this.searchTerm()) {
          this.searchSubject.next('');
        }
      }
      return;
    }

    switch (event.key) {
      case 'ArrowDown': {
        event.preventDefault();
        const nextIndex =
          this.highlightedIndex() < options.length - 1
            ? this.highlightedIndex() + 1
            : 0;
        this.highlightedIndex.set(nextIndex);
        break;
      }

      case 'ArrowUp': {
        event.preventDefault();
        const prevIndex =
          this.highlightedIndex() > 0
            ? this.highlightedIndex() - 1
            : options.length - 1;
        this.highlightedIndex.set(prevIndex);
        break;
      }

      case 'Enter': {
        event.preventDefault();
        const highlightedOption = options[this.highlightedIndex()];
        if (highlightedOption) {
          this.onOptionSelect(highlightedOption);
        }
        break;
      }

      case 'Tab':
        this.isDropdownOpen.set(false);
        this.highlightedIndex.set(-1);
        break;
    }
  }
}
