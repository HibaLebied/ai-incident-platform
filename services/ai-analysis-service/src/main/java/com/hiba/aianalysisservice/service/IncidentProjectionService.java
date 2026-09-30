package com.hiba.aianalysisservice.service;

import com.hiba.aianalysisservice.event.IncidentEvent;
import com.hiba.aianalysisservice.event.IncidentAnomalyEvent;
import com.hiba.aianalysisservice.projection.IncidentAnomalyProjection;
import com.hiba.aianalysisservice.projection.IncidentContext;
import com.hiba.aianalysisservice.projection.IncidentProjection;
import com.hiba.aianalysisservice.projection.IncidentTimelineEvent;
import com.hiba.aianalysisservice.repository.IncidentAnomalyProjectionRepository;
import com.hiba.aianalysisservice.repository.IncidentProjectionRepository;
import com.hiba.aianalysisservice.repository.IncidentTimelineEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.time.ZoneOffset;

@Service
public class IncidentProjectionService {

    private static final Logger log = LoggerFactory.getLogger(IncidentProjectionService.class);

    private final IncidentProjectionRepository repository;
    private final IncidentAnomalyProjectionRepository anomalyRepository;
    private final IncidentTimelineEventRepository timelineRepository;

    public IncidentProjectionService(
            IncidentProjectionRepository repository,
            IncidentAnomalyProjectionRepository anomalyRepository,
            IncidentTimelineEventRepository timelineRepository
    ) {
        this.repository = repository;
        this.anomalyRepository = anomalyRepository;
        this.timelineRepository = timelineRepository;
    }

    @Transactional
    public IncidentProjection project(IncidentEvent event) {
        validate(event);

        Optional<IncidentProjection> existing = repository.findByIncidentId(event.incidentId());
        if (existing.isPresent()) {
            IncidentProjection projection = existing.get();
            boolean snapshotApplied = isNewer(event, projection);
            IncidentProjection saved = projection;
            if (snapshotApplied) {
                projection.updateFrom(event);
                saved = repository.save(projection);
            }
            persistTimeline(event);
            persistAnomaly(event, saved);
            log.info("Processed incident context incidentId={} eventId={} snapshotApplied={}",
                    event.incidentId(), event.eventId(), snapshotApplied);
            return saved;
        }

        IncidentProjection saved = repository.save(new IncidentProjection(event));
        persistTimeline(event);
        persistAnomaly(event, saved);
        log.info("Created incident context incidentId={} eventId={}", event.incidentId(), event.eventId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<IncidentProjection> findByIncidentId(Long incidentId) {
        return repository.findByIncidentId(incidentId);
    }

    @Transactional(readOnly = true)
    public Optional<IncidentContext> findContext(Long incidentId) {
        return repository.findByIncidentId(incidentId)
                .map(incident -> new IncidentContext(
                        incident,
                        anomalyRepository.findByIncidentProjection_IncidentIdOrderByDetectedAtAsc(incidentId),
                        timelineRepository.findByIncidentIdOrderByOccurredAtAscIdAsc(incidentId)
                ));
    }

    private void persistTimeline(IncidentEvent event) {
        saveTimelineIfMissing(
                event,
                event.eventType(),
                "Incident state changed to " + event.status(),
                event.occurredAt()
        );
    }

    private boolean isNewer(IncidentEvent event, IncidentProjection projection) {
        return projection.getLastEventOccurredAt() == null
                || !event.occurredAt().isBefore(projection.getLastEventOccurredAt());
    }

    private void persistAnomaly(IncidentEvent event, IncidentProjection incident) {
        IncidentAnomalyEvent anomaly = event.anomaly();
        if (anomaly == null) {
            return;
        }

        Optional<IncidentAnomalyProjection> existing =
                anomalyRepository.findByIncidentProjection_IncidentIdAndLogIdAndAnomalyType(
                        event.incidentId(), anomaly.logId(), anomaly.anomalyType()
                );
        if (existing.isPresent()) {
            return;
        }

        anomalyRepository.save(new IncidentAnomalyProjection(
                incident,
                anomaly.logId(),
                anomaly.anomalyType(),
                anomaly.description(),
                anomaly.category(),
                anomaly.severityScore(),
                anomaly.traceId(),
                anomaly.detectedAt()
        ));
        saveTimelineIfMissing(
                event,
                "AnomalyDetected",
                anomaly.anomalyType() + " detected",
                // The upstream anomaly contract is LocalDateTime; UTC keeps the conversion deterministic.
                anomaly.detectedAt().toInstant(ZoneOffset.UTC)
        );
        log.info("Stored incident anomaly projection incidentId={} logId={} anomalyType={}",
                event.incidentId(), anomaly.logId(), anomaly.anomalyType());
    }

    private void saveTimelineIfMissing(
            IncidentEvent event,
            String eventType,
            String details,
            Instant occurredAt
    ) {
        if (timelineRepository.findBySourceEventIdAndEventType(event.eventId(), eventType).isPresent()) {
            return;
        }
        timelineRepository.save(new IncidentTimelineEvent(
                event.incidentId(),
                event.eventId(),
                eventType,
                occurredAt,
                event.source(),
                event.correlationId(),
                details
        ));
    }

    private void validate(IncidentEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Incident event must not be null");
        }
        if (event.eventId() == null || isBlank(event.eventType()) || event.schemaVersion() == null
                || event.occurredAt() == null || isBlank(event.source()) || isBlank(event.correlationId())) {
            throw new IllegalArgumentException("Incident event metadata is incomplete");
        }
        if (event.incidentId() == null || isBlank(event.title()) || isBlank(event.serviceName())
                || isBlank(event.environment()) || isBlank(event.fingerprint()) || event.severity() == null
                || event.status() == null || event.occurrenceCount() == null || event.occurrenceCount() < 1
                || event.firstSeen() == null || event.lastSeen() == null) {
            throw new IllegalArgumentException("Incident event payload is incomplete");
        }
        if (event.anomaly() != null) {
            IncidentAnomalyEvent anomaly = event.anomaly();
            if (anomaly.logId() == null || isBlank(anomaly.anomalyType()) || anomaly.detectedAt() == null) {
                throw new IllegalArgumentException("Incident anomaly payload is incomplete");
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
