package com.hiba.aianalysisservice.projection;

import com.hiba.aianalysisservice.domain.IncidentSeverity;
import com.hiba.aianalysisservice.domain.IncidentStatus;
import com.hiba.aianalysisservice.event.IncidentEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.time.Instant;

@Entity
@Table(
        name = "incident_projections",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_incident_projections_incident_id",
                columnNames = "incident_id"
        )
)
public class IncidentProjection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "incident_id", nullable = false)
    private Long incidentId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 120)
    private String serviceName;

    @Column(nullable = false, length = 80)
    private String environment;

    @Column(nullable = false, length = 128)
    private String fingerprint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IncidentSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IncidentStatus status;

    @Column(nullable = false)
    private Integer occurrenceCount;

    @Column(nullable = false)
    private LocalDateTime firstSeen;

    @Column(nullable = false)
    private LocalDateTime lastSeen;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Nullable keeps ddl-auto=update compatible with projections created in Lot 12.
    @Column(name = "last_event_occurred_at")
    private Instant lastEventOccurredAt;

    protected IncidentProjection() {
    }

    public IncidentProjection(IncidentEvent event) {
        updateFrom(event);
    }

    public void updateFrom(IncidentEvent event) {
        this.incidentId = event.incidentId();
        this.title = event.title();
        this.serviceName = event.serviceName();
        this.environment = event.environment();
        this.fingerprint = event.fingerprint();
        this.severity = event.severity();
        this.status = event.status();
        this.occurrenceCount = event.occurrenceCount();
        this.firstSeen = event.firstSeen();
        this.lastSeen = event.lastSeen();
        this.lastEventOccurredAt = event.occurredAt();
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getIncidentId() {
        return incidentId;
    }

    public String getTitle() {
        return title;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public IncidentSeverity getSeverity() {
        return severity;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public Integer getOccurrenceCount() {
        return occurrenceCount;
    }

    public LocalDateTime getFirstSeen() {
        return firstSeen;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Instant getLastEventOccurredAt() {
        return lastEventOccurredAt;
    }
}
