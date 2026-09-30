package com.hiba.incidentservice.controller;

import com.hiba.incidentservice.dto.IncidentAnomalyResponse;
import com.hiba.incidentservice.dto.IncidentResponse;
import com.hiba.incidentservice.dto.IncidentTimelineResponse;
import com.hiba.incidentservice.dto.UpdateIncidentStatusRequest;
import com.hiba.incidentservice.service.IncidentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {
    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    public List<IncidentResponse> findAllIncidents() {
        return incidentService.findAllIncidents();
    }

    @GetMapping("/{incidentId}")
    public IncidentResponse findIncident(@PathVariable Long incidentId) {
        return incidentService.findIncident(incidentId);
    }

    @GetMapping("/{incidentId}/anomalies")
    public List<IncidentAnomalyResponse> findIncidentAnomalies(@PathVariable Long incidentId) {
        return incidentService.findIncidentAnomalies(incidentId);
    }

    @GetMapping("/{incidentId}/timeline")
    public IncidentTimelineResponse findIncidentTimeline(@PathVariable Long incidentId) {
        return incidentService.findIncidentTimeline(incidentId);
    }

    @PatchMapping("/{incidentId}/status")
    public IncidentResponse updateStatus(@PathVariable Long incidentId,
                                         @Valid @RequestBody UpdateIncidentStatusRequest request) {
        return incidentService.updateStatus(incidentId, request.status());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", exception.getMessage()));
    }
}
