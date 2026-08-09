package com.aha.domain.document.client.embedding.dto;

import java.util.List;

public record EmbeddingResponse(
        List<EmbeddingData> data
) {
    public record EmbeddingData(
            Integer index,
            List<Double> embedding
    ) {
    }
}
