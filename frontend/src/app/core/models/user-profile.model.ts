import { FormControl } from '@angular/forms';

export interface ProfileFormControls {
  name: FormControl<string>;
  surname: FormControl<string>;
  birthDate: FormControl<string>;
  phoneNumber: FormControl<string>;
  governmentId: FormControl<string>;
  province: FormControl<string>;
  postalCode: FormControl<string>;
  city: FormControl<string>;
  number: FormControl<string>;
  street: FormControl<string>;
}
