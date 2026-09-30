package com.hiba.aianalysisservice.service;

import com.hiba.aianalysisservice.rag.EmbeddingService;
import com.hiba.aianalysisservice.rag.KnowledgeDocument;
import com.hiba.aianalysisservice.rag.KnowledgeDocumentType;
import com.hiba.aianalysisservice.rag.RagProperties;
import com.hiba.aianalysisservice.rag.RagSearchResult;
import com.hiba.aianalysisservice.repository.KnowledgeDocumentRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RagRetrievalServiceTest {

    @Test
    void embedsQueryAndDelegatesTopKToVectorRepository() {
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        KnowledgeDocumentRepository repository = mock(KnowledgeDocumentRepository.class);
        RagProperties properties = new RagProperties();
        properties.setDefaultTopK(3);
        float[] embedding = {1, 0};
        when(embeddingService.embed("database error")).thenReturn(embedding);
        RagSearchResult result = new RagSearchResult(new KnowledgeDocument(
                1L, "doc-1", "DB guide", "connection pool", "docs",
                KnowledgeDocumentType.DOCUMENTATION, 0, embedding, null, null, null), 0.9);
        when(repository.searchSimilar(embedding, 3)).thenReturn(List.of(result));

        var actual = new RagRetrievalService(embeddingService, repository, properties)
                .retrieveRelevantContext("database error", null);

        assertThat(actual).containsExactly(result);
        verify(repository).searchSimilar(embedding, 3);
    }
}
