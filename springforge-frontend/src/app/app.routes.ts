import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/wizard', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'wizard',
    loadComponent: () => import('./features/wizard/wizard.component').then(m => m.WizardComponent),
    canActivate: [authGuard]
  },
  {
    path: 'editor/new',
    loadComponent: () => import('./features/template-editor/template-editor.component').then(m => m.TemplateEditorComponent),
    canActivate: [authGuard]
  },
  {
    path: 'editor/:id',
    loadComponent: () => import('./features/template-editor/template-editor.component').then(m => m.TemplateEditorComponent),
    canActivate: [authGuard]
  },
  { path: '**', redirectTo: '/wizard' }
];
