export interface InstitutionResponse {
  institution: {
    address: {
      province: string;
      number: string;
      postalCode: string;
      city: string;
      street?: string;
    };
    employees: {
      name: string;
      pfpImage: Base64URLString;
      roleCode: number;
      specialisations: string[];
      surname: string;
      userId: string;
    }[];
    id: string;
    image: Base64URLString;
    isPublic: boolean;
    name: string;
    rating: number;
    types: string[];
  };
}
export interface Institution {
  address: {
    province: string;
    number: string;
    postalCode: string;
    city: string;
    street?: string;
  };
  employees: {
    name: string;
    pfpImage: Base64URLString;
    roleCode: number;
    specialisation: string[];
    surname: string;
    userId: string;
  }[];
  id: string;
  image: Base64URLString;
  isPublic: boolean;
  name: string;
  rating: number;
  specialisation: string[];
}

export interface InstitutionShortInfo {
  institutionId: string;
  institutionName: string;
}
