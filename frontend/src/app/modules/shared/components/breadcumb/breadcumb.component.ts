import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { Breadcrumb } from 'primeng/breadcrumb';
import { BreadcrumbService } from './breadcumb.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-breadcumb-component',
  imports: [Breadcrumb],
  templateUrl: './breadcumb.component.html',
  styleUrl: './breadcumb.component.scss',
})
export class BreadcumbComponent implements OnInit {
  private breadcrumbService = inject(BreadcrumbService);
  private destroyRef = inject(DestroyRef);
  protected readonly breadCrumbItems = signal<MenuItem[]>([]);
  ngOnInit() {
    this.breadcrumbService.breadcrumb$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((items) => {
        this.breadCrumbItems.set(items);
      });
  }
}
