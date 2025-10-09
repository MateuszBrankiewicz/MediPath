export interface StarRatingOption {
  label: string;
  value: number;
  stars: string;
}

export interface CommentResponse {
  id: string;
  doctor: string;
  institution: string;
  doctorRating: number;
  institutionRating: number;
  content: string;
}
export interface CommentApiResponse {
  comments: CommentResponse[];
}
export interface AddComentRequest {
  visitID: string;
  comment: string;
  doctorRating: string;
  institutionRating: string;
}

export interface InstitutionCommentResponse {
  comments: {
    id: string;
    author: string;
    doctor: string;
    institution: string;
    doctorRating: number;
    institutionRating: number;
    content: string;
    createdAt: string;
  }[];
}

export interface InstitutionComment {
  id: string;
  author: string;
  doctor: string;
  institution: string;
  doctorRating: number;
  institutionRating: number;
  content: string;
  createdAt: string;
}

export interface CommentWithRating {
  id: string;
  comment: string;
  doctorName: string;
  institutionName: string;
  doctorRating: number;
  institutionRating: number;
}
