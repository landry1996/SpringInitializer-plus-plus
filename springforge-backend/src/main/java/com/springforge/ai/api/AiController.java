package com.springforge.ai.api;

import com.springforge.ai.application.AiAssistantService;
import com.springforge.ai.domain.LlmResponse;
import com.springforge.generator.domain.ProjectConfiguration;
import com.springforge.shared.security.AuthenticatedUser;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private final AiAssistantService aiService;

    public AiController(AiAssistantService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/review")
    public ResponseEntity<AiResponseDto> review(
            @RequestBody ProjectConfiguration config,
            @AuthenticationPrincipal AuthenticatedUser user) {
        LlmResponse response = aiService.reviewProject(config);
        return ResponseEntity.ok(AiResponseDto.from(response));
    }

    @PostMapping("/suggest")
    public ResponseEntity<AiResponseDto> suggest(
            @RequestBody ProjectConfiguration config,
            @AuthenticationPrincipal AuthenticatedUser user) {
        LlmResponse response = aiService.suggestImprovements(config);
        return ResponseEntity.ok(AiResponseDto.from(response));
    }

    @PostMapping("/generate")
    public ResponseEntity<AiResponseDto> generateCode(
            @RequestBody CodeGenerationRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        LlmResponse response = aiService.generateCode(request.description(), request.configuration());
        return ResponseEntity.ok(AiResponseDto.from(response));
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(
            @RequestBody ChatRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return aiService.streamChat(request.message(), request.configuration())
                .map(chunk -> "data: " + chunk + "\n\n");
    }

    public record CodeGenerationRequest(String description, ProjectConfiguration configuration) {}
    public record ChatRequest(String message, ProjectConfiguration configuration) {}
    public record AiResponseDto(String content, int tokensUsed) {
        public static AiResponseDto from(LlmResponse response) {
            return new AiResponseDto(response.content(), response.inputTokens() + response.outputTokens());
        }
    }
}
