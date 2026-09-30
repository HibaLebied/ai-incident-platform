package com.hiba.incidentservice.dto;

import com.hiba.incidentservice.entity.Incident;
import com.hiba.incidentservice.enums.IncidentSeverity;
import com.hiba.incidentservice.enums.IncidentStatus;

import java.time.LocalDateTime;

public record IncidentResponse(
        Long id,
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
        LocalDateTime updatedAt
) {
    public static IncidentResponse from(Incident incident) {
        return new IncidentResponse(
                incident.getId(),
                incident.getTitle(),
                incident.getServiceName(),
                incident.getEnvironment(),
                incident.getFingerprint(),
                incident.getSeverity(),
                incident.getStatus(),
                incident.getOccurrenceCount(),
                incident.getFirstSeen(),
                incident.getLastSeen(),
                incident.getCreatedAt(),
                incident.getUpdatedAt()
        );
    }
}
