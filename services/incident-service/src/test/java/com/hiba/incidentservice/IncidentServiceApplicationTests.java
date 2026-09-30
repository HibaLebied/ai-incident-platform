package com.hiba.incidentservice;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class IncidentServiceApplicationTests {

    @Test
    void mainClassCanBeReferenced() {
        assertDoesNotThrow(() -> Class.forName(IncidentServiceApplication.class.getName()));
    }
}
