package com.hiba.incidentservice.consumer;

import com.hiba.incidentservice.event.AnomalyDetectedEvent;
import com.hiba.incidentservice.service.IncidentCorrelationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AnomalyDetectedConsumer {
    private final IncidentCorrelationService correlationService;

    public AnomalyDetectedConsumer(IncidentCorrelationService correlationService) {
        this.correlationService = correlationService;
    }

    @KafkaListener(
            topics = "anomalies.detected",
            groupId = "incident-service-group"
    )
    public void consume(AnomalyDetectedEvent event) {
        correlationService.process(event);
    }
}
