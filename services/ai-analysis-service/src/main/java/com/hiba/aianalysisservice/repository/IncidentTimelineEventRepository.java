package com.hiba.aianalysisservice.repository;

import com.hiba.aianalysisservice.projection.IncidentTimelineEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncidentTimelineEventRepository extends JpaRepository<IncidentTimelineEvent, Long> {

    Optional<IncidentTimelineEvent> findBySourceEventIdAndEventType(UUID sourceEventId, String eventType);

    List<IncidentTimelineEvent> findByIncidentIdOrderByOccurredAtAscIdAsc(Long incidentId);
}
