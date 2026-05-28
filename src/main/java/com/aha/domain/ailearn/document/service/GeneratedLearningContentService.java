package com.aha.domain.ailearn.document.service;

import com.aha.domain.ailearn.content.entity.ContentBodyType;
import com.aha.domain.ailearn.content.entity.LearningContent;
import com.aha.domain.ailearn.content.entity.LearningContentBody;
import com.aha.domain.ailearn.content.repository.LearningContentBodyRepository;
import com.aha.domain.ailearn.content.repository.LearningContentRepository;
import com.aha.domain.ailearn.document.dto.response.GeneratedLearningContentBodyListResponseDto;
import com.aha.domain.ailearn.document.dto.response.GeneratedLearningContentBodyResponseDto;
import com.aha.domain.ailearn.document.dto.response.LearningContentPublishResponseDto;
import com.aha.domain.ailearn.document.entity.AiGeneratedLearningContentBody;
import com.aha.domain.ailearn.document.entity.LearningSourceDocument;
import com.aha.domain.ailearn.document.repository.AiGeneratedLearningContentBodyRepository;
import com.aha.domain.ailearn.document.repository.LearningSourceDocumentRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GeneratedLearningContentService {

    private final LearningSourceDocumentRepository sourceDocumentRepository;
    private final AiGeneratedLearningContentBodyRepository generatedBodyRepository;
    private final LearningContentRepository learningContentRepository;
    private final LearningContentBodyRepository learningContentBodyRepository;

    @Transactional(readOnly = true)
    public GeneratedLearningContentBodyListResponseDto getGeneratedBodies(Long sourceDocumentId) {
        sourceDocumentRepository.findById(sourceDocumentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SOURCE_DOCUMENT_NOT_FOUND));

        List<GeneratedLearningContentBodyResponseDto> bodies =
                generatedBodyRepository.findBySourceDocumentIdOrderByIdAsc(sourceDocumentId)
                        .stream()
                        .map(GeneratedLearningContentBodyResponseDto::from)
                        .toList();

        return new GeneratedLearningContentBodyListResponseDto(sourceDocumentId, bodies);
    }

    @Transactional
    public LearningContentPublishResponseDto publishGeneratedBodies(Long sourceDocumentId) {
        LearningSourceDocument sourceDocument = sourceDocumentRepository.findById(sourceDocumentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SOURCE_DOCUMENT_NOT_FOUND));

        if (sourceDocument.getExamScopeNodeId() == null) {
            throw new BusinessException(ErrorCode.SOURCE_DOCUMENT_SCOPE_REQUIRED);
        }

        List<AiGeneratedLearningContentBody> generatedBodies =
                generatedBodyRepository.findBySourceDocumentIdOrderByIdAsc(sourceDocumentId);

        if (generatedBodies.isEmpty()) {
            throw new BusinessException(ErrorCode.GENERATED_CONTENT_NOT_FOUND);
        }

        LearningContent learningContent = learningContentRepository
                .findFirstByExamScopeNodeIdAndIsActiveTrueOrderByDisplayOrderAsc(
                        sourceDocument.getExamScopeNodeId()
                )
                .orElseGet(() -> learningContentRepository.save(
                        LearningContent.builder()
                                .examScopeNodeId(sourceDocument.getExamScopeNodeId())
                                .title(sourceDocument.getTitle())
                                .summary(sourceDocument.getDescription())
                                .ragEnabled(true)
                                .isActive(true)
                                .displayOrder(1)
                                .build()
                ));

        List<LearningContentBody> existingBodies =
                learningContentBodyRepository.findByLearningContentIdAndIsActiveTrueOrderByDisplayOrderAsc(
                        learningContent.getId()
                );

        learningContentBodyRepository.deleteAll(existingBodies);

        List<AiGeneratedLearningContentBody> sortedBodies = generatedBodies.stream()
                .sorted(Comparator.comparingInt(body -> getDisplayOrder(body.getBodyType().name())))
                .toList();

        int displayOrder = 1;

        for (AiGeneratedLearningContentBody generatedBody : sortedBodies) {
            LearningContentBody body = LearningContentBody.builder()
                    .learningContentId(learningContent.getId())
                    .bodyType(ContentBodyType.valueOf(generatedBody.getBodyType().name()))
                    .title(generatedBody.getTitle())
                    .bodyText(generatedBody.getContent())
                    .displayOrder(displayOrder)
                    .ragChunkOrder(displayOrder)
                    .isActive(true)
                    .build();

            learningContentBodyRepository.save(body);

            generatedBody.publish();

            displayOrder++;
        }

        return new LearningContentPublishResponseDto(
                sourceDocumentId,
                learningContent.getId(),
                sortedBodies.size()
        );
    }

    private int getDisplayOrder(String bodyType) {
        return switch (bodyType) {
            case "BASE_EXPLANATION" -> 1;
            case "CORE_POINT" -> 2;
            case "EXAMPLE" -> 3;
            case "CONFUSION_NOTE" -> 4;
            case "EXAM_POINT" -> 5;
            default -> 99;
        };
    }
}