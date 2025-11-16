export interface AddDoctorRequest {
  userDetails: {
    name: string;
    email: string;
    surname: string;
    govID: string;
    birthDate: string;
    province: string;
    city: string;
    postalCode: string;
    phoneNumber: string;
    street: string;
    number: string;
  };
  licenceNumber: string;
  employeeDetails: {
    roleCode: number;
    specialisations: string[];
  };
}
