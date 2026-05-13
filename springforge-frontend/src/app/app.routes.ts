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
  {
    path: 'settings/billing',
    loadComponent: () => import('./features/billing/billing.component').then(m => m.BillingComponent),
    canActivate: [authGuard]
  },
  {
    path: 'settings/webhooks',
    loadComponent: () => import('./features/webhooks/webhooks.component').then(m => m.WebhooksComponent),
    canActivate: [authGuard]
  },
  {
    path: 'ai/chat',
    loadComponent: () => import('./features/ai/ai-chat-panel.component').then(m => m.AiChatPanelComponent),
    canActivate: [authGuard]
  },
  { path: '**', redirectTo: '/wizard' }
];
