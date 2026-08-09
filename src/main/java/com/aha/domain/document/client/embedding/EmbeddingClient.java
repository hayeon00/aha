package com.aha.domain.document.client.embedding;

import java.util.List;

public interface EmbeddingClient {

    List<Double> embed(String text);

    List<List<Double>> embedAll(List<String> texts);
}