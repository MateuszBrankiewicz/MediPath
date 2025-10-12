import { CommonModule } from '@angular/common';
import {
  Component,
  inject,
  input,
  OnInit,
  output,
  signal,
} from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinner } from 'primeng/progressspinner';
import { AddressFormatPipe } from '../../../../../../../core/pipes/address-format-pipe';
import { TranslationService } from '../../../../../../../core/services/translation/translation.service';
import {
  AddressChange,
  BookAppointment,
  DaySchedule,
  Doctor,
  TimeSlot,
} from '../../search-result.model';

@Component({
  selector: 'app-doctor-card',
  imports: [CommonModule, AddressFormatPipe, ButtonModule, ProgressSpinner],
  templateUrl: './doctor-card.component.html',
  styleUrl: './doctor-card.component.scss',
})
export class DoctorCardComponent implements OnInit {
  public readonly translationService = inject(TranslationService);

  public readonly doctor = input.required<Doctor>();
  public readonly bookAppointment = output<BookAppointment>();
  public readonly showMoreInfo = output<Doctor>();
  public readonly addressChange = output<AddressChange>();
  protected readonly selectedAddressIndex = signal(0);
  public isScheduleLoading = input(true);
  protected readonly MAX_VISIBLE_SLOTS = 4;

  protected readonly currentPages = signal<Map<string, number>>(new Map());

  ngOnInit(): void {
    this.selectedAddressIndex.set(this.doctor().currentAddressIndex || 0);
    console.log(this.doctor());
  }
  getStarsArray(): boolean[] {
    const stars = [];
    for (let i = 0; i < 5; i++) {
      stars.push(i < Math.floor(this.doctor().rating));
    }
    return stars;
  }

  onTimeSlotClick(day: DaySchedule, timeSlot: TimeSlot): void {
    if (timeSlot.available && !timeSlot.booked) {
      this.bookAppointment.emit({
        doctor: this.doctor(),
        day: day.date,
        time: timeSlot.time,
        slotId: timeSlot.id,
      });
    }
  }

  onMoreInfoClick(): void {
    this.showMoreInfo.emit(this.doctor());
  }

  onAddressTabClick(index: number): void {
    this.selectedAddressIndex.set(index);
    this.addressChange.emit({
      doctor: this.doctor(),
      addressIndex: index,
    });
  }

  getAvailableSlotsCount(day: DaySchedule): number {
    return day.slots.filter((slot) => slot.available && !slot.booked).length;
  }

  onShowMoreHours(day: DaySchedule): void {
    const pagesMap = new Map(this.currentPages());
    const currentPage = pagesMap.get(day.dayName) || 0;
    pagesMap.set(day.dayName, currentPage + 1);
    this.currentPages.set(pagesMap);
  }

  onShowPreviousHours(day: DaySchedule): void {
    const pagesMap = new Map(this.currentPages());
    const currentPage = pagesMap.get(day.dayName) || 0;
    if (currentPage > 0) {
      pagesMap.set(day.dayName, currentPage - 1);
      this.currentPages.set(pagesMap);
    }
  }

  getVisibleSlots(slots: TimeSlot[], dayName: string): TimeSlot[] {
    const currentPage = this.currentPages().get(dayName) || 0;
    const startIndex = currentPage * this.MAX_VISIBLE_SLOTS;
    const endIndex = startIndex + this.MAX_VISIBLE_SLOTS;
    return slots.slice(startIndex, endIndex);
  }

  hasMoreSlots(slots: TimeSlot[], dayName: string): boolean {
    const currentPage = this.currentPages().get(dayName) || 0;
    const startIndex = (currentPage + 1) * this.MAX_VISIBLE_SLOTS;
    return startIndex < slots.length;
  }

  hasPreviousSlots(dayName: string): boolean {
    const currentPage = this.currentPages().get(dayName) || 0;
    return currentPage > 0;
  }

  getRemainingSlotCount(slots: TimeSlot[], dayName: string): number {
    const currentPage = this.currentPages().get(dayName) || 0;
    const startIndex = (currentPage + 1) * this.MAX_VISIBLE_SLOTS;
    return Math.max(0, slots.length - startIndex);
  }

  getCurrentPageInfo(slots: TimeSlot[], dayName: string): string {
    const currentPage = this.currentPages().get(dayName) || 0;
    const startIndex = currentPage * this.MAX_VISIBLE_SLOTS;
    const endIndex = Math.min(
      startIndex + this.MAX_VISIBLE_SLOTS,
      slots.length,
    );
    return `${startIndex + 1}-${endIndex} z ${slots.length}`;
  }

  formatDayDate(day: DaySchedule): string {
    if (!day.date) {
      return '';
    }

    const date = typeof day.date === 'string' ? new Date(day.date) : day.date;

    if (!(date instanceof Date) || isNaN(date.getTime())) {
      return '';
    }

    const dayNumber = date.getDate();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');

    return `${dayNumber}.${month}`;
  }

  protected getSortedDays(): DaySchedule[] {
    return [...this.doctor().schedule]
      .sort((a, b) => {
        const dateA = typeof a.date === 'string' ? new Date(a.date) : a.date;
        const dateB = typeof b.date === 'string' ? new Date(b.date) : b.date;
        return dateA.getTime() - dateB.getTime();
      })
      .slice(0, 6);
  }
}
