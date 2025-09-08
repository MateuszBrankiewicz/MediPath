import { Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Button } from 'primeng/button';
import { Card } from 'primeng/card';
import { Menubar } from 'primeng/menubar';

enum LandingPageSelectedTab {
  FOR_INSTITUTIONS = 'for_institutions',
  FOR_DOCTORS = 'for_doctors',
  FOR_PATIENTS = 'for_patients',
}

@Component({
  selector: 'app-landing-page-component',
  imports: [Menubar, Button, Card, RouterLink],
  templateUrl: './landing-page-component.html',
  styleUrl: './landing-page-component.scss',
})
export class LandingPageComponent {
  protected readonly selectedTab = signal(
    LandingPageSelectedTab.FOR_INSTITUTIONS,
  );
}
