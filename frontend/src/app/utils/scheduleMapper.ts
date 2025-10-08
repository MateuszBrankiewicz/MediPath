export function groupSchedulesByDate(
  schedules: { startTime: string; isBooked: boolean; id: string }[],
) {
  const grouped = new Map<
    string,
    { startTime: string; isBooked: boolean; id: string }[]
  >();

  schedules.forEach((slot) => {
    const date = new Date(slot.startTime);
    const dateKey = date.toISOString().split('T')[0];

    if (!grouped.has(dateKey)) {
      grouped.set(dateKey, []);
    }
    grouped.get(dateKey)!.push(slot);
  });

  return Array.from(grouped.entries()).map(([dateKey, slots]) => {
    const date = new Date(dateKey);
    const dayNames = [
      'Niedziela',
      'Poniedziałek',
      'Wtorek',
      'Środa',
      'Czwartek',
      'Piątek',
      'Sobota',
    ];

    return {
      date: dateKey,
      dayName: dayNames[date.getDay()],
      dayNumber: date.getDate().toString(),
      slots: slots.map((slot) => {
        const slotDate = new Date(slot.startTime);
        const timeString = slotDate.toLocaleTimeString('pl-PL', {
          hour: '2-digit',
          minute: '2-digit',
          hour12: false,
        });

        return {
          time: timeString,
          available: !slot.isBooked,
          booked: slot.isBooked,
          id: slot.id,
        };
      }),
    };
  });
}
