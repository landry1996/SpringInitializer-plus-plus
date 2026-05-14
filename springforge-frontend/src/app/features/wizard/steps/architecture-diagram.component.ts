import { Component, computed, ElementRef, ViewChild, AfterViewInit, OnChanges, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WizardStateService } from '../wizard-state.service';

interface DiagramNode {
  id: string;
  label: string;
  x: number;
  y: number;
  type: 'service' | 'gateway' | 'registry' | 'database' | 'broker' | 'config';
  databases?: string[];
}

interface DiagramEdge {
  from: string;
  to: string;
  type: 'sync' | 'async';
  label: string;
}

@Component({
  selector: 'app-architecture-diagram',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="diagram-container">
      <h3>Architecture Diagram</h3>
      <div class="diagram-legend">
        <span class="legend-item"><span class="legend-line solid"></span> Synchronous</span>
        <span class="legend-item"><span class="legend-line dashed"></span> Asynchronous</span>
      </div>
      <svg [attr.width]="svgWidth" [attr.height]="svgHeight" class="diagram-svg">
        <defs>
          <marker id="arrowhead" markerWidth="10" markerHeight="7" refX="10" refY="3.5" orient="auto">
            <polygon points="0 0, 10 3.5, 0 7" fill="#666"/>
          </marker>
          <marker id="arrowhead-async" markerWidth="10" markerHeight="7" refX="10" refY="3.5" orient="auto">
            <polygon points="0 0, 10 3.5, 0 7" fill="#ff9800"/>
          </marker>
        </defs>

        @for (edge of edges(); track edge.from + edge.to) {
          <line
            [attr.x1]="getNode(edge.from)?.x! + 75"
            [attr.y1]="getNode(edge.from)?.y! + 25"
            [attr.x2]="getNode(edge.to)?.x! + 75"
            [attr.y2]="getNode(edge.to)?.y! + 25"
            [attr.stroke]="edge.type === 'sync' ? '#1976d2' : '#ff9800'"
            [attr.stroke-dasharray]="edge.type === 'async' ? '5,5' : ''"
            stroke-width="2"
            [attr.marker-end]="edge.type === 'sync' ? 'url(#arrowhead)' : 'url(#arrowhead-async)'"
          />
          <text
            [attr.x]="(getNode(edge.from)?.x! + getNode(edge.to)?.x! + 150) / 2"
            [attr.y]="(getNode(edge.from)?.y! + getNode(edge.to)?.y! + 50) / 2 - 8"
            font-size="10" fill="#888" text-anchor="middle">
            {{ edge.label }}
          </text>
        }

        @for (node of nodes(); track node.id) {
          <g>
            <rect
              [attr.x]="node.x"
              [attr.y]="node.y"
              width="150"
              height="50"
              [attr.rx]="node.type === 'database' ? 15 : 8"
              [attr.fill]="getNodeColor(node.type)"
              stroke="#ddd"
              stroke-width="1"
            />
            <text
              [attr.x]="node.x + 75"
              [attr.y]="node.y + 22"
              font-size="11"
              font-weight="bold"
              fill="white"
              text-anchor="middle"
              dominant-baseline="middle">
              {{ node.label }}
            </text>
            @if (node.type === 'service') {
              <text
                [attr.x]="node.x + 75"
                [attr.y]="node.y + 38"
                font-size="9"
                fill="rgba(255,255,255,0.8)"
                text-anchor="middle">
                {{ getNodeIcon(node.type) }}
              </text>
            }
            @if (node.databases && node.databases.length > 0) {
              @for (db of node.databases; track db; let i = $index) {
                <rect
                  [attr.x]="node.x + 155"
                  [attr.y]="node.y + (i * 22)"
                  width="80"
                  height="18"
                  rx="9"
                  fill="#f3e5f5"
                  stroke="#ce93d8"
                  stroke-width="1"
                />
                <text
                  [attr.x]="node.x + 195"
                  [attr.y]="node.y + (i * 22) + 12"
                  font-size="9"
                  fill="#6a1b9a"
                  text-anchor="middle">
                  {{ db }}
                </text>
              }
            }
          </g>
        }
      </svg>
    </div>
  `,
  styles: [`
    .diagram-container { margin: 1.5rem 0; padding: 1rem; background: #fafafa; border-radius: 8px; border: 1px solid #e0e0e0; overflow-x: auto; }
    .diagram-container h3 { margin: 0 0 0.75rem; font-size: 0.9rem; color: #666; text-transform: uppercase; }
    .diagram-legend { display: flex; gap: 1.5rem; margin-bottom: 1rem; }
    .legend-item { display: flex; align-items: center; gap: 0.5rem; font-size: 0.8rem; color: #666; }
    .legend-line { display: inline-block; width: 30px; height: 2px; }
    .legend-line.solid { background: #1976d2; }
    .legend-line.dashed { background: repeating-linear-gradient(90deg, #ff9800 0, #ff9800 5px, transparent 5px, transparent 10px); }
    .diagram-svg { display: block; }
  `]
})
export class ArchitectureDiagramComponent {
  svgWidth = 800;
  svgHeight = 500;

  constructor(private wizardState: WizardStateService) {}

  nodes = computed(() => this.computeNodes());
  edges = computed(() => this.computeEdges());

  getNode(id: string): DiagramNode | undefined {
    return this.nodes().find(n => n.id === id);
  }

  getNodeColor(type: string): string {
    switch (type) {
      case 'gateway': return '#1565c0';
      case 'registry': return '#2e7d32';
      case 'config': return '#6a1b9a';
      case 'broker': return '#e65100';
      case 'database': return '#4527a0';
      default: return '#37474f';
    }
  }

  getNodeIcon(type: string): string {
    switch (type) {
      case 'gateway': return 'Gateway';
      case 'registry': return 'Registry';
      case 'config': return 'Config';
      case 'broker': return 'Broker';
      default: return 'Service';
    }
  }

  private computeNodes(): DiagramNode[] {
    const arch = this.wizardState.state().architecture;
    const nodes: DiagramNode[] = [];

    if (arch.type === 'MICROSERVICES' && arch.microservices) {
      const ms = arch.microservices;

      nodes.push({ id: 'registry', label: 'Service Registry', x: 300, y: 20, type: 'registry' });
      nodes.push({ id: 'gateway', label: 'API Gateway', x: 300, y: 100, type: 'gateway' });

      if (ms.centralizedConfig?.configServer) {
        nodes.push({ id: 'config', label: 'Config Server', x: 550, y: 20, type: 'config' });
      }

      const hasBroker = ms.asyncCommunications && ms.asyncCommunications.length > 0;
      if (hasBroker) {
        const brokerType = ms.asyncCommunications[0].broker;
        nodes.push({ id: 'broker', label: brokerType, x: 550, y: 100, type: 'broker' });
      }

      const serviceCount = ms.services.length;
      const startY = 200;
      const cols = Math.min(serviceCount, 3);
      ms.services.forEach((svc, i) => {
        const col = i % cols;
        const row = Math.floor(i / cols);
        nodes.push({
          id: svc.name,
          label: svc.name,
          x: 80 + col * 250,
          y: startY + row * 100,
          type: 'service',
          databases: svc.databases?.map(db => db.type) || []
        });
      });

      this.svgWidth = Math.max(800, cols * 250 + 200);
      this.svgHeight = Math.max(400, startY + Math.ceil(serviceCount / cols) * 100 + 80);

    } else if (arch.type === 'MODULITH' && arch.modulith) {
      arch.modulith.modules.forEach((mod, i) => {
        const col = i % 3;
        const row = Math.floor(i / 3);
        nodes.push({ id: mod.name, label: mod.name, x: 80 + col * 220, y: 50 + row * 90, type: 'service' });
      });
      this.svgWidth = Math.max(600, Math.min(arch.modulith.modules.length, 3) * 220 + 160);
      this.svgHeight = Math.max(300, Math.ceil(arch.modulith.modules.length / 3) * 90 + 120);

    } else if (arch.type === 'DDD' && arch.ddd) {
      arch.ddd.boundedContexts.forEach((ctx, i) => {
        const col = i % 3;
        const row = Math.floor(i / 3);
        nodes.push({ id: ctx.name, label: ctx.name, x: 80 + col * 220, y: 50 + row * 100, type: 'service' });
      });
      this.svgWidth = Math.max(600, Math.min(arch.ddd.boundedContexts.length, 3) * 220 + 160);
      this.svgHeight = Math.max(300, Math.ceil(arch.ddd.boundedContexts.length / 3) * 100 + 120);

    } else {
      const modules = arch.modules || [];
      modules.forEach((mod, i) => {
        const col = i % 3;
        const row = Math.floor(i / 3);
        nodes.push({ id: mod, label: mod, x: 80 + col * 220, y: 50 + row * 90, type: 'service' });
      });
      this.svgWidth = Math.max(600, Math.min(modules.length, 3) * 220 + 160);
      this.svgHeight = Math.max(250, Math.ceil(modules.length / 3) * 90 + 120);
    }

    return nodes;
  }

  private computeEdges(): DiagramEdge[] {
    const arch = this.wizardState.state().architecture;
    const edges: DiagramEdge[] = [];

    if (arch.type === 'MICROSERVICES' && arch.microservices) {
      const ms = arch.microservices;

      ms.services.forEach(svc => {
        edges.push({ from: 'gateway', to: svc.name, type: 'sync', label: '' });
      });

      if (ms.syncCommunications) {
        ms.syncCommunications.forEach(comm => {
          edges.push({ from: comm.source, to: comm.target, type: 'sync', label: comm.protocol });
        });
      }

      if (ms.asyncCommunications) {
        ms.asyncCommunications.forEach(comm => {
          edges.push({ from: comm.source, to: comm.target, type: 'async', label: comm.topic });
        });
      }

    } else if (arch.type === 'DDD' && arch.ddd) {
      arch.ddd.contextMapping?.forEach(rel => {
        edges.push({ from: rel.source, to: rel.target, type: 'sync', label: rel.relationType });
      });

    } else if (arch.type === 'MODULITH' && arch.modulith) {
      arch.modulith.allowedDependencies?.filter(d => d.allowed).forEach(dep => {
        edges.push({ from: dep.from, to: dep.to, type: 'sync', label: '' });
      });
      arch.modulith.internalEvents?.forEach(evt => {
        evt.subscriberModules.forEach(sub => {
          edges.push({ from: evt.publisherModule, to: sub, type: 'async', label: evt.name });
        });
      });
    }

    return edges;
  }
}
