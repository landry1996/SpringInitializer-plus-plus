import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface BlueprintDraft {
  id?: string;
  name: string;
  description: string;
  category: string;
  version: string;
  tags: string[];
  dependencies: string[];
  files: { path: string; type: string }[];
  variables: { name: string; type: string; defaultValue: string }[];
}

@Injectable({
  providedIn: 'root'
})
export class BlueprintEditorService {
  private apiUrl = `${environment.apiUrl}/api/v1/marketplace/blueprints`;

  constructor(private http: HttpClient) {}

  load(id: string): Observable<BlueprintDraft> {
    return this.http.get<BlueprintDraft>(`${this.apiUrl}/${id}`);
  }

  save(draft: BlueprintDraft): Observable<BlueprintDraft> {
    if (draft.id) {
      return this.http.put<BlueprintDraft>(`${this.apiUrl}/${draft.id}`, draft);
    }
    return this.http.post<BlueprintDraft>(this.apiUrl, draft);
  }

  export(draft: BlueprintDraft): string {
    return JSON.stringify(draft, null, 2);
  }

  import(json: string): BlueprintDraft {
    return JSON.parse(json);
  }
}
