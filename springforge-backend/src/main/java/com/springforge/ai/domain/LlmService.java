package com.springforge.ai.domain;

import reactor.core.publisher.Flux;

public interface LlmService {

    LlmResponse complete(LlmRequest request);

    Flux<String> stream(LlmRequest request);

    LlmProvider getProvider();
}
