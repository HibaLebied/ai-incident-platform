package com.hiba.aianalysisservice.rag;

import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.Locale;

@Service
@ConditionalOnProperty(name = "rag.embedding.provider", havingValue = "MOCK", matchIfMissing = true)
public class MockEmbeddingService implements EmbeddingService {

    private final RagProperties properties;

    public MockEmbeddingService(RagProperties properties) {
        this.properties = properties;
    }

    @Override
    public float[] embed(String text) {
        int dimension = properties.getEmbedding().getDimension();
        if (dimension <= 0) {
            throw new IllegalArgumentException("Embedding dimension must be positive");
        }
        float[] vector = new float[dimension];
        String normalized = text == null ? "" : text.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
        for (String token : normalized.split("[^\\p{L}\\p{Nd}]+")) {
            if (!token.isBlank()) {
                vector[Math.floorMod(token.hashCode(), dimension)] += 1.0f;
            }
        }
        double norm = 0;
        for (float value : vector) {
            norm += value * value;
        }
        if (norm == 0) {
            vector[0] = 1.0f;
            return vector;
        }
        float length = (float) Math.sqrt(norm);
        for (int i = 0; i < vector.length; i++) {
            vector[i] /= length;
        }
        return vector;
    }
}
