export interface TimeSlot {
  time: string;
  available: boolean;
  booked?: boolean;
}

export interface DaySchedule {
  date: string;
  dayName: string;
  dayNumber: string;
  slots: TimeSlot[];
}

export interface Doctor {
  id: number;
  name: string;
  specialisation: string;
  rating: number;
  reviewsCount: number;
  photoUrl: string;
  addresses: string[];
  currentAddressIndex: number;
  schedule: DaySchedule[];
}

export interface BookAppointment {
  doctor: Doctor;
  day: string;
  time: string;
}

export interface AddressChange {
  doctor: Doctor;
  addressIndex: number;
}
