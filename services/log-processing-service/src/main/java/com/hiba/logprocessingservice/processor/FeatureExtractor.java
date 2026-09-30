package com.hiba.logprocessingservice.processor;

import com.hiba.logprocessingservice.model.LogFeatures;
import org.springframework.stereotype.Component;

@Component
public class FeatureExtractor {

    public LogFeatures extract(
            String level,
            String message,
            Integer httpStatus,
            Long responseTimeMs
    ) {

        int severityScore = calculateSeverity(level);

        boolean error = "ERROR".equals(level)
                || "FATAL".equals(level);

        boolean httpError = httpStatus != null
                && httpStatus >= 400;

        boolean slowRequest = responseTimeMs != null
                && responseTimeMs >= 1000;

        return new LogFeatures(
                severityScore,
                error,
                message.length(),
                httpError,
                slowRequest
        );
    }

    private int calculateSeverity(String level) {

        return switch (level) {
            case "TRACE" -> 0;
            case "DEBUG" -> 1;
            case "INFO" -> 2;
            case "WARN" -> 3;
            case "ERROR" -> 4;
            case "FATAL" -> 5;
            default -> 0;
        };
    }
}