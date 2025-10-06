import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TabsModule } from 'primeng/tabs';
import { Textarea } from 'primeng/textarea';
import { CalendarSchedule } from '../../../shared/components/calendar-schedule/calendar-schedule';
import { DoctorPageModel } from '../../models/doctor.model';
import { DoctorService } from '../../services/doctor.service';
import { PatientCommentComponent } from '../patient-comment-component/patient-comment-component';

@Component({
  imports: [
    CardModule,
    BreadcrumbModule,
    PatientCommentComponent,
    TabsModule,
    CalendarSchedule,
    ButtonModule,
    FormsModule,
    Textarea,
  ],
  templateUrl: './doctor-page.html',
  styleUrl: './doctor-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DoctorPage implements OnInit {
  private activatedRoute = inject(ActivatedRoute);
  private destroyRef = inject(DestroyRef);
  protected readonly doctorId = signal<string | null>(null);
  private doctorService = inject(DoctorService);
  protected readonly patientRemarks = signal<string>('');
  protected readonly homeItem = signal<MenuItem>({
    label: 'Doctors',
    routerLink: '/patient/doctors/',
    icon: '',
  });
  ngOnInit(): void {
    this.activatedRoute.paramMap.subscribe((params) => {
      this.doctorId.set(params.get('id'));
    });
    if (this.doctorId()) {
      this.doctorService
        .getDoctorDetails(this.doctorId() ?? '')
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe((doctor) => {
          console.log(doctor);
        });
    }
  }
  protected readonly breadcumbMenuItems = computed<MenuItem[]>(() => {
    return [
      {
        label: 'Twoj stary',
      },
    ];
  });

  protected selectedTabIndex = signal(0);
  protected setSelectedTabIndex(index: number): void {
    this.selectedTabIndex.set(index);
  }

  protected readonly exampleDoctor = signal<DoctorPageModel>({
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
  protected selectedInstitution = signal<string | null>(null);
  protected selectInstitution(institution: string): void {
    this.selectedInstitution.set(institution);
  }
}
