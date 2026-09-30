package com.hiba.incidentservice.event;

import java.time.LocalDateTime;

/** Anomaly details carried by an incident event when a new anomaly is attached. */
public record IncidentAnomalyEvent(
        Long logId,
        String anomalyType,
        String description,
        String category,
        Integer severityScore,
        String traceId,
        LocalDateTime detectedAt
) {
}
