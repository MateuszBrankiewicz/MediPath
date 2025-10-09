import { DaySchedule } from '../../modules/shared/components/ui/search-result.component/search-result.model';

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
  schedule: DaySchedule[];

  comments: Comment[];
}
export interface Comment {
  id: string;
  userName: string;
  visitedInstitution: string;
  content: string;
  dateOfVisit: Date;
  numberOfStars: number;
}
