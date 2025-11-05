import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  effect,
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
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TabsModule } from 'primeng/tabs';
import { Textarea } from 'primeng/textarea';
import { DoctorService } from '../../../../core/services/doctor/doctor.service';
import { groupSchedulesByDate } from '../../../../utils/scheduleMapper';

import { DoctorPageModel } from '../../../../core/models/doctor.model';
import { InstitutionShortInfo } from '../../../../core/models/institution.model';
import {
  AvailableDay,
  ScheduleByInstitutionResponse,
  ScheduleItem,
} from '../../../../core/models/schedule.model';
import { CommentService } from '../../../../core/services/comment/comment.service';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { VisitsService } from '../../../../core/services/visits/visits.service';
import { CalendarSchedule } from '../../../shared/components/calendar-schedule/calendar-schedule';
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
    ProgressSpinnerModule,
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
  private commentService = inject(CommentService);
  private toastService = inject(ToastService);
  private translationService = inject(TranslationService);
  protected readonly homeItem = signal<MenuItem>({
    label: 'Doctors',
    routerLink: '/patient/doctors/',
    icon: '',
  });

  private visitsService = inject(VisitsService);

  private isLCommentsLoading = signal(false);
  protected isScheduleLoading = signal(false);
  private isDoctorInfoLoading = signal(false);
  protected readonly isLoading = computed<boolean>(() => {
    const isCommentsLoading = this.isLCommentsLoading();
    const isDoctorInfoLoading = this.isDoctorInfoLoading();
    return isCommentsLoading || isDoctorInfoLoading;
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
          this.doctorInfo.set(doctor);
          this.selectInstitution(this.doctorInfo().institutions[0]);
        });
      this.initDoctorComments();
      this.initDoctorSchedule();
    }
  }
  protected readonly breadcumbMenuItems = computed<MenuItem[]>(() => {
    return [
      {
        label: 'Twoj stary',
      },
    ];
  });

  protected readonly mappedSchedule = computed<AvailableDay[]>(() => {
    return this.doctorInfo().schedule.map((daySchedule) => ({
      date: daySchedule.date,
      slots: daySchedule.slots.map((slot) => ({
        ...slot,
        booked: slot.booked ?? false,
      })),
    }));
  });

  protected selectedTabIndex = signal(0);
  protected setSelectedTabIndex(index: number): void {
    this.selectedTabIndex.set(index);
  }
  protected selectedId = signal<string | null>(null);

  protected selectSlotId(event: {
    date: Date;
    time: string;
    slotId?: string;
  }): void {
    this.selectedId.set(event.slotId ?? null);
  }

  protected readonly doctorInfo = signal<DoctorPageModel>({
    name: '',
    surname: '',
    photoUrl: '',
    pwz: '',
    rating: {
      stars: 0,
      opinions: 0,
    },
    institutions: [],
    specialisation: [],
    schedule: [],
    comments: [],
  });
  protected selectedInstitution = signal<InstitutionShortInfo | null>(null);
  protected selectInstitution(institution: InstitutionShortInfo): void {
    this.selectedInstitution.set(institution);
  }

  private initDoctorComments() {
    this.isLCommentsLoading.set(true);
    this.commentService
      .getCommentByDoctor(this.doctorId() ?? '')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((comments) => {
        const currentDoctor = this.doctorInfo();
        this.doctorInfo.set({
          ...currentDoctor,
          comments: comments,
        });
        this.isLCommentsLoading.set(false);
      });
  }

  private initDoctorSchedule() {
    this.isScheduleLoading.set(true);
    this.doctorService
      .getDoctorScheduleByInstitution(
        this.selectedInstitution()?.institutionId || '',
        this.doctorId() || '',
      )
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((response: ScheduleByInstitutionResponse) => {
        const newSchedule = this.mapAndGroupSchedules(response.schedules);
        this.doctorInfo.update((doctor) => ({
          ...doctor,
          schedule: newSchedule,
        }));
        this.isScheduleLoading.set(false);
      });
  }

  constructor() {
    effect(() => {
      this.initDoctorSchedule();
    });
  }

  private mapAndGroupSchedules(
    schedules: ScheduleByInstitutionResponse['schedules'],
  ): ReturnType<typeof groupSchedulesByDate> {
    const mappedSchedules: ScheduleItem[] = schedules.map((schedule) => ({
      id: schedule.id,
      startTime: schedule.startHour,
      isBooked: schedule.isBooked,
    }));
    return groupSchedulesByDate(mappedSchedules);
  }

  protected bookVisit(): void {
    this.visitsService
      .scheduleVisit({
        scheduleID: this.selectedId() || '',
        patientRemarks: this.patientRemarks() || '',
      })
      .subscribe({
        next: () => {
          this.patientRemarks.set('');
          this.initDoctorSchedule();
          this.toastService.showSuccess('patient.appointment.bookSuccess');
        },
        error: () => {
          this.toastService.showError('patient.appointment.bookError');
        },
      });
  }
}
