package com.aha.domain.learningnote.dto.response;

import com.aha.domain.document.entity.DocumentProcessing;
import com.aha.domain.document.enums.DocumentProcessingStatus;
import com.aha.domain.learningnote.entity.LearningNote;

public record LearningNoteCreateResponseDto(
        Long learningNoteId,
        Long processingId,
        Long userExamId,
        String title,
        DocumentProcessingStatus processingStatus
) {

    public static LearningNoteCreateResponseDto from(
            LearningNote learningNote,
            DocumentProcessing processing
    ) {
        if (learningNote == null || processing == null) {
            throw new IllegalArgumentException(
                    "학습 노트와 문서 처리 작업은 필수입니다."
            );
        }

        return new LearningNoteCreateResponseDto(
                learningNote.getId(),
                processing.getId(),
                learningNote.getUserExam().getId(),
                learningNote.getTitle(),
                processing.getStatus()
        );
    }
}
