import { CommonModule } from '@angular/common';
import { Component, input, OnInit, output, signal } from '@angular/core';
import {
  AddressChange,
  BookAppointment,
  DaySchedule,
  Doctor,
  TimeSlot,
} from '../../search-result.model';
import { AddressFormatPipe } from '../../../../../../../core/pipes/address-format-pipe';

@Component({
  selector: 'app-doctor-card',
  imports: [CommonModule, AddressFormatPipe],
  templateUrl: './doctor-card.component.html',
  styleUrl: './doctor-card.component.scss',
})
export class DoctorCardComponent implements OnInit {
  onShowMore() {
    throw new Error('Method not implemented.');
  }
  public readonly doctor = input.required<Doctor>();
  public readonly bookAppointment = output<BookAppointment>();
  public readonly showMoreInfo = output<Doctor>();
  public readonly addressChange = output<AddressChange>();
  protected readonly selectedAddressIndex = signal(0);

  ngOnInit(): void {
    this.selectedAddressIndex.set(this.doctor().currentAddressIndex || 0);
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
