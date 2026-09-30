package com.hiba.logprocessingservice.processor;

import com.hiba.logprocessingservice.event.LogCreatedEvent;
import com.hiba.logprocessingservice.model.LogCategory;
import com.hiba.logprocessingservice.model.LogFeatures;
import com.hiba.logprocessingservice.model.NormalizedLog;
import com.hiba.logprocessingservice.model.ParsedLogData;
import com.hiba.logprocessingservice.model.ProcessedLogData;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LogProcessor {

    private final LogValidator validator;
    private final LogNormalizer normalizer;
    private final LogClassifier classifier;
    private final LogParser parser;
    private final FeatureExtractor featureExtractor;
    private final FingerprintGenerator fingerprintGenerator;

    public LogProcessor(
            LogValidator validator,
            LogNormalizer normalizer,
            LogClassifier classifier,
            LogParser parser,
            FeatureExtractor featureExtractor,
            FingerprintGenerator fingerprintGenerator
    ) {
        this.validator = validator;
        this.normalizer = normalizer;
        this.classifier = classifier;
        this.parser = parser;
        this.featureExtractor = featureExtractor;
        this.fingerprintGenerator = fingerprintGenerator;
    }

    public ProcessedLogData process(LogCreatedEvent event) {

        if (event.logId() == 999L) {
            throw new RuntimeException("TEST RETRY DLQ");
        }

        // 1. Validation
        validator.validate(event);

        // 2. Normalization
        NormalizedLog normalized =
                normalizer.normalize(event);

        // 3. Classification
        LogCategory category =
                classifier.classify(normalized.message());

        // 4. Parsing
        ParsedLogData parsed =
                parser.parse(normalized.message());

        // 5. Feature extraction
        LogFeatures features =
                featureExtractor.extract(
                        normalized.level(),
                        normalized.message(),
                        parsed.httpStatus(),
                        parsed.responseTimeMs()
                );

        // 6. Fingerprint
        String fingerprint =
                fingerprintGenerator.generate(
                        normalized.serviceName(),
                        normalized.level(),
                        normalized.message()
                );

        // 7. Build final processed representation
        return new ProcessedLogData(
                normalized.logId(),
                normalized.serviceName(),
                normalized.level(),
                normalized.message(),
                normalized.timestamp(),
                normalized.environment(),
                normalized.traceId(),

                category.name(),

                features.severityScore(),

                parsed.httpMethod(),
                parsed.endpoint(),
                parsed.httpStatus(),
                parsed.responseTimeMs(),
                parsed.ipAddress(),
                parsed.port(),

                features.error(),
                features.messageLength(),
                features.httpError(),
                features.slowRequest(),

                fingerprint,

                LocalDateTime.now()
        );
    }
}