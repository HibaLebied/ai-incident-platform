package com.hiba.logprocessingservice.producer;

import com.hiba.logprocessingservice.event.LogProcessedEvent;
import com.hiba.logprocessingservice.model.ProcessedLogData;
import org.springframework.stereotype.Component;

@Component
public class LogProcessedEventMapper {

    public LogProcessedEvent toEvent(ProcessedLogData data) {

        return new LogProcessedEvent(
                data.originalLogId(),
                data.serviceName(),
                data.level(),
                data.message(),
                data.timestamp(),
                data.environment(),
                data.traceId(),
                data.category(),
                data.severityScore(),
                data.httpMethod(),
                data.endpoint(),
                data.httpStatus(),
                data.responseTimeMs(),
                data.ipAddress(),
                data.port(),
                data.error(),
                data.messageLength(),
                data.httpError(),
                data.slowRequest(),
                data.fingerprint(),
                data.processedAt()
        );
    }
}