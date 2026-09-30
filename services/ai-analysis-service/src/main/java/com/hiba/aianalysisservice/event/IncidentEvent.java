package com.hiba.aianalysisservice.event;

import com.hiba.aianalysisservice.domain.IncidentSeverity;
import com.hiba.aianalysisservice.domain.IncidentStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/** Local Kafka contract; it is intentionally not shared with incident-service. */
public record IncidentEvent(
        UUID eventId,
        String eventType,
        Integer schemaVersion,
        Instant occurredAt,
        String source,
        String correlationId,
        Long incidentId,
        String title,
        String serviceName,
        String environment,
        String fingerprint,
        IncidentSeverity severity,
        IncidentStatus status,
        Integer occurrenceCount,
        LocalDateTime firstSeen,
        LocalDateTime lastSeen,
        IncidentAnomalyEvent anomaly
) {
    public IncidentEvent(
            UUID eventId,
            String eventType,
            Integer schemaVersion,
            Instant occurredAt,
            String source,
            String correlationId,
            Long incidentId,
            String title,
            String serviceName,
            String environment,
            String fingerprint,
            IncidentSeverity severity,
            IncidentStatus status,
            Integer occurrenceCount,
            LocalDateTime firstSeen,
            LocalDateTime lastSeen
    ) {
        this(
                eventId,
                eventType,
                schemaVersion,
                occurredAt,
                source,
                correlationId,
                incidentId,
                title,
                serviceName,
                environment,
                fingerprint,
                severity,
                status,
                occurrenceCount,
                firstSeen,
                lastSeen,
                null
        );
    }
}
