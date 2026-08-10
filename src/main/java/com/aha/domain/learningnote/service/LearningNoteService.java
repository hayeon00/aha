package com.aha.domain.learningnote.service;

import com.aha.domain.document.entity.DocumentProcessing;
import com.aha.domain.document.repository.DocumentProcessingRepository;
import com.aha.domain.learningnote.dto.response.LearningNoteDetailResponseDto;
import com.aha.domain.learningnote.dto.response.LearningNoteSummaryResponseDto;
import com.aha.domain.learningnote.entity.LearningNote;
import com.aha.domain.learningnote.entity.LearningNoteContent;
import com.aha.domain.learningnote.repository.LearningNoteContentRepository;
import com.aha.domain.learningnote.service.generation.LearningNoteContentGenerationService;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.exam.repository.ExamScopeNodeRepository;
import com.aha.domain.exam.enums.ExamScopeNodeType;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningNoteService {

    private final DocumentProcessingRepository documentProcessingRepository;
    private final LearningNoteContentRepository learningNoteContentRepository;
    private final ExamScopeNodeRepository examScopeNodeRepository;
    private final LearningNoteContentGenerationService contentGenerationService;

    @Transactional(readOnly = true)
    public List<LearningNoteSummaryResponseDto> getCompletedNotes(Long userId) {
        validateUserId(userId);

        return documentProcessingRepository.findCompletedOwnedByUserId(userId)
                .stream()
                .map(processing -> {
                    LearningNote note = processing.getLearningNote();

                    return new LearningNoteSummaryResponseDto(
                            note.getId(),
                            note.getTitle(),
                            processing.getStatus().name(),
                            note.getSourceDocument().getId(),
                            null,
                            note.getUserExam().getId(),
                            note.getUserExam().getExamVersion().getExam().getCode(),
                            note.getUserExam().getExamVersion().getExam().getName(),
                            1,
                            note.getUpdatedAt()
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public LearningNoteDetailResponseDto getCompletedNote(Long userId, Long learningNoteId) {
        validateUserId(userId);
        if (learningNoteId == null || learningNoteId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        DocumentProcessing processing = documentProcessingRepository
                .findCompletedOwnedDetail(learningNoteId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LEARNING_NOTE_NOT_FOUND));

        LearningNote note = processing.getLearningNote();

        List<LearningNoteContent> contents = learningNoteContentRepository
                .findAllByLearningNoteIdWithScopeNode(learningNoteId);

        Map<Long, LearningNoteContent> contentByScopeNodeId = contents.stream()
                .collect(Collectors.toMap(
                        content -> content.getExamScopeNode().getId(),
                        Function.identity()
                ));

        Long examVersionId = note.getUserExam().getExamVersion().getId();
        List<ExamScopeNode> learningTopics = examScopeNodeRepository
                .findActiveNodesByExamVersionIdAndNodeTypes(
                        examVersionId,
                        List.of(ExamScopeNodeType.SECTION, ExamScopeNodeType.TOPIC, ExamScopeNodeType.CONCEPT)
                )
                .stream()
                .sorted(this::compareByExamPartAndTreeOrder)
                .toList();

        List<LearningNoteDetailResponseDto.ContentItem> contentItems = learningTopics.stream()
                .map(scopeNode -> {
                    LearningNoteContent content = contentByScopeNodeId.get(scopeNode.getId());
                    return new LearningNoteDetailResponseDto.ContentItem(
                            content == null ? null : content.getId(),
                            scopeNode.getId(),
                            scopeNode.getTitle(),
                            scopeNode.getParent() == null ? null : scopeNode.getParent().getId(),
                            scopeNode.getDepth(),
                            scopeNode.getNodeType().name(),
                            scopeNode.isLeaf(),
                            scopeNode.getExamPart().getId(),
                            scopeNode.getExamPart().getName(),
                            scopeNode.getExamPart().getDisplayOrder(),
                            content == null ? null : content.getTitle(),
                            scopeNode.getDisplayOrder(),
                            content == null ? null : content.getSourceType(),
                            content == null ? null : content.getContent()
                    );
                })
                .toList();

        var sourceDocument = note.getSourceDocument();
        var exam = note.getUserExam().getExamVersion().getExam();

        return new LearningNoteDetailResponseDto(
                note.getId(),
                note.getTitle(),
                note.getUserExam().getId(),
                exam.getName(),
                List.of(new LearningNoteDetailResponseDto.DocumentItem(
                        sourceDocument.getId(),
                        sourceDocument.getOriginalFileName(),
                        sourceDocument.getFileExtension(),
                        sourceDocument.getFileSize()
                )),
                contentItems,
                note.getCreatedAt(),
                note.getUpdatedAt(),
                processing.getCompletedAt()
        );
    }

    public void generateTopicContent(Long userId, Long learningNoteId, Long tocId) {
        validateUserId(userId);
        if (learningNoteId == null || learningNoteId <= 0 || tocId == null || tocId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        documentProcessingRepository.findCompletedOwnedDetail(learningNoteId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LEARNING_NOTE_NOT_FOUND));

        contentGenerationService.generateTopic(learningNoteId, tocId);
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private int compareByExamPartAndTreeOrder(ExamScopeNode left, ExamScopeNode right) {
        int partOrderComparison = Comparator.nullsLast(Integer::compareTo)
                .compare(
                        left.getExamPart().getDisplayOrder(),
                        right.getExamPart().getDisplayOrder()
                );
        if (partOrderComparison != 0) return partOrderComparison;

        int partIdComparison = Comparator.nullsLast(Long::compareTo)
                .compare(left.getExamPart().getId(), right.getExamPart().getId());
        if (partIdComparison != 0) return partIdComparison;

        List<Integer> leftPath = buildDisplayOrderPath(left);
        List<Integer> rightPath = buildDisplayOrderPath(right);

        int commonLength = Math.min(leftPath.size(), rightPath.size());

        for (int index = 0; index < commonLength; index++) {
            int pathComparison = Integer.compare(leftPath.get(index), rightPath.get(index));
            if (pathComparison != 0) return pathComparison;
        }

        int pathLengthComparison = Integer.compare(leftPath.size(), rightPath.size());
        if (pathLengthComparison != 0) return pathLengthComparison;

        return Comparator.nullsLast(Long::compareTo).compare(left.getId(), right.getId());
    }

    private List<Integer> buildDisplayOrderPath(ExamScopeNode node) {
        List<Integer> path = new ArrayList<>();
        ExamScopeNode current = node;

        while (current != null) {
            path.add(current.getDisplayOrder() == null ? Integer.MAX_VALUE : current.getDisplayOrder());
            current = current.getParent();
        }

        Collections.reverse(path);
        return path;
    }

}
