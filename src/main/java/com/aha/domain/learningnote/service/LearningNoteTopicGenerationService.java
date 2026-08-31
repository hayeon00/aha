package com.aha.domain.learningnote.service;

import com.aha.domain.document.repository.DocumentProcessingRepository;
import com.aha.domain.document.service.processing.generation.DocumentContentGenerationService;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LearningNoteTopicGenerationService {

    private final DocumentProcessingRepository documentProcessingRepository;
    private final DocumentContentGenerationService contentGenerationService;

    public void generate(
            Long userId,
            Long learningNoteId,
            Long scopeNodeId
    ) {
        validateIds(userId, learningNoteId, scopeNodeId);

        documentProcessingRepository
                .findCompletedOwnedDetail(learningNoteId, userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.LEARNING_NOTE_NOT_FOUND
                ));

        contentGenerationService.generateTopic(learningNoteId, scopeNodeId);
    }

    private void validateIds(Long userId, Long learningNoteId, Long scopeNodeId) {
        if (userId == null || userId <= 0
                || learningNoteId == null || learningNoteId <= 0
                || scopeNodeId == null || scopeNodeId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
