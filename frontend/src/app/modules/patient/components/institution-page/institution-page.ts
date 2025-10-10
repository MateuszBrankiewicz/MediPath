import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CardModule } from 'primeng/card';
import { PaginatorModule, PaginatorState } from 'primeng/paginator';
import { RatingModule } from 'primeng/rating';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import {
  Hospital,
  HospitalCardComponent,
} from '../../../shared/components/ui/search-result.component/components/hospital-card.component/hospital-card.component';

import { Comment } from '../../../../core/models/doctor.model';
import { Institution } from '../../../../core/models/institution.model';
import { CommentService } from '../../../../core/services/comment/comment.service';
import { InstitutionService } from '../../../../core/services/institution/institution.service';
import { PatientCommentComponent } from '../patient-comment-component/patient-comment-component';

@Component({
  selector: 'app-institution-page',
  imports: [
    CardModule,
    HospitalCardComponent,
    PatientCommentComponent,
    PaginatorModule,
    RatingModule,
    FormsModule,
  ],
  templateUrl: './institution-page.html',
  styleUrl: './institution-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InstitutionPage implements OnInit {
  protected translationService = inject(TranslationService);
  protected readonly institution = signal<Institution | null>(null);
  protected readonly first = signal(0);
  protected readonly rows = signal(5);
  protected readonly firstDoctors = signal(0);

  protected readonly rowsDoctors = signal(5);

  protected readonly comments = signal<Comment[]>([]);

  private destroyRef = inject(DestroyRef);
  private institutionService = inject(InstitutionService);
  private commentService = inject(CommentService);
  private activatedRoue = inject(ActivatedRoute);
  private router = inject(Router);
  protected onDoctorPageChange(event: PaginatorState) {
    this.firstDoctors.set(event.first ?? 0);
    this.rowsDoctors.set(event.rows ?? 5);
  }
  protected onPageChange(event: PaginatorState) {
    this.first.set(event.first ?? 0);
    this.rows.set(event.rows ?? 5);
  }

  protected readonly exampleDoctor = signal({
    name: 'John',
    surname: 'Doe',
    photoUrl: 'assets/footer-landing.png',
    pwz: '4175000',
    rating: {
      stars: 4.7,
      opinions: 120,
    },
    institutions: ['City Hospital', 'Health Clinic'],
    specialisation: ['Cardiology', 'Internal Medicine'],
    comments: [
      {
        id: 1,
        userName: 'Alice Smith',
        visitedInstitution: 'City Hospital',
        content: 'Very professional and caring.',
        dateOfVisit: new Date('2024-03-15'),
        numberOfStars: 5,
      },
      {
        id: 2,
        userName: 'Bob Johnson',
        visitedInstitution: 'Health Clinic',
        content: 'Helpful and knowledgeable.',
        dateOfVisit: new Date('2024-02-10'),
        numberOfStars: 4,
      },
      {
        id: 1,
        userName: 'Alice Smith',
        visitedInstitution: 'City Hospital',
        content: 'Very professional and caring.',
        dateOfVisit: new Date('2024-03-15'),
        numberOfStars: 5,
      },
      {
        id: 2,
        userName: 'Bob Johnson',
        visitedInstitution: 'Health Clinic',
        content: 'Helpful and knowledgeable.',
        dateOfVisit: new Date('2024-02-10'),
        numberOfStars: 4,
      },
    ],
  });

  ngOnInit(): void {
    this.activatedRoue.paramMap.subscribe((params) => {
      const id = params.get('id');
      if (id) {
        this.initInstitution(id);
        this.getCommentsForInstitution(id);
      }
    });
  }

  private initInstitution(id: string): void {
    this.institutionService
      .getInstitution(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((val) => {
        this.institution.set(val);
      });
  }

  protected getDataForInstitutionCard = computed<Hospital>(() => {
    const inst = this.institution();
    console.log(inst?.image);
    return {
      id: inst?.id ?? '',
      name: inst?.name ?? '',
      address: inst?.address
        ? `${inst.address.street} ${inst.address.number} ${inst.address.postalCode} ${inst.address.city}, ${inst.address.province}`
        : '',
      specialisation: inst?.specialisation ?? [],
      isPublic: inst?.isPublic ?? false,
      imageUrl: inst?.image ?? '',
    };
  });

  protected getCommentsForInstitution(institutionId: string): void {
    this.commentService
      .getCommentByInstitution(institutionId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((val) => {
        this.comments.set(val);
      });
  }

  protected onDoctorCardClick(doctorId: string): void {
    this.router.navigate(['/patient/doctor', doctorId]);
    console.log('Doctor card clicked:', doctorId);
  }
}
