import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AiReviewResult, AiRequest } from './ai.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AiService {
  private baseUrl = `${environment.apiUrl}/ai`;

  constructor(private http: HttpClient) {}

  review(projectConfig: any): Observable<AiReviewResult> {
    return this.http.post<AiReviewResult>(`${this.baseUrl}/review`, projectConfig);
  }

  suggest(projectConfig: any): Observable<AiReviewResult> {
    return this.http.post<AiReviewResult>(`${this.baseUrl}/suggest`, projectConfig);
  }

  generate(prompt: string, context?: string): Observable<{ content: string }> {
    return this.http.post<{ content: string }>(`${this.baseUrl}/generate`, { prompt, context });
  }

  streamChat(request: AiRequest): Observable<string> {
    return new Observable(subscriber => {
      const abortController = new AbortController();

      fetch(`${this.baseUrl}/chat`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
        },
        body: JSON.stringify(request),
        signal: abortController.signal
      }).then(async response => {
        if (!response.ok) {
          subscriber.error(new Error(`HTTP ${response.status}`));
          return;
        }
        const reader = response.body?.getReader();
        if (!reader) {
          subscriber.error(new Error('No response body'));
          return;
        }
        const decoder = new TextDecoder();
        let buffer = '';

        while (true) {
          const { done, value } = await reader.read();
          if (done) break;

          buffer += decoder.decode(value, { stream: true });
          const lines = buffer.split('\n');
          buffer = lines.pop() || '';

          for (const line of lines) {
            if (line.startsWith('data:')) {
              const data = line.slice(5).trim();
              if (data === '[DONE]') {
                subscriber.complete();
                return;
              }
              if (data) {
                subscriber.next(data);
              }
            }
          }
        }
        subscriber.complete();
      }).catch(err => {
        if (err.name !== 'AbortError') {
          subscriber.error(err);
        }
      });

      return () => abortController.abort();
    });
  }
}
