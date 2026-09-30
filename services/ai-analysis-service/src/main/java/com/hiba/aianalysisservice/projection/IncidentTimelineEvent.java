package com.hiba.aianalysisservice.projection;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "incident_timeline_events",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_ai_timeline_source_event_type",
                columnNames = {"source_event_id", "event_type"}
        ),
        indexes = @Index(
                name = "idx_ai_timeline_incident_occurred_at",
                columnList = "incident_id, occurred_at"
        )
)
public class IncidentTimelineEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "incident_id", nullable = false)
    private Long incidentId;

    @Column(name = "source_event_id", nullable = false)
    private UUID sourceEventId;

    @Column(name = "event_type", nullable = false, length = 80)
    private String eventType;

    @Column(nullable = false)
    private Instant occurredAt;

    @Column(nullable = false, length = 120)
    private String source;

    @Column(nullable = false, length = 200)
    private String correlationId;

    @Column(length = 1000)
    private String details;

    protected IncidentTimelineEvent() {
    }

    public IncidentTimelineEvent(
            Long incidentId,
            UUID sourceEventId,
            String eventType,
            Instant occurredAt,
            String source,
            String correlationId,
            String details
    ) {
        this.incidentId = incidentId;
        this.sourceEventId = sourceEventId;
        this.eventType = eventType;
        this.occurredAt = occurredAt;
        this.source = source;
        this.correlationId = correlationId;
        this.details = details;
    }

    public Long getId() {
        return id;
    }

    public Long getIncidentId() {
        return incidentId;
    }

    public UUID getSourceEventId() {
        return sourceEventId;
    }

    public String getEventType() {
        return eventType;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getSource() {
        return source;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getDetails() {
        return details;
    }
}
