package com.aha.domain.learningnote.service.generation;

import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.exam.repository.ExamScopeNodeRepository;
import com.aha.domain.learningnote.entity.LearningNote;
import com.aha.domain.learningnote.entity.LearningNoteContent;
import com.aha.domain.learningnote.repository.LearningNoteContentRepository;
import com.aha.domain.learningnote.repository.LearningNoteRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LearningNoteContentPersistenceService {

    private final LearningNoteRepository learningNoteRepository;
    private final ExamScopeNodeRepository examScopeNodeRepository;
    private final LearningNoteContentRepository learningNoteContentRepository;

    @Transactional
    public void replaceDocumentBasedContents(
            Long learningNoteId,
            List<GeneratedConceptExplanation> explanations
    ) {
        if (learningNoteId == null || explanations == null || explanations.isEmpty()) {
            throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED);
        }

        LearningNote learningNote = learningNoteRepository.findById(learningNoteId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LEARNING_NOTE_NOT_FOUND));

        List<Long> scopeNodeIds = explanations.stream()
                .map(GeneratedConceptExplanation::examScopeNodeId)
                .distinct()
                .toList();

        Map<Long, ExamScopeNode> scopeNodesById = examScopeNodeRepository.findAllById(scopeNodeIds)
                .stream()
                .collect(Collectors.toMap(ExamScopeNode::getId, Function.identity()));

        if (scopeNodesById.size() != scopeNodeIds.size()) {
            throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED);
        }

        String generationVersion = "concept-generation-v1";

        List<LearningNoteContent> contents = explanations.stream()
                .map(explanation ->
                        LearningNoteContent.createDocumentBased(
                                learningNote,
                                scopeNodesById.get(
                                        explanation.examScopeNodeId()
                                ),
                                explanation.title(),
                                explanation.content(),
                                null,
                                generationVersion
                        )
                )
                .toList();

        int deletedCount = learningNoteContentRepository
                .deleteDocumentBasedByLearningNoteId(learningNoteId);
        learningNoteContentRepository.saveAll(contents);

        log.info(
                "목차별 개념 설명 교체 저장 완료. learningNoteId={}, deletedCount={}, contentCount={}",
                learningNoteId,
                deletedCount,
                contents.size()
        );
    }

    @Transactional
    public void saveDocumentBasedContent(
            Long learningNoteId,
            GeneratedConceptExplanation explanation
    ) {
        if (learningNoteId == null || explanation == null || explanation.examScopeNodeId() == null) {
            throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED);
        }

        LearningNoteContent existing = learningNoteContentRepository
                .findByLearningNote_IdAndExamScopeNode_Id(
                        learningNoteId,
                        explanation.examScopeNodeId()
                )
                .orElse(null);

        if (existing != null) {
            existing.update(explanation.title(), explanation.content());
            return;
        }

        LearningNote learningNote = learningNoteRepository.findById(learningNoteId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LEARNING_NOTE_NOT_FOUND));
        ExamScopeNode scopeNode = examScopeNodeRepository.findById(explanation.examScopeNodeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED));

        learningNoteContentRepository.save(
                LearningNoteContent.createDocumentBased(
                        learningNote,
                        scopeNode,
                        explanation.title(),
                        explanation.content(),
                        null,
                        "concept-generation-v1"
                )
        );
    }
}
