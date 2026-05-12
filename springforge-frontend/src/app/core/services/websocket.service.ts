import { Injectable, signal } from '@angular/core';
import { environment } from '../../../environments/environment';

export interface GenerationProgress {
  generationId: string;
  step: string;
  progress: number;
  message: string;
  status: string;
}

@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private socket: WebSocket | null = null;
  private stompConnected = false;

  progress = signal<GenerationProgress | null>(null);

  connect(generationId: string): void {
    const wsUrl = environment.wsUrl || 'ws://localhost:8080/ws';
    this.socket = new WebSocket(`${wsUrl}/websocket`);

    this.socket.onopen = () => {
      this.stompConnected = true;
      this.sendStompConnect();
      this.subscribeToGeneration(generationId);
    };

    this.socket.onmessage = (event) => {
      this.handleStompMessage(event.data);
    };

    this.socket.onerror = () => {
      this.stompConnected = false;
    };

    this.socket.onclose = () => {
      this.stompConnected = false;
    };
  }

  disconnect(): void {
    if (this.socket && this.stompConnected) {
      this.socket.send('DISCONNECT\n\n\0');
      this.socket.close();
    }
    this.socket = null;
    this.stompConnected = false;
  }

  private sendStompConnect(): void {
    const frame = 'CONNECT\naccept-version:1.2\nhost:localhost\n\n\0';
    this.socket?.send(frame);
  }

  private subscribeToGeneration(generationId: string): void {
    const frame = `SUBSCRIBE\nid:sub-0\ndestination:/topic/generation/${generationId}\n\n\0`;
    setTimeout(() => this.socket?.send(frame), 100);
  }

  private handleStompMessage(data: string): void {
    if (!data.startsWith('MESSAGE')) return;
    const bodyStart = data.indexOf('\n\n');
    if (bodyStart === -1) return;
    const body = data.substring(bodyStart + 2).replace('\0', '');
    try {
      const event: GenerationProgress = JSON.parse(body);
      this.progress.set(event);
    } catch {
      // ignore non-JSON frames
    }
  }
}
