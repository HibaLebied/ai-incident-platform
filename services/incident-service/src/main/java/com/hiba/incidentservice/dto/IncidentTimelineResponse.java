package com.hiba.incidentservice.dto;

import java.util.List;

public record IncidentTimelineResponse(
        IncidentResponse incident,
        List<IncidentAnomalyResponse> anomalies
) {
}
