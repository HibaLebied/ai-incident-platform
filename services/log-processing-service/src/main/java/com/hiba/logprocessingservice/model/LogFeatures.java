package com.hiba.logprocessingservice.model;

public record LogFeatures(
        Integer severityScore,
        Boolean error,
        Integer messageLength,
        Boolean httpError,
        Boolean slowRequest
) {
}