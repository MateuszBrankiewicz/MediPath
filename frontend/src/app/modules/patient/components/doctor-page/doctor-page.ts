import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import { CardModule } from 'primeng/card';

@Component({
  selector: 'app-doctor-page',
  imports: [CardModule, BreadcrumbModule],
  templateUrl: './doctor-page.html',
  styleUrl: './doctor-page.scss',
})
export class DoctorPage {
  private activatedRoute = inject(ActivatedRoute);
  protected readonly doctorId = signal<string | null>(null);
  protected readonly homeItem = signal<MenuItem>({
    label: 'Doctors',
    routerLink: '/patient/doctors/',
    icon: '',
  });
  public constructor() {
    this.activatedRoute.paramMap.subscribe((params) => {
      this.doctorId.set(params.get('dcotorId'));
    });
  }
  protected readonly breadcumbMenuItems = computed<MenuItem[]>(() => {
    return [
      {
        label: 'Twoj stary',
      },
    ];
  });

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
        userName: 'Alice Smith',
        visitedInstitution: 'City Hospital',
        content: 'Very professional and caring.',
        dateOfVisit: new Date('2024-03-15'),
        numberOfStars: 5,
      },
      {
        userName: 'Bob Johnson',
        visitedInstitution: 'Health Clinic',
        content: 'Helpful and knowledgeable.',
        dateOfVisit: new Date('2024-02-10'),
        numberOfStars: 4,
      },
    ],
  });
}
