package com.hiba.aianalysisservice.controller;

import com.hiba.aianalysisservice.projection.IncidentAnomalyProjection;
import com.hiba.aianalysisservice.projection.IncidentContext;
import com.hiba.aianalysisservice.projection.IncidentTimelineEvent;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record IncidentContextResponse(
        IncidentProjectionResponse incident,
        List<IncidentAnomalyResponse> anomalies,
        List<IncidentTimelineResponse> timeline
) {
    public static IncidentContextResponse from(IncidentContext context) {
        return new IncidentContextResponse(
                IncidentProjectionResponse.from(context.incident()),
                context.anomalies().stream().map(IncidentAnomalyResponse::from).toList(),
                context.timeline().stream().map(IncidentTimelineResponse::from).toList()
        );
    }

    public record IncidentAnomalyResponse(
            Long id,
            Long logId,
            String anomalyType,
            String description,
            String category,
            Integer severityScore,
            String traceId,
            LocalDateTime detectedAt
    ) {
        private static IncidentAnomalyResponse from(IncidentAnomalyProjection anomaly) {
            return new IncidentAnomalyResponse(
                    anomaly.getId(),
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

    public record IncidentTimelineResponse(
            Long id,
            Long incidentId,
            UUID sourceEventId,
            String eventType,
            Instant occurredAt,
            String source,
            String correlationId,
            String details
    ) {
        private static IncidentTimelineResponse from(IncidentTimelineEvent event) {
            return new IncidentTimelineResponse(
                    event.getId(),
                    event.getIncidentId(),
                    event.getSourceEventId(),
                    event.getEventType(),
                    event.getOccurredAt(),
                    event.getSource(),
                    event.getCorrelationId(),
                    event.getDetails()
            );
        }
    }
}
