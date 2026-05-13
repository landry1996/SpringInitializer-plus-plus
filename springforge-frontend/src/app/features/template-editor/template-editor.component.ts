import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { BlueprintEditorService, BlueprintDraft } from './blueprint-editor.service';

@Component({
  selector: 'app-template-editor',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="editor-container">
      <header class="editor-header">
        <h1>{{ isNew ? 'New Blueprint' : 'Edit Blueprint' }}</h1>
        <div class="header-actions">
          <button class="btn btn-secondary" (click)="exportBlueprint()">Export</button>
          <button class="btn btn-secondary" (click)="importBlueprint()">Import</button>
          <button class="btn btn-primary" (click)="saveBlueprint()" [disabled]="!isValid">
            {{ isNew ? 'Publish to Marketplace' : 'Save Changes' }}
          </button>
        </div>
      </header>

      <div class="editor-body">
        <aside class="editor-sidebar">
          <nav class="section-nav">
            <button [class.active]="activeSection === 'metadata'" (click)="activeSection = 'metadata'">Metadata</button>
            <button [class.active]="activeSection === 'dependencies'" (click)="activeSection = 'dependencies'">Dependencies</button>
            <button [class.active]="activeSection === 'files'" (click)="activeSection = 'files'">File Structure</button>
            <button [class.active]="activeSection === 'variables'" (click)="activeSection = 'variables'">Variables</button>
            <button [class.active]="activeSection === 'raw'" (click)="activeSection = 'raw'">Raw YAML</button>
          </nav>

          <div class="validation-panel" *ngIf="validationErrors.length > 0">
            <h4>Validation Issues</h4>
            <ul>
              <li *ngFor="let error of validationErrors" class="error">{{ error }}</li>
            </ul>
          </div>
        </aside>

        <main class="editor-main">
          <!-- Metadata Section -->
          <section *ngIf="activeSection === 'metadata'" class="section">
            <h2>Blueprint Metadata</h2>
            <div class="form-group">
              <label>Name</label>
              <input [(ngModel)]="draft.name" placeholder="My Custom Blueprint" (ngModelChange)="validate()" />
            </div>
            <div class="form-group">
              <label>Description</label>
              <textarea [(ngModel)]="draft.description" rows="3" placeholder="Describe what this blueprint generates..." (ngModelChange)="validate()"></textarea>
            </div>
            <div class="form-group">
              <label>Category</label>
              <select [(ngModel)]="draft.category">
                <option value="ARCHITECTURE">Architecture</option>
                <option value="MICROSERVICE">Microservice</option>
                <option value="FULLSTACK">Full Stack</option>
                <option value="DATA_PIPELINE">Data Pipeline</option>
                <option value="DEVOPS">DevOps</option>
              </select>
            </div>
            <div class="form-group">
              <label>Tags (comma separated)</label>
              <input [(ngModel)]="tagsInput" placeholder="spring, hexagonal, ddd" />
            </div>
            <div class="form-group">
              <label>Version</label>
              <input [(ngModel)]="draft.version" placeholder="1.0.0" />
            </div>
          </section>

          <!-- Dependencies Section -->
          <section *ngIf="activeSection === 'dependencies'" class="section">
            <h2>Dependencies</h2>
            <p class="hint">Select the dependencies this blueprint will include by default.</p>
            <div class="dep-grid">
              <label *ngFor="let dep of availableDependencies" class="dep-item">
                <input type="checkbox" [checked]="isDependencySelected(dep.id)" (change)="toggleDependency(dep.id)" />
                <span class="dep-name">{{ dep.name }}</span>
                <span class="dep-desc">{{ dep.description }}</span>
              </label>
            </div>
          </section>

          <!-- File Structure Section -->
          <section *ngIf="activeSection === 'files'" class="section">
            <h2>File Structure</h2>
            <p class="hint">Define the files that will be generated. Use FreeMarker syntax for templates.</p>
            <div class="file-list">
              <div *ngFor="let file of draft.files; let i = index" class="file-item">
                <input [(ngModel)]="file.path" placeholder="src/main/java/com/example/Service.java" class="file-path" />
                <select [(ngModel)]="file.type">
                  <option value="template">Template</option>
                  <option value="static">Static</option>
                </select>
                <button class="btn-icon" (click)="removeFile(i)">&#x2715;</button>
              </div>
            </div>
            <button class="btn btn-secondary btn-sm" (click)="addFile()">+ Add File</button>
          </section>

          <!-- Variables Section -->
          <section *ngIf="activeSection === 'variables'" class="section">
            <h2>Template Variables</h2>
            <p class="hint">Define variables that users can customize when using this blueprint.</p>
            <div class="var-list">
              <div *ngFor="let v of draft.variables; let i = index" class="var-item">
                <input [(ngModel)]="v.name" placeholder="variableName" />
                <select [(ngModel)]="v.type">
                  <option value="string">String</option>
                  <option value="boolean">Boolean</option>
                  <option value="select">Select</option>
                </select>
                <input [(ngModel)]="v.defaultValue" placeholder="Default value" />
                <button class="btn-icon" (click)="removeVariable(i)">&#x2715;</button>
              </div>
            </div>
            <button class="btn btn-secondary btn-sm" (click)="addVariable()">+ Add Variable</button>
          </section>

          <!-- Raw YAML Section -->
          <section *ngIf="activeSection === 'raw'" class="section">
            <h2>Raw Configuration (YAML)</h2>
            <textarea class="raw-editor" [(ngModel)]="rawYaml" rows="30" (ngModelChange)="onRawYamlChange()"></textarea>
          </section>
        </main>

        <aside class="preview-panel">
          <h3>Preview</h3>
          <div class="preview-tree">
            <pre>{{ previewStructure }}</pre>
          </div>
        </aside>
      </div>
    </div>
  `,
  styles: [`
    .editor-container { display: flex; flex-direction: column; height: 100vh; }
    .editor-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 24px; border-bottom: 1px solid #e0e0e0; background: #fafafa; }
    .header-actions { display: flex; gap: 8px; }
    .editor-body { display: flex; flex: 1; overflow: hidden; }
    .editor-sidebar { width: 200px; border-right: 1px solid #e0e0e0; padding: 16px; background: #f5f5f5; }
    .section-nav button { display: block; width: 100%; text-align: left; padding: 8px 12px; border: none; background: none; cursor: pointer; border-radius: 4px; margin-bottom: 4px; }
    .section-nav button.active { background: #1976d2; color: white; }
    .editor-main { flex: 1; padding: 24px; overflow-y: auto; }
    .preview-panel { width: 280px; border-left: 1px solid #e0e0e0; padding: 16px; background: #f9f9f9; overflow-y: auto; }
    .preview-tree pre { font-size: 12px; white-space: pre-wrap; }
    .form-group { margin-bottom: 16px; }
    .form-group label { display: block; margin-bottom: 4px; font-weight: 500; }
    .form-group input, .form-group textarea, .form-group select { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; }
    .dep-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
    .dep-item { display: flex; align-items: flex-start; gap: 8px; padding: 8px; border: 1px solid #eee; border-radius: 4px; }
    .dep-name { font-weight: 500; }
    .dep-desc { font-size: 12px; color: #666; }
    .file-item, .var-item { display: flex; gap: 8px; margin-bottom: 8px; align-items: center; }
    .file-path { flex: 1; }
    .btn { padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer; }
    .btn-primary { background: #1976d2; color: white; }
    .btn-secondary { background: #e0e0e0; }
    .btn-sm { padding: 4px 12px; font-size: 13px; }
    .btn-icon { background: none; border: none; cursor: pointer; font-size: 16px; color: #999; }
    .raw-editor { width: 100%; font-family: monospace; font-size: 13px; padding: 12px; border: 1px solid #ddd; border-radius: 4px; }
    .validation-panel { margin-top: 16px; padding: 8px; background: #fff3e0; border-radius: 4px; }
    .validation-panel .error { color: #e65100; font-size: 12px; }
    .hint { color: #666; font-size: 13px; margin-bottom: 12px; }
    h1 { margin: 0; font-size: 20px; }
  `]
})
export class TemplateEditorComponent implements OnInit {
  activeSection = 'metadata';
  isNew = true;
  draft: BlueprintDraft = {
    name: '',
    description: '',
    category: 'ARCHITECTURE',
    version: '1.0.0',
    tags: [],
    dependencies: [],
    files: [],
    variables: []
  };
  tagsInput = '';
  rawYaml = '';
  validationErrors: string[] = [];
  previewStructure = '';

  availableDependencies = [
    { id: 'spring-boot-starter-web', name: 'Web', description: 'Spring MVC' },
    { id: 'spring-boot-starter-data-jpa', name: 'JPA', description: 'Hibernate ORM' },
    { id: 'spring-boot-starter-data-mongodb', name: 'MongoDB', description: 'Spring Data MongoDB' },
    { id: 'spring-boot-starter-security', name: 'Security', description: 'Spring Security' },
    { id: 'spring-boot-starter-validation', name: 'Validation', description: 'Bean Validation' },
    { id: 'spring-boot-starter-actuator', name: 'Actuator', description: 'Health & Metrics' },
    { id: 'spring-boot-starter-data-redis', name: 'Redis', description: 'Redis Cache' },
    { id: 'spring-boot-starter-websocket', name: 'WebSocket', description: 'Real-time' },
    { id: 'spring-kafka', name: 'Kafka', description: 'Event streaming' },
  ];

  get isValid(): boolean {
    return this.validationErrors.length === 0 && this.draft.name.length > 0;
  }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private editorService: BlueprintEditorService
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isNew = false;
      this.editorService.load(id).subscribe(blueprint => {
        this.draft = blueprint;
        this.tagsInput = blueprint.tags.join(', ');
        this.updatePreview();
      });
    }
    this.validate();
  }

  validate() {
    this.validationErrors = [];
    if (!this.draft.name || this.draft.name.length < 3) {
      this.validationErrors.push('Name must be at least 3 characters');
    }
    if (!this.draft.description || this.draft.description.length < 10) {
      this.validationErrors.push('Description must be at least 10 characters');
    }
    this.updatePreview();
  }

  isDependencySelected(id: string): boolean {
    return this.draft.dependencies.includes(id);
  }

  toggleDependency(id: string) {
    const idx = this.draft.dependencies.indexOf(id);
    if (idx >= 0) {
      this.draft.dependencies.splice(idx, 1);
    } else {
      this.draft.dependencies.push(id);
    }
    this.updatePreview();
  }

  addFile() {
    this.draft.files.push({ path: '', type: 'template' });
  }

  removeFile(index: number) {
    this.draft.files.splice(index, 1);
    this.updatePreview();
  }

  addVariable() {
    this.draft.variables.push({ name: '', type: 'string', defaultValue: '' });
  }

  removeVariable(index: number) {
    this.draft.variables.splice(index, 1);
  }

  onRawYamlChange() {
    // Raw YAML editing - parse changes
  }

  updatePreview() {
    const lines: string[] = [];
    lines.push(`${this.draft.name || 'my-project'}/`);
    lines.push('├── src/main/java/');
    lines.push('│   └── com/example/');
    for (const file of this.draft.files) {
      if (file.path) {
        lines.push(`│       └── ${file.path}`);
      }
    }
    lines.push('├── src/main/resources/');
    lines.push('│   └── application.yml');
    if (this.draft.dependencies.includes('spring-boot-starter-data-mongodb')) {
      lines.push('├── docker-compose.yml (MongoDB)');
    } else if (this.draft.dependencies.includes('spring-boot-starter-data-jpa')) {
      lines.push('├── docker-compose.yml (PostgreSQL)');
    }
    lines.push('├── pom.xml');
    lines.push('└── README.md');
    this.previewStructure = lines.join('\n');
  }

  saveBlueprint() {
    this.draft.tags = this.tagsInput.split(',').map(t => t.trim()).filter(Boolean);
    this.editorService.save(this.draft).subscribe({
      next: () => this.router.navigate(['/marketplace']),
      error: (err) => this.validationErrors.push('Save failed: ' + err.message)
    });
  }

  exportBlueprint() {
    this.draft.tags = this.tagsInput.split(',').map(t => t.trim()).filter(Boolean);
    const json = JSON.stringify(this.draft, null, 2);
    const blob = new Blob([json], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${this.draft.name || 'blueprint'}.json`;
    a.click();
    URL.revokeObjectURL(url);
  }

  importBlueprint() {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.json,.yaml,.yml';
    input.onchange = (event: any) => {
      const file = event.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = (e: any) => {
          try {
            const imported = JSON.parse(e.target.result);
            this.draft = { ...this.draft, ...imported };
            this.tagsInput = (this.draft.tags || []).join(', ');
            this.validate();
          } catch {
            this.validationErrors.push('Invalid file format');
          }
        };
        reader.readAsText(file);
      }
    };
    input.click();
  }
}
