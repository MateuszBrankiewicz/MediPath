import { Component, signal, inject } from '@angular/core';
import { CardModule } from 'primeng/card';
import {
  Hospital,
  HospitalCardComponent,
} from '../../../shared/components/ui/search-result.component/components/hospital-card.component/hospital-card.component';
import { PatientCommentComponent } from '../patient-comment-component/patient-comment-component';
import { Comment } from '../../models/doctor.model';
import { TranslationService } from '../../../../core/services/translation/translation.service';

@Component({
  selector: 'app-institution-page',
  imports: [CardModule, HospitalCardComponent, PatientCommentComponent],
  templateUrl: './institution-page.html',
  styleUrl: './institution-page.scss',
})
export class InstitutionPage {
  protected translationService = inject(TranslationService);

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
