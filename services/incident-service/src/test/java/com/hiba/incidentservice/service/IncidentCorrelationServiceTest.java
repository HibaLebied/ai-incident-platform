package com.hiba.incidentservice.service;

import com.hiba.incidentservice.entity.Incident;
import com.hiba.incidentservice.entity.IncidentAnomaly;
import com.hiba.incidentservice.entity.OutboxEvent;
import com.hiba.incidentservice.enums.IncidentSeverity;
import com.hiba.incidentservice.enums.IncidentStatus;
import com.hiba.incidentservice.event.AnomalyDetectedEvent;
import com.hiba.incidentservice.event.IncidentEvent;
import com.hiba.incidentservice.repository.IncidentAnomalyRepository;
import com.hiba.incidentservice.repository.IncidentCorrelationLock;
import com.hiba.incidentservice.repository.IncidentRepository;
import com.hiba.incidentservice.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncidentCorrelationServiceTest {
    private static final LocalDateTime FIRST_TIMESTAMP = LocalDateTime.of(2026, 9, 15, 21, 30);
    private static final LocalDateTime SECOND_TIMESTAMP = LocalDateTime.of(2026, 9, 15, 21, 40);

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private IncidentAnomalyRepository incidentAnomalyRepository;

    @Mock
    private IncidentCorrelationLock correlationLock;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    private IncidentCorrelationService correlationService;

    @BeforeEach
    void setUp() {
        IncidentService incidentService = new IncidentService(
                incidentRepository,
                incidentAnomalyRepository,
                outboxEventRepository,
                objectMapper
        );
        correlationService = new IncidentCorrelationService(incidentRepository, incidentService, correlationLock);

        AtomicLong ids = new AtomicLong(1);
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> {
            Incident incident = invocation.getArgument(0);
            if (incident.getId() == null) {
                incident.setId(ids.getAndIncrement());
            }
            return incident;
        });
    }

    @Test
    void createsNewIncidentWhenNoOpenIncidentExists() {
        AnomalyDetectedEvent event = anomaly(1002L, "payment-service", "production", "DATABASE_ERROR", "fp-1", FIRST_TIMESTAMP);

        when(incidentRepository.findFirstByServiceNameAndEnvironmentAndFingerprintAndStatus(
                "payment-service", "production", "fp-1", IncidentStatus.OPEN
        )).thenReturn(Optional.empty());

        Incident incident = correlationService.process(event);

        assertEquals(IncidentStatus.OPEN, incident.getStatus());
        assertEquals(IncidentSeverity.HIGH, incident.getSeverity());
        assertEquals(1, incident.getOccurrenceCount());
        assertEquals(FIRST_TIMESTAMP, incident.getFirstSeen());
        assertEquals(FIRST_TIMESTAMP, incident.getLastSeen());

        ArgumentCaptor<IncidentAnomaly> anomalyCaptor = ArgumentCaptor.forClass(IncidentAnomaly.class);
        verify(incidentAnomalyRepository).save(anomalyCaptor.capture());
        assertSame(incident, anomalyCaptor.getValue().getIncident());
        assertEquals(1002L, anomalyCaptor.getValue().getLogId());
        assertEquals("DATABASE_ERROR", anomalyCaptor.getValue().getAnomalyType());

        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(outboxCaptor.capture());
        assertEquals("IncidentCreated", outboxCaptor.getValue().getEventType());
        assertEquals("incidents.created", outboxCaptor.getValue().getTopic());

        ArgumentCaptor<IncidentEvent> eventCaptor = ArgumentCaptor.forClass(IncidentEvent.class);
        verify(objectMapper).writeValueAsString(eventCaptor.capture());
        assertEquals(2, eventCaptor.getValue().schemaVersion());
        assertEquals(1002L, eventCaptor.getValue().anomaly().logId());
        assertEquals("DATABASE_ERROR", eventCaptor.getValue().anomaly().anomalyType());
    }

    @Test
    void correlatesAnomalyWithExistingOpenIncident() {
        Incident existingIncident = openIncident(1L, "payment-service", "production", "fp-1", FIRST_TIMESTAMP);
        AnomalyDetectedEvent event = anomaly(1003L, "payment-service", "production", "HTTP_SERVER_ERROR", "fp-1", SECOND_TIMESTAMP);

        when(incidentRepository.findFirstByServiceNameAndEnvironmentAndFingerprintAndStatus(
                "payment-service", "production", "fp-1", IncidentStatus.OPEN
        )).thenReturn(Optional.of(existingIncident));

        Incident incident = correlationService.process(event);

        assertSame(existingIncident, incident);
        assertEquals(2, incident.getOccurrenceCount());
        assertEquals(SECOND_TIMESTAMP, incident.getLastSeen());
        verify(incidentRepository).save(existingIncident);
        verify(incidentAnomalyRepository).save(any(IncidentAnomaly.class));
    }

    @Test
    void createsNewIncidentWhenFingerprintIsDifferent() {
        AnomalyDetectedEvent event = anomaly(1004L, "payment-service", "production", "DATABASE_ERROR", "fp-2", FIRST_TIMESTAMP);

        when(incidentRepository.findFirstByServiceNameAndEnvironmentAndFingerprintAndStatus(
                "payment-service", "production", "fp-2", IncidentStatus.OPEN
        )).thenReturn(Optional.empty());

        Incident incident = correlationService.process(event);

        assertEquals("fp-2", incident.getFingerprint());
        assertEquals(1, incident.getOccurrenceCount());
        verify(incidentAnomalyRepository).save(any(IncidentAnomaly.class));
    }

    @Test
    void createsNewIncidentWhenEnvironmentIsDifferent() {
        AnomalyDetectedEvent event = anomaly(1005L, "payment-service", "staging", "DATABASE_ERROR", "fp-1", FIRST_TIMESTAMP);

        when(incidentRepository.findFirstByServiceNameAndEnvironmentAndFingerprintAndStatus(
                "payment-service", "staging", "fp-1", IncidentStatus.OPEN
        )).thenReturn(Optional.empty());

        Incident incident = correlationService.process(event);

        assertEquals("staging", incident.getEnvironment());
        assertEquals(1, incident.getOccurrenceCount());
        verify(incidentAnomalyRepository).save(any(IncidentAnomaly.class));
    }

    @Test
    void doesNotReuseResolvedIncidentBecauseCorrelationOnlySearchesOpenIncidents() {
        AnomalyDetectedEvent event = anomaly(1006L, "payment-service", "production", "DATABASE_ERROR", "fp-1", FIRST_TIMESTAMP);

        when(incidentRepository.findFirstByServiceNameAndEnvironmentAndFingerprintAndStatus(
                "payment-service", "production", "fp-1", IncidentStatus.OPEN
        )).thenReturn(Optional.empty());

        Incident incident = correlationService.process(event);

        assertEquals(IncidentStatus.OPEN, incident.getStatus());
        assertEquals(1, incident.getOccurrenceCount());
        verify(incidentRepository).findFirstByServiceNameAndEnvironmentAndFingerprintAndStatus(
                eq("payment-service"),
                eq("production"),
                eq("fp-1"),
                eq(IncidentStatus.OPEN)
        );
    }

    @Test
    void ignoresDuplicateIncidentAnomalyForSameIncidentLogAndType() {
        Incident existingIncident = openIncident(1L, "payment-service", "production", "fp-1", FIRST_TIMESTAMP);
        AnomalyDetectedEvent duplicate = anomaly(1002L, "payment-service", "production", "DATABASE_ERROR", "fp-1", SECOND_TIMESTAMP);

        when(incidentRepository.findFirstByServiceNameAndEnvironmentAndFingerprintAndStatus(
                "payment-service", "production", "fp-1", IncidentStatus.OPEN
        )).thenReturn(Optional.of(existingIncident));
        when(incidentAnomalyRepository.existsByIncidentIdAndLogIdAndAnomalyType(1L, 1002L, "DATABASE_ERROR"))
                .thenReturn(true);

        Incident incident = correlationService.process(duplicate);

        assertEquals(1, incident.getOccurrenceCount());
        assertEquals(FIRST_TIMESTAMP, incident.getLastSeen());
        verify(incidentAnomalyRepository, never()).save(any(IncidentAnomaly.class));
        assertTrue(incidentAnomalyRepository.existsByIncidentIdAndLogIdAndAnomalyType(1L, 1002L, "DATABASE_ERROR"));
    }

    private Incident openIncident(Long id, String serviceName, String environment, String fingerprint, LocalDateTime timestamp) {
        Incident incident = new Incident(
                "DATABASE_ERROR detected on " + serviceName,
                serviceName,
                environment,
                fingerprint,
                IncidentSeverity.HIGH,
                IncidentStatus.OPEN,
                1,
                timestamp,
                timestamp
        );
        incident.setId(id);
        return incident;
    }

    private AnomalyDetectedEvent anomaly(Long logId, String serviceName, String environment,
                                         String anomalyType, String fingerprint, LocalDateTime timestamp) {
        return new AnomalyDetectedEvent(
                logId,
                serviceName,
                environment,
                "trace-" + logId,
                timestamp,
                anomalyType,
                anomalyType + " detected",
                fingerprint,
                "DATABASE",
                4
        );
    }
}
