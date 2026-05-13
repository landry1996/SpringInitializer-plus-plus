package com.springforge.ai.domain;

public record LlmRequest(
    String prompt,
    String context,
    String model,
    double temperature,
    int maxTokens
) {
    public LlmRequest(String prompt, String context) {
        this(prompt, context, null, 0.7, 4096);
    }
}
