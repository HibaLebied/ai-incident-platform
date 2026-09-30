package com.hiba.aianalysisservice.projection;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "incident_anomaly_projections",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_ai_incident_anomaly_log_type",
                columnNames = {"incident_projection_id", "log_id", "anomaly_type"}
        ),
        indexes = @Index(
                name = "idx_ai_anomaly_incident_detected_at",
                columnList = "incident_projection_id, detected_at"
        )
)
public class IncidentAnomalyProjection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_projection_id", nullable = false)
    private IncidentProjection incidentProjection;

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

    protected IncidentAnomalyProjection() {
    }

    public IncidentAnomalyProjection(
            IncidentProjection incidentProjection,
            Long logId,
            String anomalyType,
            String description,
            String category,
            Integer severityScore,
            String traceId,
            LocalDateTime detectedAt
    ) {
        this.incidentProjection = incidentProjection;
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

    public IncidentProjection getIncidentProjection() {
        return incidentProjection;
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
