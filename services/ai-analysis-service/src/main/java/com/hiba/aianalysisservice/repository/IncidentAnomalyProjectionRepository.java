package com.hiba.aianalysisservice.repository;

import com.hiba.aianalysisservice.projection.IncidentAnomalyProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IncidentAnomalyProjectionRepository extends JpaRepository<IncidentAnomalyProjection, Long> {

    Optional<IncidentAnomalyProjection> findByIncidentProjection_IncidentIdAndLogIdAndAnomalyType(
            Long incidentId,
            Long logId,
            String anomalyType
    );

    List<IncidentAnomalyProjection> findByIncidentProjection_IncidentIdOrderByDetectedAtAsc(Long incidentId);
}
