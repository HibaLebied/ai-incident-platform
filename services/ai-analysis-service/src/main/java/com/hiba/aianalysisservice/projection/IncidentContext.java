package com.hiba.aianalysisservice.projection;

import java.util.List;

public record IncidentContext(
        IncidentProjection incident,
        List<IncidentAnomalyProjection> anomalies,
        List<IncidentTimelineEvent> timeline
) {
}
