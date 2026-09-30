package com.hiba.aianalysisservice.service;

import com.hiba.aianalysisservice.domain.IncidentSeverity;
import com.hiba.aianalysisservice.domain.IncidentStatus;
import com.hiba.aianalysisservice.event.IncidentEvent;
import com.hiba.aianalysisservice.projection.IncidentContext;
import com.hiba.aianalysisservice.projection.IncidentProjection;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HistoricalIncidentDocumentBuilderTest {

    @Test
    void buildsStableSearchableIncidentText() {
        IncidentEvent event = new IncidentEvent(UUID.randomUUID(), "IncidentCreated", 2,
                Instant.parse("2026-09-20T10:00:00Z"), "incident-service", "corr-1", 42L,
                "Database error detected on payment-service", "payment-service", "production", "fp-1",
                IncidentSeverity.HIGH, IncidentStatus.OPEN, 1,
                LocalDateTime.of(2026, 9, 20, 10, 0), LocalDateTime.of(2026, 9, 20, 10, 0));

        String document = new DeterministicHistoricalIncidentDocumentBuilder()
                .build(new IncidentContext(new IncidentProjection(event), List.of(), List.of()));

        assertThat(document).contains("Historical incident 42", "payment-service", "production", "Database error");
    }
}
