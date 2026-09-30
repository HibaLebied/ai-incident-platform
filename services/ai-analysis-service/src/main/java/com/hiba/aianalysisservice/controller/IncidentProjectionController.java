package com.hiba.aianalysisservice.controller;

import com.hiba.aianalysisservice.service.IncidentProjectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/incidents")
public class IncidentProjectionController {

    private final IncidentProjectionService projectionService;

    public IncidentProjectionController(IncidentProjectionService projectionService) {
        this.projectionService = projectionService;
    }

    @GetMapping("/{incidentId}")
    public ResponseEntity<IncidentProjectionResponse> findByIncidentId(@PathVariable Long incidentId) {
        return projectionService.findByIncidentId(incidentId)
                .map(IncidentProjectionResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{incidentId}/context")
    public ResponseEntity<IncidentContextResponse> findContext(@PathVariable Long incidentId) {
        return projectionService.findContext(incidentId)
                .map(IncidentContextResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
