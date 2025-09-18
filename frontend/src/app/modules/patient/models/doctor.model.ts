export interface DoctorPageModel {
  name: string;
  surname: string;
  photoUrl: string;
  pwz: string;
  rating: {
    stars: number;
    opinions: number;
  };
  institutions: string[];
  specialisation: string[];
  comments: Comment[];
}
export interface Comment {
  id: number;
  userName: string;
  visitedInstitution: string;
  content: string;
  dateOfVisit: Date;
  numberOfStars: number;
}
