export interface AiChatMessage {
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
}

export interface AiReviewResult {
  content: string;
  suggestions: string[];
}

export interface AiRequest {
  prompt: string;
  context?: string;
  model?: string;
  temperature?: number;
  maxTokens?: number;
}
