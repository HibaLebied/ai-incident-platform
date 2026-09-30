package com.hiba.logprocessingservice.processor;

import com.hiba.logprocessingservice.event.LogCreatedEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LogValidatorTest {

    private final LogValidator validator = new LogValidator();

    private LogCreatedEvent validEvent() {
        return new LogCreatedEvent(
                1L,
                "payment-service",
                "ERROR",
                "GET /api/users returned 500",
                LocalDateTime.now(),
                "production",
                "trace-123"
        );
    }

    @Test
    void shouldAcceptValidLog() {
        LogCreatedEvent event = validEvent();

        assertDoesNotThrow(() -> validator.validate(event));
    }

    @Test
    void shouldRejectNullEvent() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(null)
        );
    }

    @Test
    void shouldRejectNullLogId() {
        LogCreatedEvent event = new LogCreatedEvent(
                null,
                "payment-service",
                "ERROR",
                "Something went wrong",
                LocalDateTime.now(),
                "production",
                "trace-123"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(event)
        );
    }

    @Test
    void shouldRejectBlankServiceName() {
        LogCreatedEvent event = new LogCreatedEvent(
                1L,
                "   ",
                "ERROR",
                "Something went wrong",
                LocalDateTime.now(),
                "production",
                "trace-123"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(event)
        );
    }

    @Test
    void shouldRejectBlankMessage() {
        LogCreatedEvent event = new LogCreatedEvent(
                1L,
                "payment-service",
                "ERROR",
                "   ",
                LocalDateTime.now(),
                "production",
                "trace-123"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(event)
        );
    }
}