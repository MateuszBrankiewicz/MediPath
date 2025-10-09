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

    const valid = (v: unknown) => !!v && v !== 'null';

    if (valid(street)) {
      // street buildingNumber PostalCode City, Province
      const addressParts: string[] = [];
      if (valid(street)) {
        addressParts.push(street);
      }
      if (valid(buildingNumber)) {
        addressParts.push(buildingNumber);
      }
      if (valid(postalCode)) {
        addressParts.push(postalCode);
      }
      if (valid(city)) {
        addressParts.push(city);
      }
      let result = addressParts.join(' ');
      if (valid(province) && province !== city) {
        result += `, ${province}`;
      }
      return result;
    } else {
      // city buildingNumber PostalCode City Province
      const addressParts: string[] = [];
      if (valid(city)) {
        addressParts.push(city);
      }
      if (valid(buildingNumber)) {
        addressParts.push(buildingNumber);
      }
      if (valid(postalCode)) {
        addressParts.push(postalCode);
      }
      if (valid(city)) {
        addressParts.push(city);
      }
      if (valid(province) && province !== city) {
        addressParts.push(province);
      }
      return addressParts.join(' ');
    }
  }
}
