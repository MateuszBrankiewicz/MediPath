import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { FloatLabel } from 'primeng/floatlabel';
import { MultiSelect } from 'primeng/multiselect';
import { ScheduleService } from '../../../../core/services/schedule/schedule.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { Appointment } from '../../../doctor/components/doctor-schedule/doctor-schedule';
import { Calendar } from '../../../shared/components/calendar/calendar';

@Component({
  selector: 'app-institution-schedule',
  imports: [Calendar, MultiSelect, ButtonModule, FloatLabel],
  templateUrl: './institution-schedule.html',
  styleUrl: './institution-schedule.scss',
})
export class InstitutionSchedule implements OnInit {
  protected translationService = inject(TranslationService);
  public currentMonth = signal<Date>(new Date());
  public readonly currentDayNumber = new Date().getDate();
  public readonly currentDayName = this.getDayName(new Date());
  private router = inject(Router);
  private scheduleService = inject(ScheduleService);
  ngOnInit(): void {
    this.loadToDayAppointments();
  }

  public todayAppointments: Appointment[] = [
    {
      id: '1',
      time: '8:00 am',
      patientName: 'Jan Nowak',
      status: 'booked',
    },
    {
      id: '2',
      time: '10:00 am',
      patientName: 'Michał Kowalski',
      status: 'booked',
    },
    {
      id: '3',
      time: '3:00 pm',
      patientName: 'Adam Brzęk',
      status: 'booked',
    },
  ];

  public readonly currentMonthYear = computed(() => {
    const current = this.currentMonth();
    const monthKeys = [
      'months.january',
      'months.february',
      'months.march',
      'months.april',
      'months.may',
      'months.june',
      'months.july',
      'months.august',
      'months.september',
      'months.october',
      'months.november',
      'months.december',
    ];
    const monthName = this.translationService.translate(
      monthKeys[current.getMonth()],
    );
    return `${monthName} ${current.getFullYear()}`;
  });
  private getDayName(date: Date): string {
    const dayKeys = [
      'weekdays.sunday',
      'weekdays.monday',
      'weekdays.tuesday',
      'weekdays.wednesday',
      'weekdays.thursday',
      'weekdays.friday',
      'weekdays.saturday',
    ];
    return this.translationService.translate(dayKeys[date.getDay()]);
  }

  protected saveSchedule(): void {
    this.router.navigate(['/admin/schedule/add-schedule']);
  }
}
