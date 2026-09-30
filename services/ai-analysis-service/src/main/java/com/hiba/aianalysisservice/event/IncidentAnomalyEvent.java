package com.hiba.aianalysisservice.event;

import java.time.LocalDateTime;

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
