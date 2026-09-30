package com.hiba.aianalysisservice.controller;

import com.hiba.aianalysisservice.service.HistoricalIncidentIndexingService;
import com.hiba.aianalysisservice.service.KnowledgeDocumentIngestionService;
import com.hiba.aianalysisservice.service.RagRetrievalService;
import com.hiba.aianalysisservice.rag.KnowledgeDocumentType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagRetrievalService retrievalService;
    private final KnowledgeDocumentIngestionService ingestionService;
    private final HistoricalIncidentIndexingService historicalIndexingService;

    public RagController(RagRetrievalService retrievalService,
                         KnowledgeDocumentIngestionService ingestionService,
                         HistoricalIncidentIndexingService historicalIndexingService) {
        this.retrievalService = retrievalService;
        this.ingestionService = ingestionService;
        this.historicalIndexingService = historicalIndexingService;
    }

    @PostMapping("/search")
    public RagSearchResponse search(@RequestBody RagSearchRequest request) {
        return RagSearchResponse.from(request.query(), retrievalService.retrieveRelevantContext(request.query(), request.topK()));
    }

    @PostMapping("/documents")
    public ResponseEntity<Integer> ingest(@RequestBody KnowledgeDocumentRequest request) {
        return ResponseEntity.ok(ingestionService.ingest(request.documentId(), request.title(), request.source(),
                request.documentType(), request.content(), request.metadata()));
    }

    @PostMapping(value = "/documents/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Integer> ingestFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam("documentId") String documentId,
            @RequestParam("title") String title,
            @RequestParam("source") String source,
            @RequestParam("documentType") KnowledgeDocumentType documentType,
            @RequestParam(value = "metadata", required = false) String metadata
    ) throws IOException {
        return ResponseEntity.ok(ingestionService.ingestFileContent(
                documentId, title, source, documentType, file.getBytes(), metadata));
    }

    @PostMapping("/historical-incidents/{incidentId}")
    public ResponseEntity<Integer> indexHistoricalIncident(@PathVariable Long incidentId) {
        return ResponseEntity.ok(historicalIndexingService.index(incidentId));
    }
}
