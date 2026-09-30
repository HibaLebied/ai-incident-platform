package com.hiba.logprocessingservice.processor;

import com.hiba.logprocessingservice.event.LogCreatedEvent;
import com.hiba.logprocessingservice.model.NormalizedLog;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LogNormalizerTest {

    private final LogNormalizer normalizer = new LogNormalizer();

    @Test
    void shouldNormalizeLog() {

        LogCreatedEvent event = new LogCreatedEvent(
                1L,
                "  Payment-Service  ",
                "error",
                "  GET   /api/users    returned 500   ",
                LocalDateTime.of(2026, 9, 3, 15, 30),
                "  Production ",
                " trace-123 "
        );

        NormalizedLog result = normalizer.normalize(event);

        assertEquals(1L, result.logId());
        assertEquals("payment-service", result.serviceName());
        assertEquals("ERROR", result.level());
        assertEquals(
                "GET /api/users returned 500",
                result.message()
        );
        assertEquals("production", result.environment());
        assertEquals("trace-123", result.traceId());
    }

    @Test
    void shouldUppercaseLevel() {

        LogCreatedEvent event = new LogCreatedEvent(
                1L,
                "payment-service",
                "warn",
                "Something happened",
                LocalDateTime.now(),
                "production",
                "trace-123"
        );

        NormalizedLog result = normalizer.normalize(event);

        assertEquals("WARN", result.level());
    }

    @Test
    void shouldTrimServiceName() {

        LogCreatedEvent event = new LogCreatedEvent(
                1L,
                "   Payment-Service   ",
                "INFO",
                "Application started",
                LocalDateTime.now(),
                "production",
                "trace-123"
        );

        NormalizedLog result = normalizer.normalize(event);

        assertEquals("payment-service", result.serviceName());
    }
}