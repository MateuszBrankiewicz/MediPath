import {
  Component,
  OnInit,
  signal,
  WritableSignal,
  inject,
} from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

// Importy komponentów PrimeNG
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import {
  AutoCompleteModule,
  AutoCompleteCompleteEvent,
} from 'primeng/autocomplete';
import { MultiSelectModule } from 'primeng/multiselect';
import { InputMaskModule } from 'primeng/inputmask';
import { FileUploadModule } from 'primeng/fileupload';
import { ButtonModule } from 'primeng/button';
import { FloatLabelModule } from 'primeng/floatlabel';
import { CommonModule } from '@angular/common';
import { TextareaModule } from 'primeng/textarea';

// Definicje typów dla czytelności
interface Doctor {
  id: number;
  name: string;
  pwz: string;
}

interface InstitutionType {
  id: number;
  name: string;
}
@Component({
  selector: 'app-add-doctors-page',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    CardModule,
    InputTextModule,
    TextareaModule,
    AutoCompleteModule,
    MultiSelectModule,
    InputMaskModule,
    FileUploadModule,
    ButtonModule,
    FloatLabelModule,
  ],
  templateUrl: './add-doctors-page.html',
  styleUrl: './add-doctors-page.scss',
})
export class AddDoctorsPage implements OnInit {
  private fb = inject(FormBuilder);

  // Gówny formularz reaktywny
  institutionForm!: FormGroup;

  // Sygnały do zarządzania dynamicznymi danymi
  allDoctors: WritableSignal<Doctor[]> = signal([
    { id: 1, name: 'Jan Kowalski', pwz: '234567' },
    { id: 2, name: 'Anna Nowak', pwz: '123456' },
    { id: 3, name: 'Piotr Wiśniewski', pwz: '987654' },
    { id: 4, name: 'Katarzyna Wójcik', pwz: '555444' },
  ]);

  filteredDoctors: WritableSignal<Doctor[]> = signal([]);

  allInstitutionTypes: WritableSignal<InstitutionType[]> = signal([
    { id: 1, name: 'Cardiologist' },
    { id: 2, name: 'Neurologist' },
    { id: 3, name: 'Pediatrician' },
    { id: 4, name: 'General practice' },
  ]);

  constructor() {
    // Inicjalizacja formularza z walidatorami
    this.institutionForm = this.fb.group({
      name: ['Szpital kliniczny', Validators.required],
      description: [''],
      doctors: [
        [{ id: 1, name: 'Jan Kowalski', pwz: '234567' }],
        Validators.required,
      ],
      institutionTypes: [
        [{ id: 1, name: 'Cardiologist' }],
        Validators.required,
      ],
      address: this.fb.group({
        province: ['lubelskie', Validators.required],
        postalCode: [
          '99-999',
          [Validators.required, Validators.pattern(/^\d{2}-\d{3}$/)],
        ],
        city: ['Lublin', Validators.required],
        street: ['Nadbystrzycka', Validators.required],
        number: ['99', Validators.required],
      }),
      image: [null],
    });
  }

  ngOnInit(): void {
    this.filteredDoctors.set(this.allDoctors());
  }

  // Metoda do filtrowania lekarzy w komponencie AutoComplete
  searchDoctor(event: AutoCompleteCompleteEvent): void {
    const query = event.query.toLowerCase();
    const filtered = this.allDoctors().filter(
      (doctor) =>
        doctor.name.toLowerCase().includes(query) || doctor.pwz.includes(query),
    );
    this.filteredDoctors.set(filtered);
  }

  // Metoda do obsługi wysłania formularza
  onSubmit(): void {
    if (this.institutionForm.valid) {
      console.log(
        'Formularz jest prawidłowy. Dane:',
        this.institutionForm.value,
      );
      // Tutaj logika wysyłania danych do serwera
    } else {
      console.error('Formularz zawiera błędy.');
      this.institutionForm.markAllAsTouched(); // Pokaż błędy walidacji
    }
  }
}
