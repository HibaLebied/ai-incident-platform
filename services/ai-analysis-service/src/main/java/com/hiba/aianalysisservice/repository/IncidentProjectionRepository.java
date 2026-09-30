package com.hiba.aianalysisservice.repository;

import com.hiba.aianalysisservice.projection.IncidentProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IncidentProjectionRepository extends JpaRepository<IncidentProjection, Long> {

    Optional<IncidentProjection> findByIncidentId(Long incidentId);
}
