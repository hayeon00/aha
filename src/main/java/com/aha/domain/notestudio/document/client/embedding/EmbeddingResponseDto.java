package com.aha.domain.notestudio.document.client.embedding;

import java.util.List;

public record EmbeddingResponseDto(
        List<EmbeddingData> data
) {
    public record EmbeddingData(
            Integer index,
            List<Double> embedding
    ) {
    }
}