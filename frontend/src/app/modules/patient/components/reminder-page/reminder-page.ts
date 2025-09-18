import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { DialogService, DynamicDialogRef } from 'primeng/dynamicdialog';
import { PanelModule } from 'primeng/panel';
import { TooltipModule } from 'primeng/tooltip';
import { AddReminderDialog } from './components/add-reminder-dialog/add-reminder-dialog';

interface Reminder {
  id: string;
  date: Date;
  hour: string;
  title: string;
  description: string;
}

@Component({
  selector: 'app-reminder-page',
  imports: [PanelModule, ButtonModule, DatePipe, TooltipModule],
  templateUrl: './reminder-page.html',
  styleUrl: './reminder-page.scss',
  providers: [DialogService],
})
export class ReminderPage {
  private dialogService = inject(DialogService);
  private ref: DynamicDialogRef | undefined;

  reminders = signal<Reminder[]>([
    {
      id: '1',
      date: new Date('2024-06-01'),
      hour: '08:00',
      title: 'Morning Medication',
      description: 'Take blood pressure medicine.',
    },
    {
      id: '2',
      date: new Date('2024-06-01'),
      hour: '20:00',
      title: 'Evening Medication',
      description: 'Take cholesterol medicine.',
    },
    {
      id: '3',
      date: new Date('2024-06-02'),
      hour: '12:30',
      title: 'Lunch Supplement',
      description: 'Take vitamin D supplement with food.',
    },
  ]);

  protected openMenuId = signal<string | null>(null);

  protected addNewEntry(): void {
    this.ref = this.dialogService.open(AddReminderDialog, {
      header: 'Add New Reminder',
      width: '70%',
      height: 'auto',
      closable: true,
      modal: true,
      styleClass: 'add-reminder-dialog',
    });

    this.ref.onClose.subscribe((result) => {
      if (result) {
        console.log('Visit rescheduled:', result);
      }
    });
  }

  protected deleteReminder(reminderId: string): void {
    const currentReminders = this.reminders();
    this.reminders.set(
      currentReminders.filter((reminder) => reminder.id !== reminderId),
    );
  }

  protected viewReminder(reminder: Reminder): void {
    console.log('Viewing reminder:', reminder);
    // TODO: Implement view reminder functionality
  }

  protected getTodayReminders(): number {
    const today = new Date();
    const todayString = today.toDateString();

    return this.reminders().filter(
      (reminder) => reminder.date.toDateString() === todayString,
    ).length;
  }

  protected editReminder(reminder: Reminder): void {
    console.log('Editing reminder:', reminder);
  }

  protected toggleMenu(event: Event, reminder: Reminder): void {
    event.stopPropagation();
    const currentId = this.openMenuId();
    if (currentId === reminder.id) {
      this.openMenuId.set(null);
    } else {
      this.openMenuId.set(reminder.id);
    }
  }

  isMenuOpen(reminderId: string): boolean {
    return this.openMenuId() === reminderId;
  }
}
