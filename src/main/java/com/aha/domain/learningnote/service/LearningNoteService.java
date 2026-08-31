package com.aha.domain.learningnote.service;

import com.aha.domain.document.entity.DocumentProcessing;
import com.aha.domain.document.entity.SourceDocument;
import com.aha.domain.document.repository.DocumentProcessingRepository;
import com.aha.domain.exam.entity.Exam;
import com.aha.domain.exam.entity.ExamPart;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.exam.repository.ExamScopeNodeRepository;
import com.aha.domain.learningnote.dto.response.LearningNoteDetailResponseDto;
import com.aha.domain.learningnote.dto.response.LearningNoteSummaryResponseDto;
import com.aha.domain.learningnote.entity.LearningNote;
import com.aha.domain.learningnote.entity.LearningNoteContent;
import com.aha.domain.learningnote.repository.LearningNoteContentRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : LearningNoteService
 * @since : 2026. 8. 16. 일요일
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningNoteService {

    private final DocumentProcessingRepository documentProcessingRepository;
    private final LearningNoteContentRepository learningNoteContentRepository;
    private final ExamScopeNodeRepository examScopeNodeRepository;

    public List<LearningNoteSummaryResponseDto> getCompletedNotes(Long userId) {
        validateUserId(userId);

        return documentProcessingRepository.findCompletedOwnedByUserId(userId)
                .stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    public LearningNoteDetailResponseDto getCompletedNote(
            Long userId,
            Long learningNoteId
    ) {
        validateIds(userId, learningNoteId);

        DocumentProcessing processing = documentProcessingRepository
                .findCompletedOwnedDetail(learningNoteId, userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.LEARNING_NOTE_NOT_FOUND
                ));

        LearningNote note = processing.getLearningNote();
        SourceDocument document = note.getSourceDocument();
        Long examVersionId = note.getUserExam().getExamVersion().getId();

        Map<Long, LearningNoteContent> contentByScopeNodeId =
                learningNoteContentRepository
                        .findAllByLearningNoteIdWithScopeNode(learningNoteId)
                        .stream()
                        .collect(Collectors.toMap(
                                content -> content.getExamScopeNode().getId(),
                                Function.identity()
                        ));

        List<LearningNoteDetailResponseDto.ContentItem> contents =
                examScopeNodeRepository
                        .findAllActiveForLearningNoteDetail(examVersionId)
                        .stream()
                        .map(scopeNode -> toContentItem(
                                scopeNode,
                                contentByScopeNodeId.get(scopeNode.getId())
                        ))
                        .toList();

        Exam exam = note.getUserExam().getExamVersion().getExam();

        return new LearningNoteDetailResponseDto(
                note.getId(),
                note.getTitle(),
                note.getUserExam().getId(),
                exam.getName(),
                List.of(toDocumentItem(document)),
                contents,
                note.getCreatedAt(),
                note.getUpdatedAt(),
                processing.getCompletedAt()
        );
    }

    @Transactional
    public void updateTitle(
            Long userId,
            Long learningNoteId,
            String title
    ) {
        validateIds(userId, learningNoteId);

        LearningNote note = documentProcessingRepository
                .findCompletedOwnedDetail(learningNoteId, userId)
                .map(DocumentProcessing::getLearningNote)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.LEARNING_NOTE_NOT_FOUND
                ));

        try {
            note.updateTitle(title);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private LearningNoteSummaryResponseDto toSummaryResponse(
            DocumentProcessing processing
    ) {
        LearningNote note = processing.getLearningNote();
        SourceDocument document = note.getSourceDocument();
        Exam exam = note.getUserExam().getExamVersion().getExam();

        Long firstTocId = learningNoteContentRepository
                .findAllByLearningNoteIdWithScopeNode(note.getId())
                .stream()
                .map(content -> content.getExamScopeNode().getId())
                .findFirst()
                .orElse(null);

        return new LearningNoteSummaryResponseDto(
                note.getId(),
                note.getTitle(),
                note.getStatus().name(),
                document.getId(),
                firstTocId,
                note.getUserExam().getId(),
                exam.getCode(),
                exam.getName(),
                1,
                note.getUpdatedAt()
        );
    }

    private LearningNoteDetailResponseDto.DocumentItem toDocumentItem(
            SourceDocument document
    ) {
        return new LearningNoteDetailResponseDto.DocumentItem(
                document.getId(),
                document.getOriginalFileName(),
                document.getFileExtension().getValue(),
                document.getFileSize()
        );
    }

    private LearningNoteDetailResponseDto.ContentItem toContentItem(
            ExamScopeNode scopeNode,
            LearningNoteContent content
    ) {
        ExamPart examPart = scopeNode.getExamPart();

        return new LearningNoteDetailResponseDto.ContentItem(
                content == null ? null : content.getId(),
                scopeNode.getId(),
                scopeNode.getTitle(),
                scopeNode.getParent() == null
                        ? null
                        : scopeNode.getParent().getId(),
                scopeNode.getDepth(),
                scopeNode.getNodeType().name(),
                scopeNode.isLeaf(),
                examPart.getId(),
                examPart.getName(),
                examPart.getDisplayOrder(),
                content == null ? null : content.getTitle(),
                scopeNode.getDisplayOrder(),
                content == null ? null : content.getSourceType(),
                content == null ? null : content.getContent()
        );
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateIds(Long userId, Long learningNoteId) {
        validateUserId(userId);

        if (learningNoteId == null || learningNoteId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
