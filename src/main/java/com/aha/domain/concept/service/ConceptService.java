package com.aha.domain.concept.service;

import com.aha.domain.concept.dto.response.ConceptDetailResponse;
import com.aha.domain.concept.dto.response.ConceptListItemResponse;
import com.aha.domain.concept.entity.Concept;
import com.aha.domain.concept.entity.ConceptCard;
import com.aha.domain.concept.respository.ConceptCardRepository;
import com.aha.domain.concept.respository.ConceptRepository;
import com.aha.domain.exam.repository.ExamVersionRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConceptService {

    private final ConceptRepository conceptRepository;
    private final ExamVersionRepository examVersionRepository;
    private final ConceptCardRepository conceptCardRepository;

    public List<ConceptListItemResponse> getConcepts(Long examVersionId, Long examScopeNodeId) {
        if (examVersionId == null || examVersionId <= 0
                || (examScopeNodeId != null && examScopeNodeId <= 0)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (!examVersionRepository.existsById(examVersionId)) {
            throw new BusinessException(ErrorCode.EXAM_VERSION_NOT_FOUND);
        }

        List<Concept> concepts = examScopeNodeId == null
                ? conceptRepository.findAllWithScopeByExamVersionId(examVersionId)
                : conceptRepository
                .findByExamVersion_IdAndExamScopeNode_IdOrderByDisplayOrderAscIdAsc(
                        examVersionId,
                        examScopeNodeId
                );

        return concepts.stream()
                .map(ConceptListItemResponse::from)
                .toList();
    }

    public ConceptDetailResponse getConcept(Long conceptId) {
        if (conceptId == null || conceptId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Concept concept = conceptRepository.findById(conceptId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONCEPT_NOT_FOUND));

        List<ConceptCard> cards =
                conceptCardRepository.findByConcept_IdOrderByDisplayOrderAscIdAsc(conceptId);

        return ConceptDetailResponse.of(concept, cards);
    }
}
