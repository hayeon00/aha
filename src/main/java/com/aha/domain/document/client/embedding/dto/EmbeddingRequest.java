package com.aha.domain.document.client.embedding.dto;

import java.util.List;

public record EmbeddingRequest(
        String model,
        List<String> input
) {
}
