package com.hiba.aianalysisservice.service;

import com.hiba.aianalysisservice.domain.IncidentSeverity;
import com.hiba.aianalysisservice.domain.IncidentStatus;
import com.hiba.aianalysisservice.event.IncidentAnomalyEvent;
import com.hiba.aianalysisservice.event.IncidentEvent;
import com.hiba.aianalysisservice.projection.IncidentProjection;
import com.hiba.aianalysisservice.projection.IncidentTimelineEvent;
import com.hiba.aianalysisservice.repository.IncidentAnomalyProjectionRepository;
import com.hiba.aianalysisservice.repository.IncidentProjectionRepository;
import com.hiba.aianalysisservice.repository.IncidentTimelineEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncidentProjectionServiceTest {

    @Mock
    private IncidentProjectionRepository repository;

    @Mock
    private IncidentAnomalyProjectionRepository anomalyRepository;

    @Mock
    private IncidentTimelineEventRepository timelineRepository;

    @Test
    void createsProjectionFromIncidentEvent() {
        IncidentEvent event = event(42L, "payments-api", "production", "fp-42");
        when(repository.findByIncidentId(42L)).thenReturn(Optional.empty());
        when(repository.save(any(IncidentProjection.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        IncidentProjection projection = service().project(event);

        assertEquals(42L, projection.getIncidentId());
        assertEquals("Database failure on payments-api", projection.getTitle());
        assertEquals("payments-api", projection.getServiceName());
        assertEquals("production", projection.getEnvironment());
        assertEquals("fp-42", projection.getFingerprint());
        assertEquals(IncidentSeverity.HIGH, projection.getSeverity());
        assertEquals(IncidentStatus.OPEN, projection.getStatus());
        assertEquals(1, projection.getOccurrenceCount());
        assertEquals(event.firstSeen(), projection.getFirstSeen());
        assertEquals(event.lastSeen(), projection.getLastSeen());
        verify(repository).save(any(IncidentProjection.class));

        ArgumentCaptor<IncidentTimelineEvent> timelineCaptor = ArgumentCaptor.forClass(IncidentTimelineEvent.class);
        verify(timelineRepository).save(timelineCaptor.capture());
        assertEquals("IncidentCreated", timelineCaptor.getValue().getEventType());
        assertEquals(event.occurredAt(), timelineCaptor.getValue().getOccurredAt());
    }

    @Test
    void usesIncidentEventOccurredAtForIncidentUpdatedTimeline() {
        IncidentEvent created = eventAt(42L, "IncidentCreated", Instant.parse("2026-09-15T21:30:00Z"));
        IncidentEvent updated = eventAt(42L, "IncidentUpdated", Instant.parse("2026-09-15T21:45:00Z"));
        IncidentProjection existing = new IncidentProjection(created);

        when(repository.findByIncidentId(42L)).thenReturn(Optional.of(existing));
        when(repository.save(any(IncidentProjection.class))).thenReturn(existing);

        service().project(updated);

        ArgumentCaptor<IncidentTimelineEvent> timelineCaptor = ArgumentCaptor.forClass(IncidentTimelineEvent.class);
        verify(timelineRepository).save(timelineCaptor.capture());
        assertEquals("IncidentUpdated", timelineCaptor.getValue().getEventType());
        assertEquals(updated.occurredAt(), timelineCaptor.getValue().getOccurredAt());
    }

    @Test
    void processingSameIncidentEventAgainUpdatesTheExistingProjection() {
        IncidentEvent event = event(42L, "payments-api", "production", "fp-42");
        IncidentProjection existing = new IncidentProjection(event);
        IncidentEvent replay = new IncidentEvent(
                event.eventId(),
                event.eventType(),
                event.schemaVersion(),
                event.occurredAt(),
                event.source(),
                event.correlationId(),
                event.incidentId(),
                event.title(),
                event.serviceName(),
                event.environment(),
                event.fingerprint(),
                event.severity(),
                event.status(),
                2,
                event.firstSeen(),
                event.lastSeen().plusMinutes(5)
        );

        when(repository.findByIncidentId(42L)).thenReturn(Optional.empty(), Optional.of(existing));
        when(repository.save(any(IncidentProjection.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(timelineRepository.findBySourceEventIdAndEventType(event.eventId(), event.eventType()))
                .thenReturn(Optional.empty(), Optional.of(new IncidentTimelineEvent(
                        event.incidentId(), event.eventId(), event.eventType(), event.occurredAt(),
                        event.source(), event.correlationId(), "already stored"
                )));

        IncidentProjectionService service = service();
        service.project(event);
        IncidentProjection replayed = service.project(replay);

        assertEquals(42L, replayed.getIncidentId());
        assertEquals(2, replayed.getOccurrenceCount());
        assertEquals(replay.lastSeen(), replayed.getLastSeen());
        ArgumentCaptor<IncidentProjection> savedCaptor = ArgumentCaptor.forClass(IncidentProjection.class);
        verify(repository, org.mockito.Mockito.times(2)).save(savedCaptor.capture());
        assertSame(existing, savedCaptor.getAllValues().get(1));
    }

    @Test
    void rejectsEventWithIncompletePayload() {
        IncidentEvent invalid = new IncidentEvent(
                UUID.randomUUID(),
                "IncidentCreated",
                1,
                Instant.parse("2026-09-15T21:30:00Z"),
                "incident-service",
                "incident-42",
                null,
                "title",
                "payments-api",
                "production",
                "fp-42",
                IncidentSeverity.HIGH,
                IncidentStatus.OPEN,
                1,
                LocalDateTime.of(2026, 9, 15, 21, 30),
                LocalDateTime.of(2026, 9, 15, 21, 30)
        );

        assertThrows(IllegalArgumentException.class, () -> service().project(invalid));
        verify(repository, never()).save(any(IncidentProjection.class));
    }

    @Test
    void storesAnomalyProjectionAndAnomalyTimelineEntry() {
        IncidentEvent event = eventWithAnomaly(42L);
        when(repository.findByIncidentId(42L)).thenReturn(Optional.empty());
        when(repository.save(any(IncidentProjection.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(anomalyRepository.findByIncidentProjection_IncidentIdAndLogIdAndAnomalyType(
                42L, 1002L, "DATABASE_ERROR"
        )).thenReturn(Optional.empty());

        service().project(event);

        ArgumentCaptor<com.hiba.aianalysisservice.projection.IncidentAnomalyProjection> anomalyCaptor =
                ArgumentCaptor.forClass(com.hiba.aianalysisservice.projection.IncidentAnomalyProjection.class);
        verify(anomalyRepository).save(anomalyCaptor.capture());
        assertEquals(1002L, anomalyCaptor.getValue().getLogId());
        assertEquals("DATABASE_ERROR", anomalyCaptor.getValue().getAnomalyType());
        ArgumentCaptor<IncidentTimelineEvent> timelineCaptor = ArgumentCaptor.forClass(IncidentTimelineEvent.class);
        verify(timelineRepository, org.mockito.Mockito.times(2)).save(timelineCaptor.capture());
        IncidentTimelineEvent anomalyTimeline = timelineCaptor.getAllValues().stream()
                .filter(timeline -> "AnomalyDetected".equals(timeline.getEventType()))
                .findFirst()
                .orElseThrow();
        assertEquals(
                event.anomaly().detectedAt().toInstant(ZoneOffset.UTC),
                anomalyTimeline.getOccurredAt()
        );
    }

    @Test
    void oldIncidentEventDoesNotOverwriteSnapshotButCanAddMissingAnomalyAndTimeline() {
        IncidentEvent newer = eventAt(42L, "IncidentUpdated", Instant.parse("2026-09-15T21:45:00Z"));
        IncidentProjection existing = new IncidentProjection(newer);
        IncidentEvent older = eventWithAnomalyAt(
                42L,
                Instant.parse("2026-09-15T21:30:00Z"),
                LocalDateTime.of(2026, 9, 15, 21, 20)
        );

        when(repository.findByIncidentId(42L)).thenReturn(Optional.of(existing));
        when(anomalyRepository.findByIncidentProjection_IncidentIdAndLogIdAndAnomalyType(
                42L, 1002L, "DATABASE_ERROR"
        )).thenReturn(Optional.empty());

        service().project(older);

        verify(repository, never()).save(any(IncidentProjection.class));
        assertEquals(newer.occurrenceCount(), existing.getOccurrenceCount());
        assertEquals(newer.occurredAt(), existing.getLastEventOccurredAt());
        verify(anomalyRepository).save(any(com.hiba.aianalysisservice.projection.IncidentAnomalyProjection.class));
    }

    @Test
    void replayingAnomalyEventDoesNotDuplicateAnomalyOrTimelineEntries() {
        IncidentEvent event = eventWithAnomaly(42L);
        IncidentProjection existing = new IncidentProjection(event);
        when(repository.findByIncidentId(42L)).thenReturn(Optional.of(existing), Optional.of(existing));
        when(repository.save(any(IncidentProjection.class))).thenReturn(existing);
        when(anomalyRepository.findByIncidentProjection_IncidentIdAndLogIdAndAnomalyType(
                42L, 1002L, "DATABASE_ERROR"
        )).thenReturn(Optional.empty(), Optional.of(new com.hiba.aianalysisservice.projection.IncidentAnomalyProjection(
                existing, 1002L, "DATABASE_ERROR", "Database error detected", "DATABASE", 4,
                "trace-1002", event.anomaly().detectedAt()
        )));
        when(timelineRepository.findBySourceEventIdAndEventType(event.eventId(), event.eventType()))
                .thenReturn(Optional.empty(), Optional.of(new IncidentTimelineEvent(
                        42L, event.eventId(), event.eventType(), event.occurredAt(), event.source(),
                        event.correlationId(), "already stored"
                )));
        when(timelineRepository.findBySourceEventIdAndEventType(event.eventId(), "AnomalyDetected"))
                .thenReturn(Optional.empty(), Optional.of(new IncidentTimelineEvent(
                        42L, event.eventId(), "AnomalyDetected", event.occurredAt(), event.source(),
                        event.correlationId(), "already stored"
                )));

        service().project(event);
        service().project(event);

        verify(anomalyRepository).save(any(com.hiba.aianalysisservice.projection.IncidentAnomalyProjection.class));
        verify(timelineRepository, org.mockito.Mockito.times(2)).save(any(IncidentTimelineEvent.class));
    }

    private IncidentProjectionService service() {
        return new IncidentProjectionService(repository, anomalyRepository, timelineRepository);
    }

    private IncidentEvent event(Long incidentId, String serviceName, String environment, String fingerprint) {
        LocalDateTime seenAt = LocalDateTime.of(2026, 9, 15, 21, 30);
        return new IncidentEvent(
                UUID.randomUUID(),
                "IncidentCreated",
                1,
                Instant.parse("2026-09-15T21:30:00Z"),
                "incident-service",
                "incident-" + incidentId,
                incidentId,
                "Database failure on " + serviceName,
                serviceName,
                environment,
                fingerprint,
                IncidentSeverity.HIGH,
                IncidentStatus.OPEN,
                1,
                seenAt,
                seenAt
        );
    }

    private IncidentEvent eventWithAnomaly(Long incidentId) {
        return eventWithAnomalyAt(
                incidentId,
                Instant.parse("2026-09-15T21:30:00Z"),
                LocalDateTime.of(2026, 9, 15, 21, 30)
        );
    }

    private IncidentEvent eventWithAnomalyAt(Long incidentId, Instant occurredAt, LocalDateTime detectedAt) {
        IncidentEvent base = event(incidentId, "payments-api", "production", "fp-42");
        return new IncidentEvent(
                UUID.randomUUID(), "IncidentUpdated", 2, occurredAt, base.source(), base.correlationId(),
                base.incidentId(), base.title(), base.serviceName(), base.environment(), base.fingerprint(),
                base.severity(), base.status(), base.occurrenceCount(), base.firstSeen(), base.lastSeen(),
                new IncidentAnomalyEvent(
                        1002L, "DATABASE_ERROR", "Database error detected", "DATABASE", 4,
                        "trace-1002", detectedAt
                )
        );
    }

    private IncidentEvent eventAt(Long incidentId, String eventType, Instant occurredAt) {
        IncidentEvent base = event(incidentId, "payments-api", "production", "fp-42");
        return new IncidentEvent(
                UUID.randomUUID(), eventType, 2, occurredAt, base.source(), base.correlationId(),
                base.incidentId(), base.title(), base.serviceName(), base.environment(), base.fingerprint(),
                base.severity(), base.status(), base.occurrenceCount(), base.firstSeen(), base.lastSeen()
        );
    }
}
