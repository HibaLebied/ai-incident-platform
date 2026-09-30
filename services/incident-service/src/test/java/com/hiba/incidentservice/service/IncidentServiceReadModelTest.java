package com.hiba.incidentservice.service;

import com.hiba.incidentservice.dto.IncidentAnomalyResponse;
import com.hiba.incidentservice.dto.IncidentResponse;
import com.hiba.incidentservice.dto.IncidentTimelineResponse;
import com.hiba.incidentservice.entity.Incident;
import com.hiba.incidentservice.entity.IncidentAnomaly;
import com.hiba.incidentservice.enums.IncidentSeverity;
import com.hiba.incidentservice.enums.IncidentStatus;
import com.hiba.incidentservice.repository.IncidentAnomalyRepository;
import com.hiba.incidentservice.repository.IncidentRepository;
import com.hiba.incidentservice.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncidentServiceReadModelTest {
    private static final LocalDateTime FIRST_TIMESTAMP = LocalDateTime.of(2026, 9, 15, 21, 30);
    private static final LocalDateTime SECOND_TIMESTAMP = LocalDateTime.of(2026, 9, 15, 21, 40);

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private IncidentAnomalyRepository incidentAnomalyRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    private IncidentService incidentService;

    @BeforeEach
    void setUp() {
        incidentService = new IncidentService(
                incidentRepository,
                incidentAnomalyRepository,
                outboxEventRepository,
                objectMapper
        );
    }

    @Test
    void returnsIncidentsOrderedByLastSeenDescendingFromRepository() {
        Incident latest = incident(2L, "checkout-service", "fp-2", SECOND_TIMESTAMP);
        Incident oldest = incident(1L, "payment-service", "fp-1", FIRST_TIMESTAMP);

        when(incidentRepository.findAllByOrderByLastSeenDesc()).thenReturn(List.of(latest, oldest));

        List<IncidentResponse> responses = incidentService.findAllIncidents();

        assertEquals(2, responses.size());
        assertEquals(2L, responses.getFirst().id());
        assertEquals(1L, responses.getLast().id());
    }

    @Test
    void returnsIncidentTimelineWithIncidentAndAnomalies() {
        Incident incident = incident(1L, "payment-service", "fp-1", SECOND_TIMESTAMP);
        IncidentAnomaly firstAnomaly = new IncidentAnomaly(incident, 1002L, "DATABASE_ERROR", FIRST_TIMESTAMP);
        IncidentAnomaly secondAnomaly = new IncidentAnomaly(incident, 1003L, "HTTP_SERVER_ERROR", SECOND_TIMESTAMP);

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.existsById(1L)).thenReturn(true);
        when(incidentAnomalyRepository.findByIncidentIdOrderByDetectedAtAsc(1L))
                .thenReturn(List.of(firstAnomaly, secondAnomaly));

        IncidentTimelineResponse timeline = incidentService.findIncidentTimeline(1L);

        assertEquals(1L, timeline.incident().id());
        assertEquals(2, timeline.anomalies().size());
        assertEquals("DATABASE_ERROR", timeline.anomalies().getFirst().anomalyType());
        assertEquals("HTTP_SERVER_ERROR", timeline.anomalies().getLast().anomalyType());
    }

    @Test
    void returnsIncidentAnomaliesOnlyWhenIncidentExists() {
        Incident incident = incident(1L, "payment-service", "fp-1", FIRST_TIMESTAMP);
        IncidentAnomaly anomaly = new IncidentAnomaly(incident, 1002L, "DATABASE_ERROR", FIRST_TIMESTAMP);

        when(incidentRepository.existsById(1L)).thenReturn(true);
        when(incidentAnomalyRepository.findByIncidentIdOrderByDetectedAtAsc(1L)).thenReturn(List.of(anomaly));

        List<IncidentAnomalyResponse> anomalies = incidentService.findIncidentAnomalies(1L);

        assertEquals(1, anomalies.size());
        assertEquals(1002L, anomalies.getFirst().logId());
        verify(incidentAnomalyRepository).findByIncidentIdOrderByDetectedAtAsc(1L);
    }

    @Test
    void updatesIncidentStatus() {
        Incident incident = incident(1L, "payment-service", "fp-1", FIRST_TIMESTAMP);

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(incident)).thenReturn(incident);

        IncidentResponse response = incidentService.updateStatus(1L, IncidentStatus.INVESTIGATING);

        assertEquals(IncidentStatus.INVESTIGATING, response.status());
        verify(incidentRepository).save(incident);
    }

    @Test
    void throwsWhenIncidentDoesNotExist() {
        when(incidentRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> incidentService.findIncident(99L)
        );

        assertEquals("Incident not found: 99", exception.getMessage());
    }

    private Incident incident(Long id, String serviceName, String fingerprint, LocalDateTime lastSeen) {
        Incident incident = new Incident(
                "DATABASE_ERROR detected on " + serviceName,
                serviceName,
                "production",
                fingerprint,
                IncidentSeverity.HIGH,
                IncidentStatus.OPEN,
                1,
                FIRST_TIMESTAMP,
                lastSeen
        );
        incident.setId(id);
        return incident;
    }
}
