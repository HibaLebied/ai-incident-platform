package com.hiba.incidentservice.service;

import com.hiba.incidentservice.entity.Incident;
import com.hiba.incidentservice.enums.IncidentStatus;
import com.hiba.incidentservice.event.AnomalyDetectedEvent;
import com.hiba.incidentservice.repository.IncidentRepository;
import com.hiba.incidentservice.repository.IncidentCorrelationLock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class IncidentCorrelationService {
    private final IncidentRepository incidentRepository;
    private final IncidentService incidentService;
    private final IncidentCorrelationLock correlationLock;

    public IncidentCorrelationService(
            IncidentRepository incidentRepository,
            IncidentService incidentService,
            IncidentCorrelationLock correlationLock
    ) {
        this.incidentRepository = incidentRepository;
        this.incidentService = incidentService;
        this.correlationLock = correlationLock;
    }

    @Transactional
    public Incident process(AnomalyDetectedEvent event) {
        correlationLock.lock(event.serviceName(), event.environment(), event.fingerprint());

        Optional<Incident> existingOpenIncident =
                incidentRepository.findFirstByServiceNameAndEnvironmentAndFingerprintAndStatus(
                        event.serviceName(),
                        event.environment(),
                        event.fingerprint(),
                        IncidentStatus.OPEN
                );

        return incidentService.createOrUpdateIncident(event, existingOpenIncident);
    }
}
