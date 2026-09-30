package com.hiba.aianalysisservice.service;

import com.hiba.aianalysisservice.rag.DocumentChunker;
import com.hiba.aianalysisservice.rag.EmbeddingService;
import com.hiba.aianalysisservice.rag.KnowledgeDocument;
import com.hiba.aianalysisservice.rag.KnowledgeDocumentType;
import com.hiba.aianalysisservice.rag.RagProperties;
import com.hiba.aianalysisservice.repository.KnowledgeDocumentRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class KnowledgeDocumentIngestionServiceTest {

    @Test
    void reingestingSameDocumentUsesSameDocumentAndChunkKeys() {
        RagProperties properties = new RagProperties();
        properties.getChunking().setMaxSize(20);
        properties.getChunking().setOverlap(2);
        DocumentChunker chunker = new DocumentChunker(properties);
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        when(embeddingService.embed(any())).thenReturn(new float[]{1, 0, 0, 0, 0, 0, 0, 0});
        KnowledgeDocumentRepository repository = mock(KnowledgeDocumentRepository.class);
        KnowledgeDocumentIngestionService service = new KnowledgeDocumentIngestionService(chunker, embeddingService, repository);

        service.ingest("runbook-1", "DB runbook", "docs", KnowledgeDocumentType.RUNBOOK,
                "restart connection pool", null);
        service.ingest("runbook-1", "DB runbook", "docs", KnowledgeDocumentType.RUNBOOK,
                "restart connection pool", null);

        verify(repository, times(2)).deleteByDocumentId("runbook-1");
        var captor = org.mockito.ArgumentCaptor.forClass(KnowledgeDocument.class);
        verify(repository, times(4)).upsert(captor.capture());
        assertThat(captor.getAllValues()).extracting(KnowledgeDocument::documentId)
                .containsOnly("runbook-1");
        assertThat(captor.getAllValues()).extracting(KnowledgeDocument::chunkIndex)
                .containsExactly(0, 1, 0, 1);
    }
}
