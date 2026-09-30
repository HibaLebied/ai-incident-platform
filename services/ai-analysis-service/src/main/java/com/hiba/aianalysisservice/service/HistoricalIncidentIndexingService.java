package com.hiba.aianalysisservice.service;

import com.hiba.aianalysisservice.rag.KnowledgeDocumentType;
import org.springframework.stereotype.Service;

@Service
public class HistoricalIncidentIndexingService {

    private final IncidentProjectionService projectionService;
    private final HistoricalIncidentDocumentBuilder documentBuilder;
    private final KnowledgeDocumentIngestionService ingestionService;

    public HistoricalIncidentIndexingService(IncidentProjectionService projectionService,
                                             HistoricalIncidentDocumentBuilder documentBuilder,
                                             KnowledgeDocumentIngestionService ingestionService) {
        this.projectionService = projectionService;
        this.documentBuilder = documentBuilder;
        this.ingestionService = ingestionService;
    }

    public int index(Long incidentId) {
        var context = projectionService.findContext(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident projection not found: " + incidentId));
        return ingestionService.ingest(
                "incident-" + incidentId,
                "Historical incident " + incidentId,
                "incident-service",
                KnowledgeDocumentType.HISTORICAL_INCIDENT,
                documentBuilder.build(context),
                "{\"incidentId\":" + incidentId + "}");
    }
}
