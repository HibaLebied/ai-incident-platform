package com.hiba.logingestionservice.event;

import java.time.LocalDateTime;

public record LogCreatedEvent(
        Long logId,
        String serviceName,
        String level,
        String message,
        LocalDateTime timestamp,
        String environment,
        String traceId
) {
}
