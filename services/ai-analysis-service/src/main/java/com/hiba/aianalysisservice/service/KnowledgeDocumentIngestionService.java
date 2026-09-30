package com.hiba.aianalysisservice.service;

import com.hiba.aianalysisservice.rag.DocumentChunk;
import com.hiba.aianalysisservice.rag.DocumentChunker;
import com.hiba.aianalysisservice.rag.EmbeddingService;
import com.hiba.aianalysisservice.rag.KnowledgeDocument;
import com.hiba.aianalysisservice.rag.KnowledgeDocumentType;
import com.hiba.aianalysisservice.repository.KnowledgeDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.nio.charset.StandardCharsets;

@Service
public class KnowledgeDocumentIngestionService {

    private final DocumentChunker chunker;
    private final EmbeddingService embeddingService;
    private final KnowledgeDocumentRepository repository;

    public KnowledgeDocumentIngestionService(DocumentChunker chunker,
                                             EmbeddingService embeddingService,
                                             KnowledgeDocumentRepository repository) {
        this.chunker = chunker;
        this.embeddingService = embeddingService;
        this.repository = repository;
    }

    @Transactional
    public int ingest(String documentId, String title, String source,
                      KnowledgeDocumentType documentType, String content, String metadata) {
        if (isBlank(documentId) || isBlank(title) || isBlank(source) || documentType == null) {
            throw new IllegalArgumentException("Document identity and type are required");
        }
        var chunks = chunker.chunk(content);
        Instant now = Instant.now();
        repository.deleteByDocumentId(documentId);
        for (DocumentChunk chunk : chunks) {
            repository.upsert(new KnowledgeDocument(
                    null, documentId, title, chunk.content(), source, documentType,
                    chunk.chunkIndex(), embeddingService.embed(chunk.content()), metadata, now, now));
        }
        return chunks.size();
    }

    public int ingestFile(Path path, String documentId, String title,
                          String source, KnowledgeDocumentType documentType, String metadata) {
        try {
            return ingest(documentId, title, source, documentType, Files.readString(path), metadata);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read knowledge document: " + path, exception);
        }
    }

    public int ingestFileContent(String documentId, String title, String source,
                                 KnowledgeDocumentType documentType, byte[] content, String metadata) {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("Document file must not be empty");
        }
        return ingest(documentId, title, source, documentType,
                new String(content, StandardCharsets.UTF_8), metadata);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
