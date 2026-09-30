package com.hiba.aianalysisservice.rag;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MockEmbeddingServiceTest {

    @Test
    void producesDeterministicNormalizedVectorsWithConfiguredDimension() {
        RagProperties properties = new RagProperties();
        properties.getEmbedding().setDimension(4);
        MockEmbeddingService service = new MockEmbeddingService(properties);

        float[] first = service.embed("Database connection failed");
        float[] second = service.embed("Database connection failed");

        assertThat(first).hasSize(4).containsExactly(second);
        assertThat(Math.sqrt(sumSquares(first))).isCloseTo(1.0, within(0.0001));
    }

    private double sumSquares(float[] values) {
        double result = 0;
        for (float value : values) result += value * value;
        return result;
    }

    private org.assertj.core.data.Offset<Double> within(double value) {
        return org.assertj.core.data.Offset.offset(value);
    }
}
