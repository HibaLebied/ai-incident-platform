package com.hiba.aianalysisservice.config;

import com.hiba.aianalysisservice.domain.IncidentStatus;
import com.hiba.aianalysisservice.event.IncidentEvent;
import com.hiba.aianalysisservice.kafka.IncidentCreatedConsumer;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.nio.charset.StandardCharsets;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class KafkaConfigTest {

    @Test
    void jacksonJsonDeserializerReadsLocalIncidentEventWithoutTypeHeaders() {
        JacksonJsonDeserializer<IncidentEvent> deserializer = new JacksonJsonDeserializer<>(IncidentEvent.class)
                .trustedPackages("com.hiba.aianalysisservice.event")
                .ignoreTypeHeaders();

        IncidentEvent event = deserializer.deserialize(
                "incidents.created",
                """
                        {
                          "eventId":"11111111-1111-1111-1111-111111111111",
                          "eventType":"IncidentCreated",
                          "schemaVersion":2,
                          "occurredAt":"2026-09-15T21:30:00Z",
                          "source":"incident-service",
                          "correlationId":"incident-42",
                          "incidentId":42,
                          "title":"Database failure",
                          "serviceName":"payments-api",
                          "environment":"production",
                          "fingerprint":"fp-42",
                          "severity":"HIGH",
                          "status":"OPEN",
                          "occurrenceCount":1,
                          "firstSeen":"2026-09-15T21:30:00",
                          "lastSeen":"2026-09-15T21:30:00",
                          "anomaly": {
                            "logId":1002,
                            "anomalyType":"DATABASE_ERROR",
                            "description":"Database error detected",
                            "category":"DATABASE",
                            "severityScore":4,
                            "traceId":"trace-1002",
                            "detectedAt":"2026-09-15T21:25:00"
                          }
                        }
                        """.getBytes(StandardCharsets.UTF_8)
        );

        assertEquals(42L, event.incidentId());
        assertEquals(IncidentStatus.OPEN, event.status());
        assertEquals("DATABASE_ERROR", event.anomaly().anomalyType());
        assertEquals(1002L, event.anomaly().logId());
    }

    @Test
    void consumerKeepsExpectedTopicsAndGroup() throws NoSuchMethodException {
        Method consume = IncidentCreatedConsumer.class.getMethod("consume", IncidentEvent.class);
        KafkaListener listener = consume.getAnnotation(KafkaListener.class);

        assertArrayEquals(
                new String[]{"incidents.created", "incidents.updated", "incidents.resolved"},
                listener.topics()
        );
        assertEquals("ai-analysis-group", listener.groupId());
    }
}
