package com.aha.domain.ailearn.document.client.embedding;

import java.util.List;

public record EmbeddingRequestDto(
        String model,
        List<String> input
) {
}