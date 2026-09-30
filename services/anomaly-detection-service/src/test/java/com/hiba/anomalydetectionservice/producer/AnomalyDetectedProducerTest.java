package com.hiba.anomalydetectionservice.producer;

import com.hiba.anomalydetectionservice.detection.AnomalyType;
import com.hiba.anomalydetectionservice.event.AnomalyDetectedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnomalyDetectedProducerTest {

    @Mock
    private KafkaTemplate<String, AnomalyDetectedEvent> kafkaTemplate;

    private final AnomalyDetectedEvent event = new AnomalyDetectedEvent(
            1002L,
            "payment-service",
            "production",
            "trace-1002",
            LocalDateTime.of(2026, 9, 24, 18, 0),
            AnomalyType.DATABASE_ERROR,
            "Database error detected",
            "fingerprint-1002",
            "DATABASE",
            4
    );

    @Test
    void waitsForSuccessfulKafkaPublication() {
        CompletableFuture<SendResult<String, AnomalyDetectedEvent>> future =
                CompletableFuture.completedFuture(null);

        when(kafkaTemplate.send("anomalies.detected", "payment-service|production|fingerprint-1002", event))
                .thenReturn(future);

        AnomalyDetectedProducer producer = new AnomalyDetectedProducer(kafkaTemplate);

        assertDoesNotThrow(() -> producer.publish(event));
        assertNotNull(event.eventId());
        assertEquals("AnomalyDetected", event.eventType());
        assertEquals(1, event.schemaVersion());
        assertEquals("anomaly-detection-service", event.source());
        assertEquals("trace-1002", event.correlationId());
        assertNotNull(event.occurredAt());
        verify(kafkaTemplate).send(
                "anomalies.detected",
                "payment-service|production|fingerprint-1002",
                event
        );
    }

    @Test
    void propagatesKafkaPublicationFailure() {
        CompletableFuture<SendResult<String, AnomalyDetectedEvent>> future =
                CompletableFuture.failedFuture(new IllegalStateException("Kafka unavailable"));

        when(kafkaTemplate.send(
                eq("anomalies.detected"),
                eq("payment-service|production|fingerprint-1002"),
                eq(event)
        ))
                .thenReturn(future);

        AnomalyDetectedProducer producer = new AnomalyDetectedProducer(kafkaTemplate);

        assertThrows(IllegalStateException.class, () -> producer.publish(event));
    }
}
