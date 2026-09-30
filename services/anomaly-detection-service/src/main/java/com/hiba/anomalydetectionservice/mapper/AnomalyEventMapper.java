package com.hiba.anomalydetectionservice.mapper;

import com.hiba.anomalydetectionservice.detection.AnomalyDetectionResult;
import com.hiba.anomalydetectionservice.event.AnomalyDetectedEvent;
import org.springframework.stereotype.Component;

@Component
public class AnomalyEventMapper {

    public AnomalyDetectedEvent toEvent(
            AnomalyDetectionResult result
    ) {

        return new AnomalyDetectedEvent(
                result.logId(),
                result.serviceName(),
                result.environment(),
                result.traceId(),
                result.timestamp(),
                result.anomalyType(),
                result.description(),
                result.fingerprint(),
                result.category(),
                result.severityScore()
        );
    }
}