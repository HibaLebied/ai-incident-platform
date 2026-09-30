package com.hiba.incidentservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "incident_anomalies",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_incident_anomaly_log_type",
                columnNames = {"incident_id", "log_id", "anomaly_type"}
        )
)
public class IncidentAnomaly {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @Column(name = "log_id", nullable = false)
    private Long logId;

    @Column(name = "anomaly_type", nullable = false, length = 80)
    private String anomalyType;

    @Column(length = 1000)
    private String description;

    @Column(length = 80)
    private String category;

    private Integer severityScore;

    @Column(length = 200)
    private String traceId;

    @Column(nullable = false)
    private LocalDateTime detectedAt;

    protected IncidentAnomaly() {
    }

    public IncidentAnomaly(Incident incident, Long logId, String anomalyType, LocalDateTime detectedAt) {
        this(incident, logId, anomalyType, null, null, null, null, detectedAt);
    }

    public IncidentAnomaly(
            Incident incident,
            Long logId,
            String anomalyType,
            String description,
            String category,
            Integer severityScore,
            String traceId,
            LocalDateTime detectedAt
    ) {
        this.incident = incident;
        this.logId = logId;
        this.anomalyType = anomalyType;
        this.description = description;
        this.category = category;
        this.severityScore = severityScore;
        this.traceId = traceId;
        this.detectedAt = detectedAt;
    }

    public Long getId() {
        return id;
    }

    public Incident getIncident() {
        return incident;
    }

    public Long getLogId() {
        return logId;
    }

    public String getAnomalyType() {
        return anomalyType;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public Integer getSeverityScore() {
        return severityScore;
    }

    public String getTraceId() {
        return traceId;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }
}
