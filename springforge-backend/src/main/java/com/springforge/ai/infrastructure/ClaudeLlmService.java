package com.springforge.ai.infrastructure;

import com.springforge.ai.domain.LlmProvider;
import com.springforge.ai.domain.LlmRequest;
import com.springforge.ai.domain.LlmResponse;
import com.springforge.ai.domain.LlmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "claude", matchIfMissing = true)
public class ClaudeLlmService implements LlmService {

    private static final Logger log = LoggerFactory.getLogger(ClaudeLlmService.class);

    private final WebClient webClient;
    private final String model;

    public ClaudeLlmService(
            @Value("${app.ai.claude.api-key:}") String apiKey,
            @Value("${app.ai.claude.model:claude-sonnet-4-6-20250514}") String model) {
        this.model = model;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.anthropic.com")
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("content-type", "application/json")
                .build();
    }

    @Override
    public LlmResponse complete(LlmRequest request) {
        try {
            Map<String, Object> body = Map.of(
                    "model", request.model() != null ? request.model() : model,
                    "max_tokens", request.maxTokens(),
                    "messages", List.of(
                            Map.of("role", "user", "content", buildPrompt(request))
                    )
            );

            Map response = webClient.post()
                    .uri("/v1/messages")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                return new LlmResponse("", 0, 0, model);
            }

            List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
            String text = content != null && !content.isEmpty()
                    ? (String) content.get(0).get("text")
                    : "";

            Map<String, Object> usage = (Map<String, Object>) response.get("usage");
            int inputTokens = usage != null ? ((Number) usage.get("input_tokens")).intValue() : 0;
            int outputTokens = usage != null ? ((Number) usage.get("output_tokens")).intValue() : 0;

            return new LlmResponse(text, inputTokens, outputTokens, model);
        } catch (Exception e) {
            log.error("Claude API call failed", e);
            throw new RuntimeException("LLM request failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Flux<String> stream(LlmRequest request) {
        Map<String, Object> body = Map.of(
                "model", request.model() != null ? request.model() : model,
                "max_tokens", request.maxTokens(),
                "stream", true,
                "messages", List.of(
                        Map.of("role", "user", "content", buildPrompt(request))
                )
        );

        return webClient.post()
                .uri("/v1/messages")
                .bodyValue(body)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(chunk -> chunk.contains("\"text\""))
                .map(this::extractTextDelta);
    }

    private String buildPrompt(LlmRequest request) {
        if (request.context() != null && !request.context().isBlank()) {
            return request.context() + "\n\n" + request.prompt();
        }
        return request.prompt();
    }

    private String extractTextDelta(String chunk) {
        try {
            int idx = chunk.indexOf("\"text\":\"");
            if (idx >= 0) {
                int start = idx + 8;
                int end = chunk.indexOf("\"", start);
                return chunk.substring(start, end);
            }
        } catch (Exception ignored) {}
        return "";
    }

    @Override
    public LlmProvider getProvider() {
        return LlmProvider.CLAUDE;
    }
}
