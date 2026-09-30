package com.hiba.anomalydetectionservice.detection.rules;

import com.hiba.anomalydetectionservice.detection.AnomalyDetectionResult;
import com.hiba.anomalydetectionservice.detection.AnomalyRule;
import com.hiba.anomalydetectionservice.event.LogProcessedEvent;
import com.hiba.anomalydetectionservice.detection.AnomalyType;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class HighSeverityRule implements AnomalyRule {

    @Override
    public Optional<AnomalyDetectionResult> detect(
            LogProcessedEvent event
    ) {

        if (event.severityScore() != null
                && event.severityScore() >= 4) {

            return Optional.of(
                    new AnomalyDetectionResult(
                            event.logId(),
                            event.serviceName(),
                            event.environment(),
                            event.traceId(),
                            event.timestamp(),

                            AnomalyType.HIGH_SEVERITY,

                            "High severity log detected",

                            event.fingerprint(),
                            event.category(),
                            event.severityScore()
                    )
            );
        }

        return Optional.empty();
    }
}