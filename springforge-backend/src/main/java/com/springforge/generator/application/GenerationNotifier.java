package com.springforge.generator.application;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GenerationNotifier {

    private final SimpMessagingTemplate messagingTemplate;

    public GenerationNotifier(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyProgress(UUID generationId, String step, int progress) {
        GenerationProgressEvent event = GenerationProgressEvent.stepStarted(generationId, step, progress);
        messagingTemplate.convertAndSend("/topic/generation/" + generationId, event);
    }

    public void notifyCompleted(UUID generationId) {
        GenerationProgressEvent event = GenerationProgressEvent.completed(generationId);
        messagingTemplate.convertAndSend("/topic/generation/" + generationId, event);
    }

    public void notifyFailed(UUID generationId, String error) {
        GenerationProgressEvent event = GenerationProgressEvent.failed(generationId, error);
        messagingTemplate.convertAndSend("/topic/generation/" + generationId, event);
    }
}
