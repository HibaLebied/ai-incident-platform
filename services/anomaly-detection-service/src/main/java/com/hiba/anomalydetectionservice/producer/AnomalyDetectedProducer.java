package com.hiba.anomalydetectionservice.producer;

import com.hiba.anomalydetectionservice.event.AnomalyDetectedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class AnomalyDetectedProducer {

    private static final String TOPIC = "anomalies.detected";

    private final KafkaTemplate<String, AnomalyDetectedEvent> kafkaTemplate;

    public AnomalyDetectedProducer(
            KafkaTemplate<String, AnomalyDetectedEvent> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(AnomalyDetectedEvent event) {
        try {
            kafkaTemplate.send(
                    TOPIC,
                    correlationKey(event),
                    event
            ).get(10, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while publishing anomaly event", exception);
        } catch (ExecutionException | TimeoutException exception) {
            throw new IllegalStateException("Failed to publish anomaly event", exception);
        }
    }

    private String correlationKey(AnomalyDetectedEvent event) {
        return event.serviceName()
                + "|" + event.environment()
                + "|" + event.fingerprint();
    }
}
