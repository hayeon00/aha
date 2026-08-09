package com.aha.domain.learningnote.client.generation;

import com.aha.domain.learningnote.client.generation.dto.ConceptExplanationRequest;
import com.aha.domain.learningnote.client.generation.dto.ConceptExplanationResult;

public interface ConceptExplanationClient {

    ConceptExplanationResult generate(ConceptExplanationRequest request);
}
