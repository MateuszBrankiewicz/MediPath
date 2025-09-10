import { Component } from '@angular/core';
import { TableModule } from 'primeng/table';
import { VisitPageModel, VisitStatus } from './visit-page.model';
import { CommonModule } from '@angular/common';
import { MenuItem } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { MenuModule } from 'primeng/menu';
import { PopoverModule } from 'primeng/popover';

@Component({
  selector: 'app-visit-page',
  imports: [TableModule, CommonModule, MenuModule, ButtonModule, PopoverModule],
  templateUrl: './visit-page.html',
  styleUrl: './visit-page.scss',
})
export class VisitPage {
  protected readonly items: MenuItem[] = [
    {
      label: 'RESCHEDULE',
      command: () => {
        console.log('RESCHEDULE');
      },
    },
    {
      label: 'CANCEL',
      command: () => {
        console.log('CANCEL');
      },
    },
  ];

  visits: VisitPageModel[] = [
    {
      id: 1,
      doctorName: 'Dr. Smith',
      institution: 'City Hospital',
      date: new Date('2024-06-01'),
      status: VisitStatus.Scheduled,
    },
    {
      id: 2,
      doctorName: 'Dr. Johnson',
      institution: 'Health Clinic',
      date: new Date('2024-05-20'),
      status: VisitStatus.Completed,
    },
    {
      id: 3,
      doctorName: 'Dr. Lee',
      institution: 'Downtown Medical Center',
      date: new Date('2024-06-10'),
      status: VisitStatus.Canceled,
    },
  ];

  protected cancelVisit() {
    console.log('cancel');
  }

  protected editVisit() {
    console.log('edit');
  }
}
