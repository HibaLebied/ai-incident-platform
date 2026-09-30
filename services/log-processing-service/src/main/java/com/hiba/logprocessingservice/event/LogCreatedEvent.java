package com.hiba.logprocessingservice.event;

import java.time.LocalDateTime;
import java.time.Instant;
import java.util.UUID;

public record LogCreatedEvent(
        Long logId,
        String serviceName,
        String level,
        String message,
        LocalDateTime timestamp,
        String environment,
        String traceId,
        UUID eventId,
        String eventType,
        Integer schemaVersion,
        Instant occurredAt,
        String source,
        String correlationId
) {
    public LogCreatedEvent(
            Long logId,
            String serviceName,
            String level,
            String message,
            LocalDateTime timestamp,
            String environment,
            String traceId
    ) {
        this(
                logId,
                serviceName,
                level,
                message,
                timestamp,
                environment,
                traceId,
                UUID.randomUUID(),
                "LogCreated",
                1,
                Instant.now(),
                "log-ingestion-service",
                correlationIdFor(logId, traceId)
        );
    }

    private static String correlationIdFor(Long logId, String traceId) {
        return traceId != null && !traceId.isBlank() ? traceId : "log-" + logId;
    }
}
