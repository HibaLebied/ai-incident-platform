package com.hiba.aianalysisservice.rag;

import java.time.Instant;

public record KnowledgeDocument(
        Long id,
        String documentId,
        String title,
        String content,
        String source,
        KnowledgeDocumentType documentType,
        int chunkIndex,
        float[] embedding,
        String metadata,
        Instant createdAt,
        Instant updatedAt
) {
}
