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
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "openai")
public class OpenAiLlmService implements LlmService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiLlmService.class);

    private final WebClient webClient;
    private final String model;

    public OpenAiLlmService(
            @Value("${app.ai.openai.api-key:}") String apiKey,
            @Value("${app.ai.openai.model:gpt-4o}") String model) {
        this.model = model;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("content-type", "application/json")
                .build();
    }

    @Override
    public LlmResponse complete(LlmRequest request) {
        try {
            Map<String, Object> body = Map.of(
                    "model", request.model() != null ? request.model() : model,
                    "max_tokens", request.maxTokens(),
                    "temperature", request.temperature(),
                    "messages", List.of(
                            Map.of("role", "user", "content", buildPrompt(request))
                    )
            );

            Map response = webClient.post()
                    .uri("/v1/chat/completions")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                return new LlmResponse("", 0, 0, model);
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            String text = "";
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                text = (String) message.get("content");
            }

            Map<String, Object> usage = (Map<String, Object>) response.get("usage");
            int inputTokens = usage != null ? ((Number) usage.get("prompt_tokens")).intValue() : 0;
            int outputTokens = usage != null ? ((Number) usage.get("completion_tokens")).intValue() : 0;

            return new LlmResponse(text, inputTokens, outputTokens, model);
        } catch (Exception e) {
            log.error("OpenAI API call failed", e);
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
                .uri("/v1/chat/completions")
                .bodyValue(body)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(chunk -> !chunk.equals("[DONE]") && chunk.contains("\"content\""))
                .map(this::extractContentDelta);
    }

    private String buildPrompt(LlmRequest request) {
        if (request.context() != null && !request.context().isBlank()) {
            return request.context() + "\n\n" + request.prompt();
        }
        return request.prompt();
    }

    private String extractContentDelta(String chunk) {
        try {
            int idx = chunk.indexOf("\"content\":\"");
            if (idx >= 0) {
                int start = idx + 11;
                int end = chunk.indexOf("\"", start);
                return chunk.substring(start, end);
            }
        } catch (Exception ignored) {}
        return "";
    }

    @Override
    public LlmProvider getProvider() {
        return LlmProvider.OPENAI;
    }
}
