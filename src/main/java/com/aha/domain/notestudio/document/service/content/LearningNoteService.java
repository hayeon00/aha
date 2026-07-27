package com.aha.domain.notestudio.document.service.content;

import com.aha.domain.notestudio.document.dto.content.response.LearningNoteSummaryResponseDto;
import com.aha.domain.notestudio.document.entity.DocumentProcessingGroup;
import com.aha.domain.notestudio.document.entity.LearningNote;
import com.aha.domain.notestudio.document.entity.SourceDocument;
import com.aha.domain.notestudio.document.enums.DocumentProcessingStatus;
import com.aha.domain.notestudio.document.enums.LearningNoteStatus;
import com.aha.domain.notestudio.document.repository.DocumentProcessingGroupRepository;
import com.aha.domain.notestudio.document.repository.LearningNoteContentRepository;
import com.aha.domain.notestudio.document.repository.LearningNoteRepository;
import com.aha.domain.notestudio.document.repository.SourceDocumentRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LearningNoteService {

    private final DocumentProcessingGroupRepository processingGroupRepository;
    private final SourceDocumentRepository sourceDocumentRepository;
    private final LearningNoteRepository learningNoteRepository;
    private final LearningNoteContentRepository learningNoteContentRepository;
    private final LearningContentBatchGenerationService batchGenerationService;
    private final TransactionTemplate transactionTemplate;

    public LearningNoteSummaryResponseDto create(Long userId, Long processingGroupId) {
        validateIds(userId, processingGroupId);

        LearningNote note = transactionTemplate.execute(status -> {
            DocumentProcessingGroup group = processingGroupRepository
                    .findOwnedByIdForUpdate(processingGroupId, userId)
                    .orElseThrow(() -> new BusinessException(
                            ErrorCode.DOCUMENT_PROCESSING_GROUP_NOT_FOUND));
            if (group.getStatus() != DocumentProcessingStatus.COMPLETED) {
                throw new BusinessException(ErrorCode.INVALID_DOCUMENT_PROCESSING_STATUS);
            }

            LearningNote existing = learningNoteRepository
                    .findByProcessingGroup_Id(processingGroupId)
                    .orElse(null);
            if (existing != null) {
                if (existing.getStatus() == LearningNoteStatus.COMPLETED) {
                    return existing;
                }
                existing.restart();
                return learningNoteRepository.save(existing);
            }

            String examName = group.getUserExam().getExamVersion().getExam().getName();
            return learningNoteRepository.save(
                    LearningNote.start(group, examName + " 개념 학습"));
        });

        if (note == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        if (note.getStatus() == LearningNoteStatus.COMPLETED) {
            return toSummary(note);
        }

        try {
            batchGenerationService.generate(processingGroupId);
            LearningNote completed = transactionTemplate.execute(status -> {
                LearningNote target = learningNoteRepository.findById(note.getId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
                target.complete();
                return learningNoteRepository.save(target);
            });
            return toSummary(completed);
        } catch (RuntimeException exception) {
            transactionTemplate.executeWithoutResult(status ->
                    learningNoteRepository.findById(note.getId()).ifPresent(target -> {
                        target.fail(resolveMessage(exception));
                        learningNoteRepository.save(target);
                    }));
            throw exception;
        }
    }

    public LearningNoteSummaryResponseDto createFromDocument(Long userId, Long documentId) {
        validateIds(userId, documentId);
        Long processingGroupId = transactionTemplate.execute(status ->
                sourceDocumentRepository.findOwnedByIdForUpdate(documentId, userId)
                        .map(document -> document.getProcessingGroup().getId())
                        .orElseThrow(() -> new BusinessException(
                                ErrorCode.SOURCE_DOCUMENT_NOT_FOUND)));
        return create(userId, processingGroupId);
    }

    public List<LearningNoteSummaryResponseDto> getCompletedNotes(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return learningNoteRepository
                .findAllByProcessingGroup_UserExam_User_IdAndStatusOrderByCompletedAtDesc(
                        userId, LearningNoteStatus.COMPLETED)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    private LearningNoteSummaryResponseDto toSummary(LearningNote note) {
        Long groupId = note.getProcessingGroup().getId();
        List<SourceDocument> documents =
                sourceDocumentRepository.findAllByProcessingGroup_IdOrderByIdAsc(groupId);
        Long documentId = documents.isEmpty() ? null : documents.get(0).getId();
        Long tocId = learningNoteContentRepository
                .findAllByLearningNote_ProcessingGroup_IdOrderByExamScopeNode_DisplayOrderAsc(groupId)
                .stream()
                .findFirst()
                .map(content -> content.getExamScopeNode().getId())
                .orElse(null);
        return new LearningNoteSummaryResponseDto(
                note.getId(),
                note.getTitle(),
                note.getStatus().name(),
                groupId,
                documentId,
                tocId,
                documents.size(),
                note.getUpdatedAt());
    }

    private String resolveMessage(RuntimeException exception) {
        if (exception instanceof BusinessException businessException) {
            return businessException.getErrorCode().getMessage();
        }
        return "학습노트 생성에 실패했습니다.";
    }

    private void validateIds(Long... ids) {
        for (Long id : ids) {
            if (id == null || id <= 0) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }
    }
}
