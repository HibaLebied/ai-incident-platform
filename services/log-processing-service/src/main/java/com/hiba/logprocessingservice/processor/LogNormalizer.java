package com.hiba.logprocessingservice.processor;

import com.hiba.logprocessingservice.event.LogCreatedEvent;
import com.hiba.logprocessingservice.model.NormalizedLog;
import org.springframework.stereotype.Component;

@Component
public class LogNormalizer {

    public NormalizedLog normalize(LogCreatedEvent event) {

        String serviceName = normalizeServiceName(event.serviceName());
        String level = normalizeLevel(event.level());
        String message = normalizeMessage(event.message());
        String environment = normalizeEnvironment(event.environment());
        String traceId = normalizeTraceId(event.traceId());

        return new NormalizedLog(
                event.logId(),
                serviceName,
                level,
                message,
                event.timestamp(),
                environment,
                traceId
        );
    }

    private String normalizeServiceName(String serviceName) {
        return serviceName
                .trim()
                .toLowerCase();
    }

    private String normalizeLevel(String level) {
        return level
                .trim()
                .toUpperCase();
    }

    private String normalizeMessage(String message) {
        return message
                .trim()
                .replaceAll("\\s+", " ");
    }

    private String normalizeEnvironment(String environment) {
        return environment
                .trim()
                .toLowerCase();
    }

    private String normalizeTraceId(String traceId) {

        if (traceId == null || traceId.isBlank()) {
            return null;
        }

        return traceId.trim();
    }
}