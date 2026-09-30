package com.hiba.incidentservice.entity;

import com.hiba.incidentservice.enums.IncidentSeverity;
import com.hiba.incidentservice.enums.IncidentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "incidents",
        indexes = @Index(
                name = "idx_incidents_correlation",
                columnList = "service_name, environment, fingerprint, status"
        )
)
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    protected Incident() {
    }

    public Incident(String title, String serviceName, String environment, String fingerprint,
                    IncidentSeverity severity, IncidentStatus status, Integer occurrenceCount,
                    LocalDateTime firstSeen, LocalDateTime lastSeen) {
        this.title = title;
        this.serviceName = serviceName;
        this.environment = environment;
        this.fingerprint = fingerprint;
        this.severity = severity;
        this.status = status;
        this.occurrenceCount = occurrenceCount;
        this.firstSeen = firstSeen;
        this.lastSeen = lastSeen;
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

    public void registerOccurrence(LocalDateTime detectedAt) {
        occurrenceCount++;
        if (detectedAt.isAfter(lastSeen)) {
            lastSeen = detectedAt;
        }
    }

    public Long getId() {
        return id;
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

    public void setId(Long id) {
        this.id = id;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }
}
