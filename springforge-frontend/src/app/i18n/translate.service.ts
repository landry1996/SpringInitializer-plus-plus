import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TranslateService {
  private translations: Record<string, string> = {};
  private currentLocale$ = new BehaviorSubject<string>('en');
  private loaded = false;

  constructor(private http: HttpClient) {
    const stored = localStorage.getItem('springforge-locale');
    if (stored) {
      this.setLocale(stored);
    } else {
      this.loadTranslations('en');
    }
  }

  setLocale(locale: string): void {
    this.currentLocale$.next(locale);
    localStorage.setItem('springforge-locale', locale);
    this.loadTranslations(locale);
  }

  getCurrentLocale(): string {
    return this.currentLocale$.value;
  }

  getLocaleChanges(): Observable<string> {
    return this.currentLocale$.asObservable();
  }

  translate(key: string, params?: Record<string, string>): string {
    let value = this.translations[key] || key;
    if (params) {
      Object.entries(params).forEach(([k, v]) => {
        value = value.replace(`{${k}}`, v);
      });
    }
    return value;
  }

  private loadTranslations(locale: string): void {
    this.http.get<Record<string, string>>(`/assets/i18n/${locale}.json`).subscribe({
      next: (translations) => {
        this.translations = translations;
        this.loaded = true;
      },
      error: () => {
        if (locale !== 'en') {
          this.loadTranslations('en');
        }
      }
    });
  }
}
