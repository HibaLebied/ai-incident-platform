package com.hiba.logprocessingservice.processor;

import com.hiba.logprocessingservice.event.LogCreatedEvent;
import org.springframework.stereotype.Component;

@Component
public class LogValidator {

    public void validate(LogCreatedEvent event) {

        if (event == null) {
            throw new IllegalArgumentException("Log event cannot be null");
        }

        if (event.logId() == null) {
            throw new IllegalArgumentException("logId cannot be null");
        }

        if (isBlank(event.serviceName())) {
            throw new IllegalArgumentException("serviceName cannot be empty");
        }

        if (isBlank(event.level())) {
            throw new IllegalArgumentException("level cannot be empty");
        }

        if (isBlank(event.message())) {
            throw new IllegalArgumentException("message cannot be empty");
        }

        if (event.timestamp() == null) {
            throw new IllegalArgumentException("timestamp cannot be null");
        }

        if (isBlank(event.environment())) {
            throw new IllegalArgumentException("environment cannot be empty");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}