package com.aha.domain.learningnote.client.generation.dto;

import java.util.List;

public record ConceptExplanationBatchResponse(
        List<ConceptExplanationBatchItem> contents
) {
}
