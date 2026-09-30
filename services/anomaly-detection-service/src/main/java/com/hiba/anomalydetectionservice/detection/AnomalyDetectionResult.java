package com.hiba.anomalydetectionservice.detection;

import java.time.LocalDateTime;

public record AnomalyDetectionResult(

        Long logId,
        String serviceName,
        String environment,
        String traceId,
        LocalDateTime timestamp,

        AnomalyType anomalyType,

        String description,

        String fingerprint,
        String category,
        Integer severityScore
) {}