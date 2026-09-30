package com.hiba.incidentservice.service;

import com.hiba.incidentservice.entity.Incident;
import com.hiba.incidentservice.entity.IncidentAnomaly;
import com.hiba.incidentservice.entity.OutboxEvent;
import com.hiba.incidentservice.dto.IncidentAnomalyResponse;
import com.hiba.incidentservice.dto.IncidentResponse;
import com.hiba.incidentservice.dto.IncidentTimelineResponse;
import com.hiba.incidentservice.enums.IncidentSeverity;
import com.hiba.incidentservice.enums.IncidentStatus;
import com.hiba.incidentservice.event.AnomalyDetectedEvent;
import com.hiba.incidentservice.event.IncidentAnomalyEvent;
import com.hiba.incidentservice.event.IncidentEvent;
import com.hiba.incidentservice.repository.IncidentAnomalyRepository;
import com.hiba.incidentservice.repository.IncidentRepository;
import com.hiba.incidentservice.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class IncidentService {
    private final IncidentRepository incidentRepository;
    private final IncidentAnomalyRepository incidentAnomalyRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public IncidentService(
            IncidentRepository incidentRepository,
            IncidentAnomalyRepository incidentAnomalyRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper
    ) {
        this.incidentRepository = incidentRepository;
        this.incidentAnomalyRepository = incidentAnomalyRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Incident createOrUpdateIncident(AnomalyDetectedEvent event, Optional<Incident> existingIncident) {
        boolean created = existingIncident.isEmpty();
        Incident incident = existingIncident
                .map(foundIncident -> updateExistingIncident(foundIncident, event))
                .orElseGet(() -> createIncident(event));

        incident = incidentRepository.save(incident);
        IncidentAnomaly addedAnomaly = addIncidentAnomalyIfMissing(incident, event);

        if (addedAnomaly != null) {
            saveOutboxEvent(
                    incident,
                    created ? "IncidentCreated" : "IncidentUpdated",
                    created ? "incidents.created" : "incidents.updated",
                    toEvent(addedAnomaly)
            );
        }

        return incident;
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> findAllIncidents() {
        return incidentRepository.findAllByOrderByLastSeenDesc()
                .stream()
                .map(IncidentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public IncidentResponse findIncident(Long incidentId) {
        return IncidentResponse.from(findIncidentOrThrow(incidentId));
    }

    @Transactional(readOnly = true)
    public List<IncidentAnomalyResponse> findIncidentAnomalies(Long incidentId) {
        ensureIncidentExists(incidentId);
        return incidentAnomalyRepository.findByIncidentIdOrderByDetectedAtAsc(incidentId)
                .stream()
                .map(IncidentAnomalyResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public IncidentTimelineResponse findIncidentTimeline(Long incidentId) {
        IncidentResponse incident = findIncident(incidentId);
        List<IncidentAnomalyResponse> anomalies = findIncidentAnomalies(incidentId);
        return new IncidentTimelineResponse(incident, anomalies);
    }

    @Transactional
    public IncidentResponse updateStatus(Long incidentId, IncidentStatus status) {
        Incident incident = findIncidentOrThrow(incidentId);
        if (incident.getStatus() == status) {
            return IncidentResponse.from(incident);
        }

        incident.setStatus(status);
        Incident savedIncident = incidentRepository.save(incident);
        String eventType = status == IncidentStatus.RESOLVED
                ? "IncidentResolved"
                : "IncidentUpdated";
        String topic = status == IncidentStatus.RESOLVED
                ? "incidents.resolved"
                : "incidents.updated";
        saveOutboxEvent(savedIncident, eventType, topic, null);
        return IncidentResponse.from(savedIncident);
    }

    private Incident createIncident(AnomalyDetectedEvent event) {
        LocalDateTime detectedAt = detectedAt(event);
        return new Incident(
                titleFor(event),
                event.serviceName(),
                event.environment(),
                event.fingerprint(),
                severityFrom(event),
                IncidentStatus.OPEN,
                1,
                detectedAt,
                detectedAt
        );
    }

    private Incident updateExistingIncident(Incident incident, AnomalyDetectedEvent event) {
        if (!isDuplicate(incident, event)) {
            incident.registerOccurrence(detectedAt(event));
        }
        return incident;
    }

    private IncidentAnomaly addIncidentAnomalyIfMissing(Incident incident, AnomalyDetectedEvent event) {
        if (isDuplicate(incident, event)) {
            return null;
        }

        IncidentAnomaly incidentAnomaly = new IncidentAnomaly(
                incident,
                event.logId(),
                event.anomalyType(),
                event.description(),
                event.category(),
                event.severityScore(),
                event.traceId(),
                detectedAt(event)
        );
        incidentAnomalyRepository.save(incidentAnomaly);
        return incidentAnomaly;
    }

    private void saveOutboxEvent(
            Incident incident,
            String eventType,
            String topic,
            IncidentAnomalyEvent anomaly
    ) {
        IncidentEvent event = new IncidentEvent(
                UUID.randomUUID(),
                eventType,
                2,
                Instant.now(),
                "incident-service",
                "incident-" + incident.getId(),
                incident.getId(),
                incident.getTitle(),
                incident.getServiceName(),
                incident.getEnvironment(),
                incident.getFingerprint(),
                incident.getSeverity(),
                incident.getStatus(),
                incident.getOccurrenceCount(),
                incident.getFirstSeen(),
                incident.getLastSeen(),
                anomaly
        );

        try {
            outboxEventRepository.save(OutboxEvent.builder()
                    .aggregateId(incident.getId().toString())
                    .eventType(eventType)
                    .topic(topic)
                    .payload(objectMapper.writeValueAsString(event))
                    .createdAt(LocalDateTime.now())
                    .build());
        } catch (JacksonException exception) {
            throw new IllegalStateException("Failed to serialize incident event", exception);
        }
    }

    private IncidentAnomalyEvent toEvent(IncidentAnomaly anomaly) {
        return new IncidentAnomalyEvent(
                anomaly.getLogId(),
                anomaly.getAnomalyType(),
                anomaly.getDescription(),
                anomaly.getCategory(),
                anomaly.getSeverityScore(),
                anomaly.getTraceId(),
                anomaly.getDetectedAt()
        );
    }

    private boolean isDuplicate(Incident incident, AnomalyDetectedEvent event) {
        return incident.getId() != null
                && incidentAnomalyRepository.existsByIncidentIdAndLogIdAndAnomalyType(
                incident.getId(),
                event.logId(),
                event.anomalyType()
        );
    }

    IncidentSeverity severityFrom(AnomalyDetectedEvent event) {
        Integer severityScore = event.severityScore();
        if (severityScore == null) {
            return IncidentSeverity.MEDIUM;
        }
        if (severityScore >= 5) {
            return IncidentSeverity.CRITICAL;
        }
        if (severityScore >= 4) {
            return IncidentSeverity.HIGH;
        }
        if (severityScore >= 2) {
            return IncidentSeverity.MEDIUM;
        }
        return IncidentSeverity.LOW;
    }

    private String titleFor(AnomalyDetectedEvent event) {
        return "Operational issue on " + event.serviceName() + " in " + event.environment();
    }

    private LocalDateTime detectedAt(AnomalyDetectedEvent event) {
        return event.timestamp() != null ? event.timestamp() : LocalDateTime.now();
    }

    private Incident findIncidentOrThrow(Long incidentId) {
        return incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + incidentId));
    }

    private void ensureIncidentExists(Long incidentId) {
        if (!incidentRepository.existsById(incidentId)) {
            throw new IllegalArgumentException("Incident not found: " + incidentId);
        }
    }
}
