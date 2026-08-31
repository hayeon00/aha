package com.aha.domain.learningnote.client.generation;

import com.aha.domain.learningnote.client.generation.dto.ConceptExplanationRequest;
import com.aha.domain.learningnote.client.generation.dto.ConceptExplanationResult;
import com.aha.domain.learningnote.client.generation.dto.ConceptExplanationBatchItem;

import java.util.List;

public interface ConceptExplanationClient {

    ConceptExplanationResult generate(ConceptExplanationRequest request);

    List<ConceptExplanationBatchItem> generateBatch(
            List<ConceptExplanationRequest> requests
    );
}
