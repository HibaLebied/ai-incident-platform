package com.hiba.logprocessingservice.processor;

import com.hiba.logprocessingservice.model.LogFeatures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FeatureExtractorTest {

    private final FeatureExtractor extractor = new FeatureExtractor();

    @Test
    void shouldCalculateSeverityScoreForEachLevel() {

        assertEquals(
                0,
                extractor.extract("TRACE", "test", null, null)
                        .severityScore()
        );

        assertEquals(
                1,
                extractor.extract("DEBUG", "test", null, null)
                        .severityScore()
        );

        assertEquals(
                2,
                extractor.extract("INFO", "test", null, null)
                        .severityScore()
        );

        assertEquals(
                3,
                extractor.extract("WARN", "test", null, null)
                        .severityScore()
        );

        assertEquals(
                4,
                extractor.extract("ERROR", "test", null, null)
                        .severityScore()
        );

        assertEquals(
                5,
                extractor.extract("FATAL", "test", null, null)
                        .severityScore()
        );
    }

    @Test
    void shouldMarkErrorForErrorLevel() {

        LogFeatures result =
                extractor.extract(
                        "ERROR",
                        "Database connection failed",
                        null,
                        null
                );

        assertTrue(result.error());
    }

    @Test
    void shouldMarkErrorForFatalLevel() {

        LogFeatures result =
                extractor.extract(
                        "FATAL",
                        "Application crashed",
                        null,
                        null
                );

        assertTrue(result.error());
    }

    @Test
    void shouldNotMarkInfoAsError() {

        LogFeatures result =
                extractor.extract(
                        "INFO",
                        "Application started",
                        null,
                        null
                );

        assertFalse(result.error());
    }

    @Test
    void shouldDetectHttpError() {

        LogFeatures result =
                extractor.extract(
                        "ERROR",
                        "GET /api/users returned 500",
                        500,
                        null
                );

        assertTrue(result.httpError());
    }

    @Test
    void shouldNotDetectHttpErrorForSuccessfulRequest() {

        LogFeatures result =
                extractor.extract(
                        "INFO",
                        "GET /api/users returned 200",
                        200,
                        null
                );

        assertFalse(result.httpError());
    }

    @Test
    void shouldDetectSlowRequest() {

        LogFeatures result =
                extractor.extract(
                        "INFO",
                        "GET /api/users returned 200 in 1500ms",
                        200,
                        1500L
                );

        assertTrue(result.slowRequest());
    }

    @Test
    void shouldNotDetectSlowRequestBelowThreshold() {

        LogFeatures result =
                extractor.extract(
                        "INFO",
                        "GET /api/users returned 200 in 500ms",
                        200,
                        500L
                );

        assertFalse(result.slowRequest());
    }

    @Test
    void shouldConsiderExactly1000msAsSlow() {

        LogFeatures result =
                extractor.extract(
                        "INFO",
                        "GET /api/users returned 200 in 1000ms",
                        200,
                        1000L
                );

        assertTrue(result.slowRequest());
    }

    @Test
    void shouldCalculateMessageLength() {

        String message = "Database connection failed";

        LogFeatures result =
                extractor.extract(
                        "ERROR",
                        message,
                        null,
                        null
                );

        assertEquals(
                message.length(),
                result.messageLength()
        );
    }

    @Test
    void shouldHandleNullHttpStatus() {

        LogFeatures result =
                extractor.extract(
                        "ERROR",
                        "Something failed",
                        null,
                        null
                );

        assertFalse(result.httpError());
    }

    @Test
    void shouldHandleNullResponseTime() {

        LogFeatures result =
                extractor.extract(
                        "ERROR",
                        "Something failed",
                        null,
                        null
                );

        assertFalse(result.slowRequest());
    }

    @Test
    void shouldReturnZeroSeverityForUnknownLevel() {

        LogFeatures result =
                extractor.extract(
                        "UNKNOWN",
                        "Something happened",
                        null,
                        null
                );

        assertEquals(0, result.severityScore());
        assertFalse(result.error());
    }
}