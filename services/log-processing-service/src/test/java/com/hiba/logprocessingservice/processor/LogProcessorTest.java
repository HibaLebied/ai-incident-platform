package com.hiba.logprocessingservice.processor;

import com.hiba.logprocessingservice.event.LogCreatedEvent;
import com.hiba.logprocessingservice.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogProcessorTest {

    @Mock
    private LogValidator validator;

    @Mock
    private LogNormalizer normalizer;

    @Mock
    private LogClassifier classifier;

    @Mock
    private LogParser parser;

    @Mock
    private FeatureExtractor featureExtractor;

    @Mock
    private FingerprintGenerator fingerprintGenerator;

    @InjectMocks
    private LogProcessor processor;

    @Test
    void shouldProcessLog() {

        LogCreatedEvent event = new LogCreatedEvent(
                11L,
                "payment-service",
                "ERROR",
                "GET /api/users returned 500 in 235ms",
                LocalDateTime.of(
                        2026,
                        9,
                        1,
                        15,
                        30
                ),
                "production",
                "trace-12345"
        );

        NormalizedLog normalized =
                new NormalizedLog(
                        11L,
                        "payment-service",
                        "ERROR",
                        "GET /api/users returned 500 in 235ms",
                        event.timestamp(),
                        "production",
                        "trace-12345"
                );

        ParsedLogData parsed =
                new ParsedLogData(
                        "GET",
                        "/api/users",
                        500,
                        235L,
                        null,
                        null
                );

        LogFeatures features =
                new LogFeatures(
                        4,
                        true,
                        36,
                        true,
                        false
                );

        when(normalizer.normalize(event))
                .thenReturn(normalized);

        when(classifier.classify(normalized.message()))
                .thenReturn(LogCategory.HTTP);

        when(parser.parse(normalized.message()))
                .thenReturn(parsed);

        when(featureExtractor.extract(
                normalized.level(),
                normalized.message(),
                parsed.httpStatus(),
                parsed.responseTimeMs()
        )).thenReturn(features);

        when(fingerprintGenerator.generate(
                normalized.serviceName(),
                normalized.level(),
                normalized.message()
        )).thenReturn("fingerprint-123");

        ProcessedLogData result =
                processor.process(event);

        assertEquals(
                11L,
                result.originalLogId()
        );

        assertEquals(
                "payment-service",
                result.serviceName()
        );

        assertEquals(
                "ERROR",
                result.level()
        );

        assertEquals(
                "HTTP",
                result.category()
        );

        assertEquals(
                4,
                result.severityScore()
        );

        assertEquals(
                "GET",
                result.httpMethod()
        );

        assertEquals(
                "/api/users",
                result.endpoint()
        );

        assertEquals(
                500,
                result.httpStatus()
        );

        assertEquals(
                235L,
                result.responseTimeMs()
        );

        assertTrue(result.error());

        assertTrue(result.httpError());

        assertFalse(result.slowRequest());

        assertEquals(
                "fingerprint-123",
                result.fingerprint()
        );

        verify(validator).validate(event);
        verify(normalizer).normalize(event);
        verify(classifier).classify(normalized.message());
        verify(parser).parse(normalized.message());
        verify(featureExtractor).extract(
                normalized.level(),
                normalized.message(),
                parsed.httpStatus(),
                parsed.responseTimeMs()
        );
        verify(fingerprintGenerator).generate(
                normalized.serviceName(),
                normalized.level(),
                normalized.message()
        );
    }
}