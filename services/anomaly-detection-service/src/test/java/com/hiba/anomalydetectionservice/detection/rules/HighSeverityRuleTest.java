package com.hiba.anomalydetectionservice.detection.rules;

import com.hiba.anomalydetectionservice.detection.AnomalyType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HighSeverityRuleTest {

    private final HighSeverityRule rule = new HighSeverityRule();

    @Test
    void detectsSeverityScoreAtThreshold() {
        var result = rule.detect(RuleTestEventFactory.event("APPLICATION", false, 4, null, false));

        assertTrue(result.isPresent());
        assertEquals(AnomalyType.HIGH_SEVERITY, result.get().anomalyType());
    }

    @Test
    void detectsMaximumSeverityScore() {
        var result = rule.detect(RuleTestEventFactory.event("APPLICATION", true, 5, null, false));

        assertTrue(result.isPresent());
    }

    @Test
    void ignoresScoreBelowThreshold() {
        var result = rule.detect(RuleTestEventFactory.event("APPLICATION", false, 3, null, false));

        assertFalse(result.isPresent());
    }

    @Test
    void ignoresMissingSeverityScore() {
        var result = rule.detect(RuleTestEventFactory.event("APPLICATION", false, null, null, false));

        assertFalse(result.isPresent());
    }
}
