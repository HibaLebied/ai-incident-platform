package com.hiba.anomalydetectionservice.detection.rules;

import com.hiba.anomalydetectionservice.detection.AnomalyType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpServerErrorRuleTest {

    private final HttpServerErrorRule rule = new HttpServerErrorRule();

    @Test
    void ignoresSuccessfulResponse() {
        var result = rule.detect(RuleTestEventFactory.event("HTTP", false, 2, 200, false));

        assertFalse(result.isPresent());
    }

    @Test
    void ignoresClientError() {
        var result = rule.detect(RuleTestEventFactory.event("HTTP", true, 4, 400, false));

        assertFalse(result.isPresent());
    }

    @Test
    void detectsFirstServerErrorStatus() {
        var result = rule.detect(RuleTestEventFactory.event("HTTP", true, 4, 500, false));

        assertTrue(result.isPresent());
        assertEquals(AnomalyType.HTTP_SERVER_ERROR, result.get().anomalyType());
    }

    @Test
    void detectsLastServerErrorStatus() {
        var result = rule.detect(RuleTestEventFactory.event("HTTP", true, 4, 599, false));

        assertTrue(result.isPresent());
    }

    @Test
    void ignoresStatusOutsideServerErrorRange() {
        var result = rule.detect(RuleTestEventFactory.event("HTTP", true, 4, 600, false));

        assertFalse(result.isPresent());
    }

    @Test
    void ignoresMissingHttpStatus() {
        var result = rule.detect(RuleTestEventFactory.event("HTTP", true, 4, null, false));

        assertFalse(result.isPresent());
    }
}
