package com.springforge.ai;

import com.springforge.ai.application.AiAssistantService;
import com.springforge.ai.domain.LlmRequest;
import com.springforge.ai.domain.LlmResponse;
import com.springforge.ai.domain.LlmService;
import com.springforge.generator.domain.ProjectConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiAssistantServiceTest {

    @Mock
    private LlmService llmService;

    private AiAssistantService aiAssistantService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        aiAssistantService = new AiAssistantService(llmService, objectMapper);
    }

    @Test
    void reviewProject_shouldCallLlmWithReviewPrompt() {
        LlmResponse response = new LlmResponse("Review result: looks good", 100, 200, "claude-sonnet");
        when(llmService.complete(any(LlmRequest.class))).thenReturn(response);

        ProjectConfiguration config = null;
        LlmResponse result = aiAssistantService.reviewProject(config);

        assertThat(result.content()).isEqualTo("Review result: looks good");
        ArgumentCaptor<LlmRequest> captor = ArgumentCaptor.forClass(LlmRequest.class);
        verify(llmService).complete(captor.capture());
        assertThat(captor.getValue().prompt()).contains("Review");
    }

    @Test
    void suggestImprovements_shouldCallLlmWithSuggestPrompt() {
        LlmResponse response = new LlmResponse("[{\"title\":\"Add cache\"}]", 80, 150, "claude-sonnet");
        when(llmService.complete(any(LlmRequest.class))).thenReturn(response);

        ProjectConfiguration config = null;
        LlmResponse result = aiAssistantService.suggestImprovements(config);

        assertThat(result.content()).contains("cache");
        ArgumentCaptor<LlmRequest> captor = ArgumentCaptor.forClass(LlmRequest.class);
        verify(llmService).complete(captor.capture());
        assertThat(captor.getValue().prompt()).contains("improvements");
    }

    @Test
    void generateCode_shouldIncludeDescriptionInPrompt() {
        LlmResponse response = new LlmResponse("public class UserController {}", 50, 100, "claude-sonnet");
        when(llmService.complete(any(LlmRequest.class))).thenReturn(response);

        ProjectConfiguration config = null;
        LlmResponse result = aiAssistantService.generateCode("CRUD controller for User entity", config);

        assertThat(result.content()).contains("UserController");
        ArgumentCaptor<LlmRequest> captor = ArgumentCaptor.forClass(LlmRequest.class);
        verify(llmService).complete(captor.capture());
        assertThat(captor.getValue().prompt()).contains("CRUD controller for User entity");
    }

    @Test
    void streamChat_shouldReturnFluxFromLlmService() {
        Flux<String> flux = Flux.just("Hello", " World");
        when(llmService.stream(any(LlmRequest.class))).thenReturn(flux);

        ProjectConfiguration config = null;
        Flux<String> result = aiAssistantService.streamChat("How do I add caching?", config);

        assertThat(result.collectList().block()).containsExactly("Hello", " World");
        verify(llmService).stream(any(LlmRequest.class));
    }

    @Test
    void streamChat_shouldWorkWithNullConfig() {
        Flux<String> flux = Flux.just("Sure", " thing");
        when(llmService.stream(any(LlmRequest.class))).thenReturn(flux);

        Flux<String> result = aiAssistantService.streamChat("General question", null);

        assertThat(result.collectList().block()).hasSize(2);
    }
}
