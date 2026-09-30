package com.hiba.aianalysisservice.controller;

import com.hiba.aianalysisservice.rag.KnowledgeDocumentType;

public record KnowledgeDocumentRequest(String documentId, String title, String source,
                                       KnowledgeDocumentType documentType, String content, String metadata) {
}
