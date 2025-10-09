export interface Reminder {
  id: string;
  date: Date;
  hour: string;
  title: string;
  description: string;
}

export interface MedicationReminder {
  id?: number;
  title: string;
  reminderTime: Date | null;
  startDate: Date | null;
  endDate: Date | null;
  content: string;
}
