package com.hiba.incidentservice.dto;

import com.hiba.incidentservice.entity.IncidentAnomaly;

import java.time.LocalDateTime;

public record IncidentAnomalyResponse(
        Long id,
        Long incidentId,
        Long logId,
        String anomalyType,
        String description,
        String category,
        Integer severityScore,
        String traceId,
        LocalDateTime detectedAt
) {
    public static IncidentAnomalyResponse from(IncidentAnomaly anomaly) {
        return new IncidentAnomalyResponse(
                anomaly.getId(),
                anomaly.getIncident().getId(),
                anomaly.getLogId(),
                anomaly.getAnomalyType(),
                anomaly.getDescription(),
                anomaly.getCategory(),
                anomaly.getSeverityScore(),
                anomaly.getTraceId(),
                anomaly.getDetectedAt()
        );
    }
}
