package com.hiba.logprocessingservice.model;

import java.time.LocalDateTime;

public record ProcessedLogData(

        Long originalLogId,

        String serviceName,

        String level,

        String message,

        LocalDateTime timestamp,

        String environment,

        String traceId,

        String category,

        Integer severityScore,

        String httpMethod,

        String endpoint,

        Integer httpStatus,

        Long responseTimeMs,

        String ipAddress,

        Integer port,

        Boolean error,

        Integer messageLength,

        Boolean httpError,

        Boolean slowRequest,

        String fingerprint,

        LocalDateTime processedAt
) {
}