import { Injectable, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class BreadcrumbService {
  private router = inject(Router);
  private activatedRoute = inject(ActivatedRoute);

  private breadcrumbSubject = new BehaviorSubject<MenuItem[]>([]);
  public breadcrumb$ = this.breadcrumbSubject.asObservable();

  constructor() {
    this.router.events.subscribe(() => {
      this.updateBreadcrumb();
    });
  }

  private updateBreadcrumb(): void {
    const items: MenuItem[] = [];

    items.push({ label: 'Home', routerLink: '/' });

    const urlSegments = this.router.url.split('/').filter((segment) => segment);

    let currentPath = '';
    urlSegments.forEach((segment, index) => {
      currentPath += `/${segment}`;

      const label = this.mapSegmentToLabel(segment, urlSegments[index + 1]);

      items.push({
        label: label,
        routerLink: currentPath,
      });
    });

    this.breadcrumbSubject.next(items);
  }

  private mapSegmentToLabel(segment: string, nextSegment?: string): string {
    switch (segment) {
      case 'search':
        return 'Search';
      case 'institution':
        return `Institution: "${nextSegment || ''}"`;
      case 'doctor':
        return `Doctor: "${nextSegment || ''}"`;
      case 'patient':
        return 'Patient Dashboard';
      case 'admin':
        return 'Admin Dashboard';
      default:
        return this.capitalize(segment);
    }
  }

  private capitalize(str: string): string {
    return str.charAt(0).toUpperCase() + str.slice(1);
  }

  public setBreadcrumb(items: MenuItem[]): void {
    this.breadcrumbSubject.next(items);
  }
}
