export function getCorrectDayFormat(date: Date): string {
  const month: number = date.getMonth() + 1;
  const day: number = date.getDate();
  const year: number = date.getFullYear();

  const formattedDay: string = day < 10 ? '0' + day : day.toString();
  const formattedMonth: string = month < 10 ? '0' + month : month.toString();

  return `${formattedDay}-${formattedMonth}-${year}`;
}
