package com.hiba.logprocessingservice.model;

import java.time.LocalDateTime;

public record NormalizedLog(
        Long logId,
        String serviceName,
        String level,
        String message,
        LocalDateTime timestamp,
        String environment,
        String traceId
) {
}