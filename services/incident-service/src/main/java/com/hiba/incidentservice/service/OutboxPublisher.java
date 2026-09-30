package com.hiba.incidentservice.service;

import com.hiba.incidentservice.entity.OutboxEvent;
import com.hiba.incidentservice.event.IncidentEvent;
import com.hiba.incidentservice.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, IncidentEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(
            OutboxEventRepository repository,
            KafkaTemplate<String, IncidentEvent> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 2000)
    public void publishEvents() {
        List<OutboxEvent> events = repository.findTop100ByPublishedAtIsNullOrderByCreatedAtAsc();

        for (OutboxEvent outboxEvent : events) {
            try {
                IncidentEvent event = objectMapper.readValue(
                        outboxEvent.getPayload(),
                        IncidentEvent.class
                );

                kafkaTemplate.send(
                        outboxEvent.getTopic(),
                        outboxEvent.getAggregateId(),
                        event
                ).whenComplete((result, exception) -> {
                    if (exception == null) {
                        outboxEvent.setPublishedAt(LocalDateTime.now());
                        repository.save(outboxEvent);
                        log.info("Published incident outbox event {}", outboxEvent.getId());
                    } else {
                        log.error("Failed to publish incident outbox event {}", outboxEvent.getId(), exception);
                    }
                });
            } catch (Exception exception) {
                log.error("Failed to process incident outbox event {}", outboxEvent.getId(), exception);
            }
        }
    }
}
