import { Component, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AiService } from './ai.service';
import { AiChatMessage } from './ai.model';

@Component({
  selector: 'app-ai-chat-panel',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="chat-container">
      <div class="chat-header">
        <h3>SpringForge AI Assistant</h3>
        <button class="btn-new" (click)="clearChat()">New Chat</button>
      </div>

      <div class="messages" #messagesContainer>
        <div *ngIf="messages.length === 0" class="welcome">
          <div class="welcome-icon">&#129302;</div>
          <h4>How can I help you?</h4>
          <p>Ask me about architecture, dependencies, best practices, or code generation.</p>
          <div class="suggestions">
            <button class="suggestion" (click)="sendSuggestion('Review my project configuration for potential issues')">Review my project</button>
            <button class="suggestion" (click)="sendSuggestion('Suggest improvements for a hexagonal architecture Spring Boot app')">Architecture tips</button>
            <button class="suggestion" (click)="sendSuggestion('Generate a REST controller with CRUD operations')">Generate code</button>
          </div>
        </div>

        <div *ngFor="let msg of messages" class="message" [class.user]="msg.role === 'user'" [class.assistant]="msg.role === 'assistant'">
          <div class="message-bubble">
            <div class="message-content" [innerHTML]="formatContent(msg.content)"></div>
            <div class="message-time">{{ msg.timestamp | date:'shortTime' }}</div>
          </div>
        </div>

        <div *ngIf="isStreaming" class="message assistant">
          <div class="message-bubble">
            <div class="message-content" [innerHTML]="formatContent(streamingContent)"></div>
            <span class="typing-cursor">|</span>
          </div>
        </div>
      </div>

      <div class="input-area">
        <textarea
          [(ngModel)]="inputText"
          (keydown.enter)="onEnter($event)"
          placeholder="Ask anything about your project..."
          rows="1"
          [disabled]="isStreaming"
        ></textarea>
        <button class="btn-send" (click)="send()" [disabled]="!inputText.trim() || isStreaming">
          <span *ngIf="!isStreaming">&#10148;</span>
          <span *ngIf="isStreaming" class="spinner"></span>
        </button>
      </div>
    </div>
  `,
  styles: [`
    .chat-container { display: flex; flex-direction: column; height: 100vh; max-width: 800px; margin: 0 auto; background: #fafafa; }
    .chat-header { display: flex; justify-content: space-between; align-items: center; padding: 1rem 1.5rem; background: white; border-bottom: 1px solid #e0e0e0; }
    .chat-header h3 { margin: 0; color: #1976d2; }
    .btn-new { padding: 0.4rem 1rem; background: #f5f5f5; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; font-size: 0.85rem; }
    .btn-new:hover { background: #e0e0e0; }
    .messages { flex: 1; overflow-y: auto; padding: 1.5rem; display: flex; flex-direction: column; gap: 1rem; }
    .welcome { text-align: center; margin-top: 3rem; }
    .welcome-icon { font-size: 3rem; margin-bottom: 1rem; }
    .welcome h4 { color: #333; margin: 0.5rem 0; }
    .welcome p { color: #666; }
    .suggestions { display: flex; flex-wrap: wrap; gap: 0.5rem; justify-content: center; margin-top: 1.5rem; }
    .suggestion { padding: 0.5rem 1rem; background: white; border: 1px solid #1976d2; color: #1976d2; border-radius: 20px; cursor: pointer; font-size: 0.85rem; }
    .suggestion:hover { background: #e3f2fd; }
    .message { display: flex; }
    .message.user { justify-content: flex-end; }
    .message.assistant { justify-content: flex-start; }
    .message-bubble { max-width: 75%; padding: 0.75rem 1rem; border-radius: 12px; }
    .message.user .message-bubble { background: #1976d2; color: white; border-bottom-right-radius: 4px; }
    .message.assistant .message-bubble { background: white; border: 1px solid #e0e0e0; border-bottom-left-radius: 4px; }
    .message-content { white-space: pre-wrap; word-break: break-word; line-height: 1.5; }
    .message-content code { background: rgba(0,0,0,0.06); padding: 2px 6px; border-radius: 4px; font-family: 'Fira Code', monospace; font-size: 0.85em; }
    .message-time { font-size: 0.7rem; opacity: 0.6; margin-top: 0.4rem; text-align: right; }
    .typing-cursor { animation: blink 1s infinite; font-weight: bold; }
    @keyframes blink { 0%, 50% { opacity: 1; } 51%, 100% { opacity: 0; } }
    .input-area { display: flex; gap: 0.5rem; padding: 1rem 1.5rem; background: white; border-top: 1px solid #e0e0e0; }
    .input-area textarea { flex: 1; padding: 0.75rem 1rem; border: 1px solid #ddd; border-radius: 8px; resize: none; font-size: 0.95rem; font-family: inherit; outline: none; }
    .input-area textarea:focus { border-color: #1976d2; }
    .btn-send { width: 44px; height: 44px; background: #1976d2; color: white; border: none; border-radius: 50%; cursor: pointer; display: flex; align-items: center; justify-content: center; font-size: 1.2rem; }
    .btn-send:disabled { background: #bbb; cursor: not-allowed; }
    .spinner { width: 16px; height: 16px; border: 2px solid white; border-top-color: transparent; border-radius: 50%; animation: spin 0.8s linear infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class AiChatPanelComponent {
  @ViewChild('messagesContainer') messagesContainer!: ElementRef;

  messages: AiChatMessage[] = [];
  inputText = '';
  isStreaming = false;
  streamingContent = '';

  constructor(private aiService: AiService) {}

  send(): void {
    const text = this.inputText.trim();
    if (!text || this.isStreaming) return;

    this.messages.push({ role: 'user', content: text, timestamp: new Date() });
    this.inputText = '';
    this.isStreaming = true;
    this.streamingContent = '';
    this.scrollToBottom();

    const context = this.messages.slice(-6).map(m => `${m.role}: ${m.content}`).join('\n');

    this.aiService.streamChat({ prompt: text, context }).subscribe({
      next: (token) => {
        this.streamingContent += token;
        this.scrollToBottom();
      },
      error: () => {
        this.messages.push({ role: 'assistant', content: this.streamingContent || 'Sorry, an error occurred.', timestamp: new Date() });
        this.isStreaming = false;
        this.streamingContent = '';
      },
      complete: () => {
        this.messages.push({ role: 'assistant', content: this.streamingContent, timestamp: new Date() });
        this.isStreaming = false;
        this.streamingContent = '';
        this.scrollToBottom();
      }
    });
  }

  sendSuggestion(text: string): void {
    this.inputText = text;
    this.send();
  }

  onEnter(event: Event): void {
    const keyEvent = event as KeyboardEvent;
    if (!keyEvent.shiftKey) {
      keyEvent.preventDefault();
      this.send();
    }
  }

  clearChat(): void {
    this.messages = [];
    this.streamingContent = '';
    this.isStreaming = false;
  }

  formatContent(content: string): string {
    return content
      .replace(/`([^`]+)`/g, '<code>$1</code>')
      .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
      .replace(/\n/g, '<br>');
  }

  private scrollToBottom(): void {
    setTimeout(() => {
      if (this.messagesContainer) {
        const el = this.messagesContainer.nativeElement;
        el.scrollTop = el.scrollHeight;
      }
    }, 50);
  }
}
