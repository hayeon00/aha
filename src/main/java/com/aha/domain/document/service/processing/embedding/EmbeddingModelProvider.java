package com.aha.domain.document.service.processing.embedding;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmbeddingModelProvider {

    private final String embeddingProvider;
    private final String embeddingModel;

    public EmbeddingModelProvider(
            @Value("${openAI.embedding-provider:OPENAI}") String embeddingProvider,
            @Value("${openAI.embedding-model:text-embedding-3-small}") String embeddingModel
    ) {
        this.embeddingProvider = embeddingProvider;
        this.embeddingModel = embeddingModel;
    }

    public String getEmbeddingProvider() {
        return embeddingProvider;
    }

    public String getEmbeddingModel() {
        return embeddingModel;
    }
}
