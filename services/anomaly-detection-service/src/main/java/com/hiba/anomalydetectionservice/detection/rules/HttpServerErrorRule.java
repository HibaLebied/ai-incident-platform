package com.hiba.anomalydetectionservice.detection.rules;

import com.hiba.anomalydetectionservice.detection.AnomalyDetectionResult;
import com.hiba.anomalydetectionservice.detection.AnomalyRule;
import com.hiba.anomalydetectionservice.detection.AnomalyType;
import com.hiba.anomalydetectionservice.event.LogProcessedEvent;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class HttpServerErrorRule implements AnomalyRule {

    @Override
    public Optional<AnomalyDetectionResult> detect(
            LogProcessedEvent event
    ) {

        Integer status = event.httpStatus();

        if (status != null
                && status >= 500
                && status <= 599) {

            return Optional.of(
                    new AnomalyDetectionResult(
                            event.logId(),
                            event.serviceName(),
                            event.environment(),
                            event.traceId(),
                            event.timestamp(),

                            AnomalyType.HTTP_SERVER_ERROR,

                            "HTTP server error detected",

                            event.fingerprint(),
                            event.category(),
                            event.severityScore()
                    )
            );
        }

        return Optional.empty();
    }
}