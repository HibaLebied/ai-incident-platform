package com.hiba.anomalydetectionservice.detection;

import com.hiba.anomalydetectionservice.event.LogProcessedEvent;

import java.util.Optional;

public interface AnomalyRule {

    Optional<AnomalyDetectionResult> detect(
            LogProcessedEvent event
    );
}