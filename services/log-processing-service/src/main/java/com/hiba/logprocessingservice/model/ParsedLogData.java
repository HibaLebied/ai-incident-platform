package com.hiba.logprocessingservice.model;

public record ParsedLogData(
        String httpMethod,
        String endpoint,
        Integer httpStatus,
        Long responseTimeMs,
        String ipAddress,
        Integer port
) {
}