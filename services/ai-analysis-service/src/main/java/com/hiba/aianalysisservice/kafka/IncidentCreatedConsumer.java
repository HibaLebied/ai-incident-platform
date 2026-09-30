package com.hiba.aianalysisservice.kafka;

import com.hiba.aianalysisservice.event.IncidentEvent;
import com.hiba.aianalysisservice.service.IncidentProjectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class IncidentCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(IncidentCreatedConsumer.class);

    private final IncidentProjectionService projectionService;

    public IncidentCreatedConsumer(IncidentProjectionService projectionService) {
        this.projectionService = projectionService;
    }

    @KafkaListener(
            topics = {"incidents.created", "incidents.updated", "incidents.resolved"},
            groupId = "ai-analysis-group"
    )
    public void consume(IncidentEvent event) {
        log.info("Received incident event type={} incidentId={} eventId={}",
                event == null ? null : event.eventType(),
                event == null ? null : event.incidentId(),
                event == null ? null : event.eventId());
        projectionService.project(event);
    }
}
