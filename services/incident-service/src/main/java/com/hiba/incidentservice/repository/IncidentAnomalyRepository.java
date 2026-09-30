package com.hiba.incidentservice.repository;

import com.hiba.incidentservice.entity.IncidentAnomaly;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentAnomalyRepository extends JpaRepository<IncidentAnomaly, Long> {
    boolean existsByIncidentIdAndLogIdAndAnomalyType(Long incidentId, Long logId, String anomalyType);

    List<IncidentAnomaly> findByIncidentId(Long incidentId);

    List<IncidentAnomaly> findByIncidentIdOrderByDetectedAtAsc(Long incidentId);
}
