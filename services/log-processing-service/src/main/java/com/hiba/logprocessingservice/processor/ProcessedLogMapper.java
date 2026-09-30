package com.hiba.logprocessingservice.processor;

import com.hiba.logprocessingservice.entity.ProcessedLog;
import com.hiba.logprocessingservice.model.ProcessedLogData;
import org.springframework.stereotype.Component;

@Component
public class ProcessedLogMapper {

    public ProcessedLog toEntity(ProcessedLogData data) {

        return ProcessedLog.builder()
                .originalLogId(data.originalLogId())
                .serviceName(data.serviceName())
                .level(data.level())
                .message(data.message())
                .timestamp(data.timestamp())
                .environment(data.environment())
                .traceId(data.traceId())
                .category(data.category())
                .severityScore(data.severityScore())
                .httpMethod(data.httpMethod())
                .endpoint(data.endpoint())
                .httpStatus(data.httpStatus())
                .responseTimeMs(data.responseTimeMs())
                .ipAddress(data.ipAddress())
                .port(data.port())
                .error(data.error())
                .messageLength(data.messageLength())
                .httpError(data.httpError())
                .slowRequest(data.slowRequest())
                .fingerprint(data.fingerprint())
                .processedAt(data.processedAt())
                .build();
    }
}