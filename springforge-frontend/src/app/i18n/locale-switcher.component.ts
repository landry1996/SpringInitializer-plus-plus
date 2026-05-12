import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateService } from './translate.service';

@Component({
  selector: 'app-locale-switcher',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="locale-switcher">
      <select [ngModel]="currentLocale" (ngModelChange)="switchLocale($event)">
        <option *ngFor="let locale of locales" [value]="locale.code">
          {{ locale.flag }} {{ locale.name }}
        </option>
      </select>
    </div>
  `,
  styles: [`
    .locale-switcher select {
      padding: 4px 8px;
      border: 1px solid #ddd;
      border-radius: 4px;
      background: white;
      cursor: pointer;
      font-size: 0.9rem;
    }
  `]
})
export class LocaleSwitcherComponent {
  currentLocale: string;

  locales = [
    { code: 'en', name: 'English', flag: '🇬🇧' },
    { code: 'fr', name: 'Français', flag: '🇫🇷' },
    { code: 'de', name: 'Deutsch', flag: '🇩🇪' },
    { code: 'es', name: 'Español', flag: '🇪🇸' }
  ];

  constructor(private translateService: TranslateService) {
    this.currentLocale = this.translateService.getCurrentLocale();
  }

  switchLocale(locale: string): void {
    this.currentLocale = locale;
    this.translateService.setLocale(locale);
  }
}
