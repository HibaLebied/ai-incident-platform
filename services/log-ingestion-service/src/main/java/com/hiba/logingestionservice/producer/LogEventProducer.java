package com.hiba.logingestionservice.producer;

import com.hiba.logingestionservice.event.LogCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class LogEventProducer {

    private final KafkaTemplate<String, LogCreatedEvent> kafkaTemplate;
    public LogEventProducer(
            KafkaTemplate<String, LogCreatedEvent> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    private static final String TOPIC = "logs.raw";

    public void send(LogCreatedEvent event) {

        kafkaTemplate.send(
                TOPIC,
                event.logId().toString(),
                event
        );
    }
}
