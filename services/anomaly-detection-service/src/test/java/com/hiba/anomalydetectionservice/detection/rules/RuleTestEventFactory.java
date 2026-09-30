package com.hiba.anomalydetectionservice.detection.rules;

import com.hiba.anomalydetectionservice.event.LogProcessedEvent;

import java.time.LocalDateTime;

final class RuleTestEventFactory {

    private RuleTestEventFactory() {
    }

    static LogProcessedEvent event(
            String category,
            Boolean error,
            Integer severityScore,
            Integer httpStatus,
            Boolean slowRequest
    ) {
        return new LogProcessedEvent(
                1002L,
                "payment-service",
                "ERROR",
                "test message",
                LocalDateTime.of(2026, 9, 24, 18, 0),
                "production",
                "trace-1002",
                category,
                severityScore,
                "GET",
                "/payments",
                httpStatus,
                1200L,
                null,
                null,
                error,
                12,
                httpStatus != null && httpStatus >= 400,
                slowRequest,
                "fingerprint-1002",
                LocalDateTime.of(2026, 9, 24, 18, 0)
        );
    }
}
