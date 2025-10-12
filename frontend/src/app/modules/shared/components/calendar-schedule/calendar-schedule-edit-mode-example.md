# Calendar Schedule - Edit Mode Example

## Tryb edycji z istniejącymi slotami

W trybie edycji komponent może otrzymać dane `availableAppointments` podobnie jak w trybie normalnym. Różnica polega na tym, że dni z istniejącymi slotami **nie będą dostępne** do zaznaczenia.

### Przykład użycia

```typescript
import { Component, signal } from "@angular/core";
import { CalendarSchedule } from "./calendar-schedule";
import { AvailableDay } from "../../../../core/models/schedule.model";

@Component({
  selector: "app-doctor-schedule-management",
  template: ` <app-calendar-schedule [editMode]="true" [availableAppointments]="existingSchedule()" (scheduleTimeSelected)="onNewTimeSlotSelected($event)" /> `,
  imports: [CalendarSchedule],
})
export class DoctorScheduleManagementComponent {
  // Existing schedule data with blocked days
  existingSchedule = signal<AvailableDay[]>([
    {
      date: new Date(2025, 9, 15), // 15 października 2025
      slots: [
        { id: "1", time: "09:00", available: true },
        { id: "2", time: "10:00", available: false },
        { id: "3", time: "11:00", available: true },
      ],
    },
    {
      date: new Date(2025, 9, 16), // 16 października 2025
      slots: [
        { id: "4", time: "14:00", available: true },
        { id: "5", time: "15:00", available: true },
      ],
    },
  ]);

  onNewTimeSlotSelected(event: { date: Date; startTime: string; endTime: string; customTime?: string }) {
    console.log("New time slot selected:", event);

    // Tutaj można dodać logikę do zapisania nowego slotu w systemie
    // np. wywołanie serwisu API
    this.saveNewTimeSlot(event);
  }

  private saveNewTimeSlot(slot: { date: Date; startTime: string; endTime: string; customTime?: string }) {
    // Implementacja zapisywania nowego slotu
    console.log("Saving new slot to backend...", slot);
  }
}
```

### Jak to działa

1. **Dni z istniejącymi slotami**: Są wyświetlane z czerwonym tłem i małym "×" w rogu, nie można ich kliknąć
2. **Dni bez slotów**: Są dostępne do zaznaczenia (szare tło, klikalne)
3. **Dni z innych miesięcy**: Są wyszarzone i nie można ich kliknąć
4. **Po wybraniu dnia**: Pokazują się opcje wyboru godzin rozpoczęcia i zakończenia

### Wizualne wskazówki

- 🟢 **Dostępne dni**: Szare tło, czarna czcionka, klikalne
- 🔴 **Zablokowane dni (z istniejącymi slotami)**: Czerwone tło, czerwona czcionka, "×" w rogu, nie klikalne
- ⚫ **Inne miesiące**: Wyszarzone, nie klikalne

### API Wydarzenia

#### scheduleTimeSelected

```typescript
{
  date: Date;           // Wybrana data
  startTime: string;    // Godzina rozpoczęcia (np. "09:00")
  endTime: string;      // Godzina zakończenia (np. "17:00")
  customTime?: string;  // Opcjonalny custom czas jeśli został użyty
}
```

### Właściwości komponentu

```typescript
// Włącza tryb edycji
editMode: boolean = true;

// Dane istniejących terminów (dni z slotami będą zablokowane)
availableAppointments: AvailableDay[] = [...];
```
