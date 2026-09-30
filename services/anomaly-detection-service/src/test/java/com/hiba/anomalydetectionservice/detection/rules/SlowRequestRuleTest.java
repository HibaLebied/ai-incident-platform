package com.hiba.anomalydetectionservice.detection.rules;

import com.hiba.anomalydetectionservice.detection.AnomalyType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlowRequestRuleTest {

    private final SlowRequestRule rule = new SlowRequestRule();

    @Test
    void detectsSlowRequest() {
        var result = rule.detect(RuleTestEventFactory.event("HTTP", false, 2, 200, true));

        assertTrue(result.isPresent());
        assertEquals(AnomalyType.SLOW_REQUEST, result.get().anomalyType());
    }

    @Test
    void ignoresNormalRequest() {
        var result = rule.detect(RuleTestEventFactory.event("HTTP", false, 2, 200, false));

        assertFalse(result.isPresent());
    }

    @Test
    void ignoresMissingSlowRequestFlag() {
        var result = rule.detect(RuleTestEventFactory.event("HTTP", false, 2, 200, null));

        assertFalse(result.isPresent());
    }
}
