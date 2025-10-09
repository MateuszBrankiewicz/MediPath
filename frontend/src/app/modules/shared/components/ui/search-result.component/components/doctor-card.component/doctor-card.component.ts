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
    console.log('Show more hours for', day.dayName);
  }
}
