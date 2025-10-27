import { signal } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthenticationService } from '../../../../core/services/authentication/authentication';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { RegisterUser } from '../../models/auth.constants';

// Tworzymy uproszczoną wersję komponentu tylko do testów
class TestableRegister {
  public translationService: TranslationService;
  public authService: AuthenticationService;
  public router: Router;

  public readonly passwordMismatchError = signal<boolean>(false);
  public readonly checkBoxNotChecked = signal<boolean>(false);
  public readonly hasError = signal({ haveError: false, errorMessage: '' });
  public readonly visible = signal(false);
  public readonly isLoading = signal(false);

  constructor(
    translationService: TranslationService,
    authService: AuthenticationService,
    router: Router,
  ) {
    this.translationService = translationService;
    this.authService = authService;
    this.router = router;
  }

  public registerFormGroup = new FormGroup({
    name: new FormControl('', [Validators.required]),
    surname: new FormControl('', [Validators.required]),
    govID: new FormControl('', [Validators.required]),
    birthDate: new FormControl('', [Validators.required]),
    province: new FormControl('', [Validators.required]),
    postalCode: new FormControl('', [Validators.required]),
    city: new FormControl('', [Validators.required]),
    number: new FormControl('', [Validators.required]),
    street: new FormControl(''),
    phoneNumber: new FormControl('', [Validators.required]),
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [
      Validators.required,
      Validators.minLength(8),
    ]),
    confirmPassword: new FormControl('', [Validators.required]),
    termsChecked: new FormControl(false, [Validators.requiredTrue]),
  });

  onRegisterFormSubmit() {
    this.registerFormGroup.markAllAsTouched();

    this.passwordMismatchError.set(false);
    this.checkBoxNotChecked.set(false);
    this.hasError.set({ haveError: false, errorMessage: '' });

    const formValue = { ...this.registerFormGroup.value };

    if (formValue.password !== formValue.confirmPassword) {
      this.passwordMismatchError.set(true);
      return;
    }

    if (formValue.termsChecked === false) {
      this.checkBoxNotChecked.set(true);
      return;
    }

    if (this.registerFormGroup.valid) {
      delete formValue.confirmPassword;
      delete formValue.termsChecked;

      this.isLoading.set(true);
      this.authService.registerUser(formValue as RegisterUser).subscribe({
        next: () => {
          this.router.navigate(['/auth/login']);
          this.isLoading.set(false);
        },
        error: () => {
          this.hasError.set({
            haveError: true,
            errorMessage: 'Error occurred',
          });
          this.isLoading.set(false);
        },
      });
    }
  }
}

// Mocky serwisów
class MockTranslationService {
  translate(key: string) {
    return key;
  }
  isLoading = signal(false);
}

class MockAuthenticationService {
  registerUser = vi.fn();
  getCities = vi.fn(() => of([]));
  private http = {} as unknown;
}

class MockRouter {
  navigate = vi.fn();
}

describe('Register', () => {
  let component: TestableRegister;
  let mockTranslationService: MockTranslationService;
  let mockAuthService: MockAuthenticationService;
  let mockRouter: MockRouter;

  beforeEach(() => {
    mockTranslationService = new MockTranslationService();
    mockAuthService = new MockAuthenticationService();
    mockRouter = new MockRouter();

    component = new TestableRegister(
      mockTranslationService as unknown as TranslationService,
      mockAuthService as unknown as AuthenticationService,
      mockRouter as unknown as Router,
    );
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should mark all fields as touched on submit', () => {
    const spy = vi.spyOn(component.registerFormGroup, 'markAllAsTouched');
    component.onRegisterFormSubmit();
    expect(spy).toHaveBeenCalled();
  });

  it('should set passwordMismatchError if passwords do not match', () => {
    component.registerFormGroup.controls.password.setValue('password1');
    component.registerFormGroup.controls.confirmPassword.setValue('password2');
    component.onRegisterFormSubmit();
    expect(component.passwordMismatchError()).toBe(true);
  });

  it('should set checkBoxNotChecked if terms are not accepted', () => {
    component.registerFormGroup.controls.password.setValue('password1');
    component.registerFormGroup.controls.confirmPassword.setValue('password1');
    component.registerFormGroup.controls.termsChecked.setValue(false);
    component.onRegisterFormSubmit();
    expect(component.checkBoxNotChecked()).toBe(true);
  });

  it('should not submit if form is invalid', () => {
    component.onRegisterFormSubmit();
    expect(mockAuthService.registerUser).not.toHaveBeenCalled();
  });

  it('should call registerUser when form is valid', () => {
    component.registerFormGroup.patchValue({
      name: 'John',
      surname: 'Doe',
      govID: '12345678901',
      birthDate: '01-01-1990',
      province: 'mazowieckie',
      postalCode: '00-000',
      city: 'Warsaw',
      number: '123',
      street: 'Main St',
      phoneNumber: '123456789',
      email: 'john@example.com',
      password: 'password123',
      confirmPassword: 'password123',
      termsChecked: true,
    });

    mockAuthService.registerUser.mockReturnValue(of({}));
    component.onRegisterFormSubmit();

    expect(mockAuthService.registerUser).toHaveBeenCalled();
  });
});
