package com.hiba.incidentservice.repository;

import com.hiba.incidentservice.entity.Incident;
import com.hiba.incidentservice.enums.IncidentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    Optional<Incident> findFirstByServiceNameAndEnvironmentAndFingerprintAndStatus(
            String serviceName,
            String environment,
            String fingerprint,
            IncidentStatus status
    );

    List<Incident> findAllByOrderByLastSeenDesc();
}
