import { Component, inject, OnInit } from '@angular/core';
import { InstitutionService } from '../../../../core/services/institution/institution.service';

@Component({
  selector: 'app-admin-institution',
  imports: [],
  templateUrl: './admin-institution.html',
  styleUrl: './admin-institution.scss',
})
export class AdminInstitution implements OnInit {
  private institutionService = inject(InstitutionService);

  ngOnInit(): void {
    this.institutionService.getInstitutionsForAdmin().subscribe();
  }
}
