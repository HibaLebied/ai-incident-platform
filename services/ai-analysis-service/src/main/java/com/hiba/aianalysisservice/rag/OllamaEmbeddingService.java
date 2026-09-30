package com.hiba.aianalysisservice.rag;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@ConditionalOnProperty(name = "rag.embedding.provider", havingValue = "OLLAMA")
public class OllamaEmbeddingService implements EmbeddingService {

    private final RestClient client;
    private final RagProperties properties;

    public OllamaEmbeddingService(RestClient.Builder restClientBuilder, RagProperties properties) {
        this.client = restClientBuilder.baseUrl(properties.getEmbedding().getUrl()).build();
        this.properties = properties;
    }

    @Override
    public float[] embed(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text to embed must not be blank");
        }
        EmbeddingResponse response = client.post()
                .uri("/api/embed")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new EmbeddingRequest(properties.getEmbedding().getModel(), text))
                .retrieve()
                .body(EmbeddingResponse.class);
        if (response == null || response.embeddings() == null || response.embeddings().length == 0
                || response.embeddings()[0] == null) {
            throw new IllegalStateException("Ollama returned no embedding");
        }
        float[] embedding = response.embeddings()[0];
        if (embedding.length != properties.getEmbedding().getDimension()) {
            throw new IllegalStateException("Ollama embedding dimension " + embedding.length
                    + " does not match configured dimension " + properties.getEmbedding().getDimension());
        }
        return embedding;
    }

    private record EmbeddingRequest(String model, String input) {
    }

    private record EmbeddingResponse(float[][] embeddings) {
    }
}
