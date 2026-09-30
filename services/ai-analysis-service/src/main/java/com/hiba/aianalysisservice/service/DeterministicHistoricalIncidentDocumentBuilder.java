package com.hiba.aianalysisservice.service;

import com.hiba.aianalysisservice.projection.IncidentAnomalyProjection;
import com.hiba.aianalysisservice.projection.IncidentContext;
import com.hiba.aianalysisservice.projection.IncidentTimelineEvent;
import org.springframework.stereotype.Component;

@Component
public class DeterministicHistoricalIncidentDocumentBuilder implements HistoricalIncidentDocumentBuilder {

    @Override
    public String build(IncidentContext context) {
        var incident = context.incident();
        StringBuilder text = new StringBuilder();
        text.append("Historical incident ").append(incident.getIncidentId()).append('\n')
                .append("Title: ").append(incident.getTitle()).append('\n')
                .append("Service: ").append(incident.getServiceName()).append('\n')
                .append("Environment: ").append(incident.getEnvironment()).append('\n')
                .append("Fingerprint: ").append(incident.getFingerprint()).append('\n')
                .append("Severity: ").append(incident.getSeverity()).append('\n')
                .append("Status: ").append(incident.getStatus()).append('\n')
                .append("Occurrences: ").append(incident.getOccurrenceCount()).append('\n')
                .append("Anomalies:\n");
        for (IncidentAnomalyProjection anomaly : context.anomalies()) {
            text.append("- ").append(anomaly.getAnomalyType())
                    .append(" category=").append(anomaly.getCategory())
                    .append(" description=").append(anomaly.getDescription())
                    .append(" detectedAt=").append(anomaly.getDetectedAt()).append('\n');
        }
        text.append("Timeline:\n");
        for (IncidentTimelineEvent event : context.timeline()) {
            text.append("- ").append(event.getOccurredAt()).append(' ')
                    .append(event.getEventType()).append(" details=")
                    .append(event.getDetails()).append('\n');
        }
        return text.toString().trim();
    }
}
