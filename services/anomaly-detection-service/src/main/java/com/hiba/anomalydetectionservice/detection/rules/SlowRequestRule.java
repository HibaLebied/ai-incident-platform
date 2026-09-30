package com.hiba.anomalydetectionservice.detection.rules;

import com.hiba.anomalydetectionservice.detection.AnomalyDetectionResult;
import com.hiba.anomalydetectionservice.detection.AnomalyRule;
import com.hiba.anomalydetectionservice.detection.AnomalyType;
import com.hiba.anomalydetectionservice.event.LogProcessedEvent;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SlowRequestRule implements AnomalyRule {

    @Override
    public Optional<AnomalyDetectionResult> detect(
            LogProcessedEvent event
    ) {

        if (Boolean.TRUE.equals(event.slowRequest())) {

            return Optional.of(
                    new AnomalyDetectionResult(
                            event.logId(),
                            event.serviceName(),
                            event.environment(),
                            event.traceId(),
                            event.timestamp(),

                            AnomalyType.SLOW_REQUEST,

                            "Slow request detected",

                            event.fingerprint(),
                            event.category(),
                            event.severityScore()
                    )
            );
        }

        return Optional.empty();
    }
}