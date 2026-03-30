import { Routes } from '@angular/router';
import { Dashboard } from './components/dashboard/dashboard';
import { LoginComponent } from './components/login/login';
import { Upload } from './components/upload/upload';
import { Search } from './components/search/search';
import { Ask } from './components/ask/ask';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: Dashboard },
  { path: 'upload', component: Upload },
  { path: 'search', component: Search },
  { path: 'ask', component: Ask }
];

