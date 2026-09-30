package com.hiba.anomalydetectionservice.event;

import java.time.LocalDateTime;
import java.time.Instant;
import java.util.UUID;

public record LogProcessedEvent(
        Long logId,
        String serviceName,
        String level,
        String message,
        LocalDateTime timestamp,
        String environment,
        String traceId,
        String category,
        Integer severityScore,
        String httpMethod,
        String endpoint,
        Integer httpStatus,
        Long responseTimeMs,
        String ipAddress,
        Integer port,
        Boolean error,
        Integer messageLength,
        Boolean httpError,
        Boolean slowRequest,
        String fingerprint,
        LocalDateTime processedAt,
        UUID eventId,
        String eventType,
        Integer schemaVersion,
        Instant occurredAt,
        String source,
        String correlationId
) {
    public LogProcessedEvent(
            Long logId,
            String serviceName,
            String level,
            String message,
            LocalDateTime timestamp,
            String environment,
            String traceId,
            String category,
            Integer severityScore,
            String httpMethod,
            String endpoint,
            Integer httpStatus,
            Long responseTimeMs,
            String ipAddress,
            Integer port,
            Boolean error,
            Integer messageLength,
            Boolean httpError,
            Boolean slowRequest,
            String fingerprint,
            LocalDateTime processedAt
    ) {
        this(
                logId,
                serviceName,
                level,
                message,
                timestamp,
                environment,
                traceId,
                category,
                severityScore,
                httpMethod,
                endpoint,
                httpStatus,
                responseTimeMs,
                ipAddress,
                port,
                error,
                messageLength,
                httpError,
                slowRequest,
                fingerprint,
                processedAt,
                UUID.randomUUID(),
                "LogProcessed",
                1,
                Instant.now(),
                "log-processing-service",
                correlationIdFor(logId, traceId)
        );
    }

    private static String correlationIdFor(Long logId, String traceId) {
        return traceId != null && !traceId.isBlank() ? traceId : "log-" + logId;
    }
}
