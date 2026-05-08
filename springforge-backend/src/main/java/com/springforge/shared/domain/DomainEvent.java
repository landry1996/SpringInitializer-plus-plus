package com.springforge.shared.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public interface DomainEvent {
    UUID eventId();
    String eventType();
    LocalDateTime occurredAt();
}
