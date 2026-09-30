package com.hiba.aianalysisservice.controller;

import com.hiba.aianalysisservice.domain.IncidentSeverity;
import com.hiba.aianalysisservice.domain.IncidentStatus;
import com.hiba.aianalysisservice.projection.IncidentProjection;

import java.time.LocalDateTime;
import java.time.Instant;

public record IncidentProjectionResponse(
        Long id,
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
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Instant lastEventOccurredAt
) {
    public static IncidentProjectionResponse from(IncidentProjection projection) {
        return new IncidentProjectionResponse(
                projection.getId(),
                projection.getIncidentId(),
                projection.getTitle(),
                projection.getServiceName(),
                projection.getEnvironment(),
                projection.getFingerprint(),
                projection.getSeverity(),
                projection.getStatus(),
                projection.getOccurrenceCount(),
                projection.getFirstSeen(),
                projection.getLastSeen(),
                projection.getCreatedAt(),
                projection.getUpdatedAt(),
                projection.getLastEventOccurredAt()
        );
    }
}
