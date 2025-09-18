import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'addressFormat',
  standalone: true,
})
export class AddressFormatPipe implements PipeTransform {
  transform(value: string): string {
    if (!value || typeof value !== 'string') {
      return '';
    }

    const parts = value.split(',').map((part) => part.trim());

    if (parts.length < 5) {
      return value;
    }

    const [province, city, street, buildingNumber, postalCode] = parts;

    const addressParts: string[] = [];

    if (
      street &&
      street !== 'null' &&
      buildingNumber &&
      buildingNumber !== 'null'
    ) {
      addressParts.push(`${street} ${buildingNumber}`);
    } else if (street && street !== 'null') {
      addressParts.push(street);
    }

    if (city && city !== 'null') {
      addressParts.push(city);
    }

    if (province && province !== 'null' && province !== city) {
      addressParts.push(province);
    }

    if (postalCode && postalCode !== 'null') {
      addressParts.push(postalCode);
    }

    return addressParts.join(', ');
  }
}
