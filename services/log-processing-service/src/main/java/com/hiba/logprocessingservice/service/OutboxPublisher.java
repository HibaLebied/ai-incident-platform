package com.hiba.logprocessingservice.service;

import tools.jackson.databind.ObjectMapper;
import com.hiba.logprocessingservice.entity.OutboxEvent;
import com.hiba.logprocessingservice.event.LogProcessedEvent;
import com.hiba.logprocessingservice.repository.OutboxEventRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OutboxPublisher {

    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, LogProcessedEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(
            OutboxEventRepository repository,
            KafkaTemplate<String, LogProcessedEvent> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 2000)
    public void publishEvents() {

        List<OutboxEvent> events =
                repository.findTop100ByPublishedAtIsNullOrderByCreatedAtAsc();

        for (OutboxEvent outboxEvent : events) {

            try {

                LogProcessedEvent event =
                        objectMapper.readValue(
                                outboxEvent.getPayload(),
                                LogProcessedEvent.class
                        );

                kafkaTemplate.send(
                        outboxEvent.getTopic(),
                        outboxEvent.getAggregateId(),
                        event
                ).whenComplete((result, exception) -> {

                    if (exception == null) {

                        outboxEvent.setPublishedAt(
                                LocalDateTime.now()
                        );

                        repository.save(outboxEvent);

                        System.out.println(
                                "Outbox event published: "
                                        + outboxEvent.getId()
                        );

                    } else {

                        System.err.println(
                                "Failed to publish outbox event: "
                                        + outboxEvent.getId()
                        );
                    }
                });

            } catch (Exception e) {

                System.err.println(
                        "Failed to process outbox event: "
                                + outboxEvent.getId()
                );

                e.printStackTrace();
            }
        }
    }
}