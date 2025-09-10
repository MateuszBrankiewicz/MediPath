import { Routes } from '@angular/router';
import { LandingPageComponent } from './components/landing-page-component/landing-page-component';
import { SearchResultComponent } from '../shared/components/ui/search-result.component/search-result.component';

export const GUEST_ROUTES: Routes = [
  { path: '', component: LandingPageComponent },
  { path: 'search/:type/:query', component: SearchResultComponent },
];
