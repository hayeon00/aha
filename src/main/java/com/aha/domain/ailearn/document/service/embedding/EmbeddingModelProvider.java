package com.aha.domain.ailearn.document.service.embedding;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmbeddingModelProvider {

    private final String embeddingModel;

    public EmbeddingModelProvider(
            @Value("${openAI.embedding-model:text-embedding-3-small}") String embeddingModel
    ) {
        this.embeddingModel = embeddingModel;
    }

    public String getEmbeddingModel() {
        return embeddingModel;
    }
}