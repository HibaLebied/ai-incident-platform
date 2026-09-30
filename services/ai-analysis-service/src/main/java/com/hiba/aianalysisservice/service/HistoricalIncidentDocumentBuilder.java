package com.hiba.aianalysisservice.service;

import com.hiba.aianalysisservice.projection.IncidentContext;
import org.springframework.stereotype.Component;

public interface HistoricalIncidentDocumentBuilder {
    String build(IncidentContext context);
}
