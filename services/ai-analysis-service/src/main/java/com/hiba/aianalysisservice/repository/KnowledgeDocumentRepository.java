package com.hiba.aianalysisservice.repository;

import com.hiba.aianalysisservice.rag.KnowledgeDocument;
import com.hiba.aianalysisservice.rag.KnowledgeDocumentType;
import com.hiba.aianalysisservice.rag.RagProperties;
import com.hiba.aianalysisservice.rag.RagSearchResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
public class KnowledgeDocumentRepository {

    private final JdbcTemplate jdbcTemplate;

    public KnowledgeDocumentRepository(JdbcTemplate jdbcTemplate, RagProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        initializeSchema(properties.getEmbedding().getDimension());
    }

    public void upsert(KnowledgeDocument document) {
        jdbcTemplate.update("""
                INSERT INTO knowledge_documents
                    (document_id, title, content, source, document_type, chunk_index, embedding, metadata, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, CAST(? AS vector), ?, ?, ?)
                ON CONFLICT (document_id, chunk_index) DO UPDATE SET
                    title = EXCLUDED.title,
                    content = EXCLUDED.content,
                    source = EXCLUDED.source,
                    document_type = EXCLUDED.document_type,
                    embedding = EXCLUDED.embedding,
                    metadata = EXCLUDED.metadata,
                    updated_at = EXCLUDED.updated_at
                """,
                document.documentId(), document.title(), document.content(), document.source(),
                document.documentType().name(), document.chunkIndex(), vectorLiteral(document.embedding()),
                document.metadata(), Timestamp.from(document.createdAt()), Timestamp.from(document.updatedAt()));
    }

    public void deleteByDocumentId(String documentId) {
        jdbcTemplate.update("DELETE FROM knowledge_documents WHERE document_id = ?", documentId);
    }

    public List<RagSearchResult> searchSimilar(float[] queryEmbedding, int topK) {
        if (topK <= 0) {
            return List.of();
        }
        return jdbcTemplate.query("""
                SELECT id, document_id, title, content, source, document_type, chunk_index,
                       embedding::text AS embedding, metadata, created_at, updated_at,
                       1 - (embedding <=> CAST(? AS vector)) AS similarity
                FROM knowledge_documents
                ORDER BY embedding <=> CAST(? AS vector)
                LIMIT ?
                """, (rs, rowNum) -> new RagSearchResult(map(rs), rs.getDouble("similarity")),
                vectorLiteral(queryEmbedding), vectorLiteral(queryEmbedding), topK);
    }

    private KnowledgeDocument map(ResultSet rs) throws SQLException {
        return new KnowledgeDocument(
                rs.getLong("id"), rs.getString("document_id"), rs.getString("title"),
                rs.getString("content"), rs.getString("source"),
                KnowledgeDocumentType.valueOf(rs.getString("document_type")), rs.getInt("chunk_index"),
                parseVector(rs.getString("embedding")), rs.getString("metadata"),
                rs.getTimestamp("created_at").toInstant(), rs.getTimestamp("updated_at").toInstant());
    }

    private void initializeSchema(int dimension) {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Embedding dimension must be positive");
        }
        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS knowledge_documents (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "document_id VARCHAR(255) NOT NULL, title VARCHAR(500) NOT NULL, content TEXT NOT NULL, " +
                "source VARCHAR(255) NOT NULL, document_type VARCHAR(40) NOT NULL, chunk_index INTEGER NOT NULL, " +
                "embedding vector(" + dimension + "), metadata TEXT, created_at TIMESTAMPTZ NOT NULL, " +
                "updated_at TIMESTAMPTZ NOT NULL, " +
                "CONSTRAINT uk_knowledge_document_chunk UNIQUE (document_id, chunk_index))");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_knowledge_documents_document_type ON knowledge_documents (document_type)");
    }

    private String vectorLiteral(float[] vector) {
        if (vector == null || vector.length == 0) {
            throw new IllegalArgumentException("Embedding must not be empty");
        }
        StringBuilder result = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) result.append(',');
            result.append(vector[i]);
        }
        return result.append(']').toString();
    }

    private float[] parseVector(String value) {
        String body = value.substring(1, value.length() - 1);
        String[] parts = body.isBlank() ? new String[0] : body.split(",");
        float[] result = new float[parts.length];
        for (int i = 0; i < parts.length; i++) result[i] = Float.parseFloat(parts[i]);
        return result;
    }
}
