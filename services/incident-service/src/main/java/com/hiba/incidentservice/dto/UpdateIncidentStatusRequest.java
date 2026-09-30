package com.hiba.incidentservice.dto;

import com.hiba.incidentservice.enums.IncidentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateIncidentStatusRequest(
        @NotNull IncidentStatus status
) {
}
