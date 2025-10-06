import {
  ChangeDetectionStrategy,
  Component,
  inject,
  signal,
} from '@angular/core';
import { CardModule } from 'primeng/card';
import { PaginatorModule, PaginatorState } from 'primeng/paginator';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import {
  Hospital,
  HospitalCardComponent,
} from '../../../shared/components/ui/search-result.component/components/hospital-card.component/hospital-card.component';
import { Comment } from '../../models/doctor.model';
import { PatientCommentComponent } from '../patient-comment-component/patient-comment-component';

@Component({
  selector: 'app-institution-page',
  imports: [
    CardModule,
    HospitalCardComponent,
    PatientCommentComponent,
    PaginatorModule,
  ],
  templateUrl: './institution-page.html',
  styleUrl: './institution-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InstitutionPage {
  protected translationService = inject(TranslationService);

  protected readonly first = signal(0);
  protected readonly rows = signal(5);
  protected readonly firstDoctors = signal(0);
  protected readonly rowsDoctors = signal(5);

  protected onDoctorPageChange(event: PaginatorState) {
    this.firstDoctors.set(event.first ?? 0);
    this.rowsDoctors.set(event.rows ?? 5);
  }
  protected onPageChange(event: PaginatorState) {
    this.first.set(event.first ?? 0);
    this.rows.set(event.rows ?? 5);
  }

  sampleHospital: Hospital = {
    id: '1',
    name: 'Szpital kliniczny',
    address: 'Jana Pawła II 25, 23-200 Lublin',
    specialisation: ['Oncologist', 'Cardiologist'],
    isPublic: true,
    imageUrl: 'assets/footer-landing.png',
  };

  comments: Comment[] = [
    {
      id: 1,
      userName: 'Alice Smith',
      visitedInstitution: 'City Hospital',
      content: 'Very professional and caring.',
      dateOfVisit: new Date('2024-03-15'),
      numberOfStars: 5,
    },
    {
      id: 2,
      userName: 'Bob Johnson',
      visitedInstitution: 'Health Clinic',
      content: 'Helpful and knowledgeable.',
      dateOfVisit: new Date('2024-02-10'),
      numberOfStars: 4,
    },
    {
      id: 1,
      userName: 'Alice Smith',
      visitedInstitution: 'City Hospital',
      content: 'Very professional and caring.',
      dateOfVisit: new Date('2024-03-15'),
      numberOfStars: 5,
    },
    {
      id: 2,
      userName: 'Bob Johnson',
      visitedInstitution: 'Health Clinic',
      content: 'Helpful and knowledgeable.',
      dateOfVisit: new Date('2024-02-10'),
      numberOfStars: 4,
    },
  ];

  protected readonly exampleDoctor = signal({
    name: 'John',
    surname: 'Doe',
    photoUrl: 'assets/footer-landing.png',
    pwz: '4175000',
    rating: {
      stars: 4.7,
      opinions: 120,
    },
    institutions: ['City Hospital', 'Health Clinic'],
    specialisation: ['Cardiology', 'Internal Medicine'],
    comments: [
      {
        id: 1,
        userName: 'Alice Smith',
        visitedInstitution: 'City Hospital',
        content: 'Very professional and caring.',
        dateOfVisit: new Date('2024-03-15'),
        numberOfStars: 5,
      },
      {
        id: 2,
        userName: 'Bob Johnson',
        visitedInstitution: 'Health Clinic',
        content: 'Helpful and knowledgeable.',
        dateOfVisit: new Date('2024-02-10'),
        numberOfStars: 4,
      },
      {
        id: 1,
        userName: 'Alice Smith',
        visitedInstitution: 'City Hospital',
        content: 'Very professional and caring.',
        dateOfVisit: new Date('2024-03-15'),
        numberOfStars: 5,
      },
      {
        id: 2,
        userName: 'Bob Johnson',
        visitedInstitution: 'Health Clinic',
        content: 'Helpful and knowledgeable.',
        dateOfVisit: new Date('2024-02-10'),
        numberOfStars: 4,
      },
    ],
  });
}
