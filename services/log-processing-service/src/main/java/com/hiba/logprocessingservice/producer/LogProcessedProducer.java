package com.hiba.logprocessingservice.producer;

import com.hiba.logprocessingservice.event.LogProcessedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class LogProcessedProducer {

    private static final String TOPIC = "logs.processed";

    private final KafkaTemplate<String, LogProcessedEvent> kafkaTemplate;

    public LogProcessedProducer(
            KafkaTemplate<String, LogProcessedEvent> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(LogProcessedEvent event) {
        kafkaTemplate.send(
                TOPIC,
                event.logId().toString(),
                event
        );
    }
}