package com.springforge.ai.domain;

public record LlmResponse(
    String content,
    int inputTokens,
    int outputTokens,
    String model
) {}
