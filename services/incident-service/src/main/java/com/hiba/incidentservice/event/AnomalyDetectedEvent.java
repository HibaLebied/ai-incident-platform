package com.hiba.incidentservice.event;

import java.time.LocalDateTime;
import java.time.Instant;
import java.util.UUID;

public record AnomalyDetectedEvent(
        Long logId,
        String serviceName,
        String environment,
        String traceId,
        LocalDateTime timestamp,
        String anomalyType,
        String description,
        String fingerprint,
        String category,
        Integer severityScore,
        UUID eventId,
        String eventType,
        Integer schemaVersion,
        Instant occurredAt,
        String source,
        String correlationId
) {
    public AnomalyDetectedEvent(
            Long logId,
            String serviceName,
            String environment,
            String traceId,
            LocalDateTime timestamp,
            String anomalyType,
            String description,
            String fingerprint,
            String category,
            Integer severityScore
    ) {
        this(
                logId,
                serviceName,
                environment,
                traceId,
                timestamp,
                anomalyType,
                description,
                fingerprint,
                category,
                severityScore,
                UUID.randomUUID(),
                "AnomalyDetected",
                1,
                Instant.now(),
                "anomaly-detection-service",
                correlationIdFor(logId, traceId)
        );
    }

    private static String correlationIdFor(Long logId, String traceId) {
        return traceId != null && !traceId.isBlank() ? traceId : "log-" + logId;
    }
}
