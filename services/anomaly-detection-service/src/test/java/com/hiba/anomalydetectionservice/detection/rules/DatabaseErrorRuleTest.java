package com.hiba.anomalydetectionservice.detection.rules;

import com.hiba.anomalydetectionservice.detection.AnomalyType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseErrorRuleTest {

    private final DatabaseErrorRule rule = new DatabaseErrorRule();

    @Test
    void detectsDatabaseError() {
        var result = rule.detect(RuleTestEventFactory.event("DATABASE", true, 4, null, false));

        assertTrue(result.isPresent());
        assertEquals(AnomalyType.DATABASE_ERROR, result.get().anomalyType());
    }

    @Test
    void ignoresDatabaseCategoryWithoutError() {
        var result = rule.detect(RuleTestEventFactory.event("DATABASE", false, 4, null, false));

        assertFalse(result.isPresent());
    }

    @Test
    void ignoresErrorFromAnotherCategory() {
        var result = rule.detect(RuleTestEventFactory.event("HTTP", true, 4, 500, false));

        assertFalse(result.isPresent());
    }

    @Test
    void ignoresMissingErrorFlag() {
        var result = rule.detect(RuleTestEventFactory.event("DATABASE", null, 4, null, false));

        assertFalse(result.isPresent());
    }
}
