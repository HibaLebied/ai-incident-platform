package com.hiba.anomalydetectionservice.detection;

import com.hiba.anomalydetectionservice.event.LogProcessedEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AnomalyDetector {

    private final List<AnomalyRule> rules;

    public AnomalyDetector(List<AnomalyRule> rules) {
        this.rules = rules;
    }

    public List<AnomalyDetectionResult> detect(
            LogProcessedEvent event
    ) {
        return rules.stream()
                .map(rule -> rule.detect(event))
                .flatMap(Optional::stream)
                .toList();
    }
}