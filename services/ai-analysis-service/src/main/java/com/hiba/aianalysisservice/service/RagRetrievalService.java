package com.hiba.aianalysisservice.service;

import com.hiba.aianalysisservice.rag.EmbeddingService;
import com.hiba.aianalysisservice.rag.RagProperties;
import com.hiba.aianalysisservice.rag.RagSearchResult;
import com.hiba.aianalysisservice.repository.KnowledgeDocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RagRetrievalService {

    private final EmbeddingService embeddingService;
    private final KnowledgeDocumentRepository repository;
    private final RagProperties properties;

    public RagRetrievalService(EmbeddingService embeddingService,
                               KnowledgeDocumentRepository repository,
                               RagProperties properties) {
        this.embeddingService = embeddingService;
        this.repository = repository;
        this.properties = properties;
    }

    public List<RagSearchResult> retrieveRelevantContext(String query, Integer topK) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query must not be blank");
        }
        int limit = topK == null ? properties.getDefaultTopK() : topK;
        if (limit <= 0) {
            throw new IllegalArgumentException("topK must be positive");
        }
        return repository.searchSimilar(embeddingService.embed(query), limit);
    }
}
