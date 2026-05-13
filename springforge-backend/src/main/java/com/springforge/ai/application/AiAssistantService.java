package com.springforge.ai.application;

import com.springforge.ai.domain.LlmRequest;
import com.springforge.ai.domain.LlmResponse;
import com.springforge.ai.domain.LlmService;
import com.springforge.generator.domain.ProjectConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class AiAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AiAssistantService.class);

    private final LlmService llmService;
    private final ObjectMapper objectMapper;

    public AiAssistantService(LlmService llmService, ObjectMapper objectMapper) {
        this.llmService = llmService;
        this.objectMapper = objectMapper;
    }

    public LlmResponse reviewProject(ProjectConfiguration config) {
        String context = serializeConfig(config);
        String prompt = """
            You are a senior Spring Boot architect. Review the following project configuration and provide:
            1. Architecture assessment (is the chosen architecture appropriate?)
            2. Dependency analysis (missing dependencies, conflicts, suggestions)
            3. Security considerations
            4. Performance recommendations
            5. Testing strategy suggestions

            Be concise and actionable. Format as markdown.
            """;
        return llmService.complete(new LlmRequest(prompt, context));
    }

    public LlmResponse suggestImprovements(ProjectConfiguration config) {
        String context = serializeConfig(config);
        String prompt = """
            Based on this Spring Boot project configuration, suggest specific improvements:
            - Better dependency choices
            - Architecture patterns that could improve maintainability
            - Infrastructure optimizations
            - Security hardening steps

            Return a JSON array of suggestions with fields: category, title, description, priority (1-5).
            """;
        return llmService.complete(new LlmRequest(prompt, context));
    }

    public LlmResponse generateCode(String description, ProjectConfiguration config) {
        String context = serializeConfig(config);
        String prompt = "Generate Spring Boot code for: " + description +
                "\n\nUse the project's package structure, architecture, and dependencies. " +
                "Return only the Java code with proper imports.";
        return llmService.complete(new LlmRequest(prompt, context));
    }

    public Flux<String> streamChat(String message, ProjectConfiguration config) {
        String context = config != null ? serializeConfig(config) : "";
        String prompt = "You are a Spring Boot expert assistant. " +
                "Help the user with their project. Be concise.\n\nUser: " + message;
        return llmService.stream(new LlmRequest(prompt, context));
    }

    private String serializeConfig(ProjectConfiguration config) {
        try {
            return objectMapper.writeValueAsString(config);
        } catch (Exception e) {
            log.warn("Failed to serialize config", e);
            return "";
        }
    }
}
