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
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TabsModule } from 'primeng/tabs';
import { Textarea } from 'primeng/textarea';
import { DoctorService } from '../../../../core/services/doctor/doctor.service';
import { groupSchedulesByDate } from '../../../../utils/scheduleMapper';

import { DoctorPageModel } from '../../../../core/models/doctor.model';
import {
  AvailableDay,
  ScheduleByInstitutionResponse,
  ScheduleItem,
} from '../../../../core/models/schedule.model';
import { CommentService } from '../../../../core/services/comment/comment.service';
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
  protected readonly homeItem = signal<MenuItem>({
    label: 'Doctors',
    routerLink: '/patient/doctors/',
    icon: '',
  });

  private isLCommentsLoading = signal(false);
  private isScheduleLoading = signal(false);

  protected readonly isLoading = computed<boolean>(() => {
    const isCommentsLoading = this.isLCommentsLoading();
    const isScheduleLoading = this.isScheduleLoading();
    return isCommentsLoading || isScheduleLoading;
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
    return this.exampleDoctor().schedule.map((daySchedule) => ({
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
    comments: [],
    schedule: [],
  });
  protected selectedInstitution = signal<string | null>(null);
  protected selectInstitution(institution: string): void {
    this.selectedInstitution.set(institution);
  }

  private initDoctorComments() {
    this.isLCommentsLoading.set(true);
    this.commentService
      .getCommentByDoctor(this.doctorId() ?? '')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((comments) => {
        const currentDoctor = this.exampleDoctor();
        console.log(currentDoctor);
        console.log(comments);
        this.exampleDoctor.set({
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
        '68c5dc05d2569d07e73a8456',
        this.doctorId() || '',
      )
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((response: ScheduleByInstitutionResponse) => {
        const newSchedule = this.mapAndGroupSchedules(response.schedules);
        this.exampleDoctor.update((doctor) => ({
          ...doctor,
          schedule: newSchedule,
        }));
        this.isScheduleLoading.set(false);
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
}
