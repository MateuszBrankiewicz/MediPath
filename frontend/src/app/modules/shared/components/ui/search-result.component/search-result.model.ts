export interface TimeSlot {
  id: string;
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
  id: string;
  name: string;
  specialisation: string;
  rating: number;
  reviewsCount: number;
  photoUrl: string;
  addresses: AddressWithInstitution[];
  currentAddressIndex: number;
  schedule: DaySchedule[];
}

export interface AddressWithInstitution {
  address: string;
  institution: string;
}

export interface BookAppointment {
  doctor: Doctor;
  day: string;
  time: string;
  slotId?: string;
}

export interface AddressChange {
  doctor: Doctor;
  addressIndex: number;
}
