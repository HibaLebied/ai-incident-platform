package com.hiba.aianalysisservice.rag;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentChunkerTest {

    @Test
    void returnsDeterministicChunksWithConfiguredOverlap() {
        RagProperties properties = new RagProperties();
        properties.getChunking().setMaxSize(5);
        properties.getChunking().setOverlap(2);

        DocumentChunker chunker = new DocumentChunker(properties);
        List<DocumentChunk> first = chunker.chunk("abcde fghij");

        assertThat(first).extracting(DocumentChunk::content)
                .containsExactly("abcde", "fghi", "j");
        assertThat(chunker.chunk("abcde fghij")).isEqualTo(first);
    }

    @Test
    void handlesEmptyShortAndExactSizeDocuments() {
        RagProperties properties = new RagProperties();
        properties.getChunking().setMaxSize(5);
        properties.getChunking().setOverlap(1);
        DocumentChunker chunker = new DocumentChunker(properties);

        assertThat(chunker.chunk(" ")).isEmpty();
        assertThat(chunker.chunk("a  b")).containsExactly(new DocumentChunk(0, "a b"));
        assertThat(chunker.chunk("abcde")).containsExactly(new DocumentChunk(0, "abcde"));
    }

    @Test
    void keepsMarkdownHeadingsAndSentenceBoundariesWhenPossible() {
        RagProperties properties = new RagProperties();
        properties.getChunking().setMaxSize(70);
        properties.getChunking().setOverlap(20);
        DocumentChunker chunker = new DocumentChunker(properties);

        List<DocumentChunk> chunks = chunker.chunk("# Runbook\n\nFirst sentence explains the incident. Second sentence explains the cause.\n\n```bash\nrestart-pool\n```");

        assertThat(chunks).allSatisfy(chunk -> assertThat(chunk.content()).doesNotEndWith(" "));
        assertThat(chunks).anyMatch(chunk -> chunk.content().contains("# Runbook"));
        assertThat(chunks).anyMatch(chunk -> chunk.content().contains("```bash"));
    }

    @Test
    void keepsNumberedListItemsTogether() {
        RagProperties properties = new RagProperties();
        properties.getChunking().setMaxSize(100);
        properties.getChunking().setOverlap(0);
        DocumentChunker chunker = new DocumentChunker(properties);

        List<DocumentChunk> chunks = chunker.chunk("## Checklist\n\n1. Check broker health.\n2. Inspect consumer lag.");

        assertThat(chunks).anyMatch(chunk -> chunk.content().contains("1. Check broker health."));
        assertThat(chunks).anyMatch(chunk -> chunk.content().contains("2. Inspect consumer lag."));
        assertThat(chunks).noneMatch(chunk -> chunk.content().equals("1."));
    }
}
