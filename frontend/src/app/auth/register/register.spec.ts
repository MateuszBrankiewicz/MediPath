import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { TranslationService } from '../../services/translation.service';
import { AuthenticationService } from '../services/authentication/authentication';
import { Register } from './register';

describe('RegisterComponent', () => {
  let component: Register;
  let fixture: ComponentFixture<Register>;
  let authServiceSpy: jasmine.SpyObj<AuthenticationService>;
  let routerSpy: jasmine.SpyObj<Router>;
  let translationServiceSpy: jasmine.SpyObj<TranslationService>;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthenticationService', [
      'registerUser',
      'getCities',
    ]);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    translationServiceSpy = jasmine.createSpyObj(
      'TranslationService',
      ['translate'],
      { isLoading: of(false) },
    );

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule],
      providers: [
        { provide: AuthenticationService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: TranslationService, useValue: translationServiceSpy },
      ],
      declarations: [Register],
    }).compileComponents();

    fixture = TestBed.createComponent(Register);
    component = fixture.componentInstance;
    translationServiceSpy.translate.and.callFake((key: string) => key);
    authServiceSpy.getCities.and.returnValue(of([]));
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should mark all fields as touched on submit', () => {
    spyOn(component.registerFormGroup, 'markAllAsTouched');
    component.onRegisterFormSubmit();
    expect(component.registerFormGroup.markAllAsTouched).toHaveBeenCalled();
  });

  it('should set passwordMismatchError if passwords do not match', () => {
    component.registerFormGroup.controls.password.setValue('password1');
    component.registerFormGroup.controls.confirmPassword.setValue('password2');
    component.onRegisterFormSubmit();
    expect(component['passwordMismatchError']()).toBeTrue();
  });

  it('should set checkBoxNotChecked if terms are not accepted', () => {
    component.registerFormGroup.controls.password.setValue('password1');
    component.registerFormGroup.controls.confirmPassword.setValue('password1');
    component.registerFormGroup.controls.termsChecked.setValue(false);
    component.onRegisterFormSubmit();
    expect(component['checkBoxNotChecked']()).toBeTrue();
  });
});
