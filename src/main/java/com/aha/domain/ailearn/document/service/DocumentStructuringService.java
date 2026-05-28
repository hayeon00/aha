package com.aha.domain.ailearn.document.service;

import com.aha.domain.ailearn.document.dto.response.DocumentStructuringResponse;
import com.aha.domain.ailearn.document.entity.AiGeneratedLearningContentBody;
import com.aha.domain.ailearn.document.entity.DocumentProcessing;
import com.aha.domain.ailearn.document.entity.ExtractedContent;
import com.aha.domain.ailearn.document.entity.LearningSourceDocument;
import com.aha.domain.ailearn.document.repository.AiGeneratedLearningContentBodyRepository;
import com.aha.domain.ailearn.document.repository.DocumentProcessingRepository;
import com.aha.domain.ailearn.document.repository.ExtractedContentRepository;
import com.aha.domain.ailearn.document.repository.LearningSourceDocumentRepository;
import com.aha.domain.ailearn.document.type.LearningContentBodyType;
import com.aha.domain.ailearn.document.type.ProcessingStatus;
import com.aha.domain.ailearn.document.type.ProcessingType;
import com.aha.domain.ailearn.document.type.ReviewStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentStructuringService {

    private final LearningSourceDocumentRepository sourceDocumentRepository;
    private final DocumentProcessingRepository processingRepository;
    private final ExtractedContentRepository extractedContentRepository;
    private final AiGeneratedLearningContentBodyRepository generatedBodyRepository;

    @Transactional
    public DocumentStructuringResponse structureContent(Long sourceDocumentId, Long requestedBy) {
        LearningSourceDocument sourceDocument = sourceDocumentRepository.findById(sourceDocumentId)
                .orElseThrow(() -> new IllegalArgumentException("학습 원본문서를 찾을 수 없습니다. id=" + sourceDocumentId));

        List<ExtractedContent> extractedContents =
                extractedContentRepository.findBySourceDocumentIdAndIsUsedForLearningTrueOrderByChunkOrderAsc(
                        sourceDocumentId
                );

        if (extractedContents.isEmpty()) {
            throw new IllegalStateException("구조화할 추출 원문이 없습니다. 먼저 텍스트 추출을 실행해주세요.");
        }

        DocumentProcessing processing = DocumentProcessing.builder()
                .sourceDocumentId(sourceDocument.getId())
                .processingType(ProcessingType.CONTENT_STRUCTURING)
                .status(ProcessingStatus.PENDING)
                .requestedBy(requestedBy)
                .build();

        DocumentProcessing savedProcessing = processingRepository.save(processing);

        try {
            savedProcessing.start();

            String mergedText = mergeExtractedText(extractedContents);

            List<AiGeneratedLearningContentBody> generatedBodies =
                    createTemporaryStructuredBodies(
                            sourceDocument,
                            savedProcessing,
                            mergedText
                    );

            generatedBodyRepository.saveAll(generatedBodies);

            savedProcessing.complete();

            return new DocumentStructuringResponse(
                    sourceDocument.getId(),
                    savedProcessing.getId(),
                    generatedBodies.size(),
                    savedProcessing.getStatus().name()
            );

        } catch (Exception e) {
            savedProcessing.fail(e.getMessage());
            throw new IllegalStateException("학습 원본문서 구조화에 실패했습니다.", e);
        }
    }

    private String mergeExtractedText(List<ExtractedContent> extractedContents) {
        StringBuilder builder = new StringBuilder();

        for (ExtractedContent content : extractedContents) {
            builder.append(content.getContentText())
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
        }

        return builder.toString().trim();
    }

    /**
     * 1차 구현용 임시 구조화 로직.
     * 나중에 이 메서드 내부를 LLM 호출 결과로 교체하면 됨.
     */
    private List<AiGeneratedLearningContentBody> createTemporaryStructuredBodies(
            LearningSourceDocument sourceDocument,
            DocumentProcessing processing,
            String mergedText
    ) {
        List<AiGeneratedLearningContentBody> bodies = new ArrayList<>();

        bodies.add(createGeneratedBody(
                sourceDocument,
                processing,
                LearningContentBodyType.BASE_EXPLANATION,
                "기본 설명",
                "다음 원문을 바탕으로 개념의 기본 의미를 학습합니다.\n\n" + summarizeForPreview(mergedText)
        ));

        bodies.add(createGeneratedBody(
                sourceDocument,
                processing,
                LearningContentBodyType.CORE_POINT,
                "핵심 포인트",
                "이 개념에서 우선 확인해야 할 핵심은 개념의 정의, 특징, 구분 기준입니다. " +
                        "추출된 원문을 기준으로 시험에서 판단에 필요한 내용을 정리해야 합니다."
        ));

        bodies.add(createGeneratedBody(
                sourceDocument,
                processing,
                LearningContentBodyType.EXAMPLE,
                "예시",
                "원문에 포함된 설명을 바탕으로 실제 서비스나 데이터 모델링 상황에 적용할 수 있는 예시를 구성합니다."
        ));

        bodies.add(createGeneratedBody(
                sourceDocument,
                processing,
                LearningContentBodyType.CONFUSION_NOTE,
                "헷갈리는 개념",
                "비슷한 개념과 혼동될 수 있는 부분을 구분합니다. 예를 들어 대상, 속성, 관계처럼 역할이 다른 개념을 비교합니다."
        ));

        bodies.add(createGeneratedBody(
                sourceDocument,
                processing,
                LearningContentBodyType.EXAM_POINT,
                "출제 포인트",
                "시험에서는 개념의 정의를 단순 암기하는 것보다, 보기에서 올바른 특징과 잘못된 설명을 구분하는 형태로 출제될 수 있습니다."
        ));

        return bodies;
    }

    private AiGeneratedLearningContentBody createGeneratedBody(
            LearningSourceDocument sourceDocument,
            DocumentProcessing processing,
            LearningContentBodyType bodyType,
            String title,
            String content
    ) {
        return AiGeneratedLearningContentBody.builder()
                .sourceDocumentId(sourceDocument.getId())
                .processingId(processing.getId())
                .examScopeNodeId(sourceDocument.getExamScopeNodeId())
                .learningContentId(null)
                .bodyType(bodyType)
                .title(title)
                .content(content)
                .reviewStatus(ReviewStatus.PENDING)
                .build();
    }

    private String summarizeForPreview(String text) {
        int maxLength = 1000;

        if (text == null || text.isBlank()) {
            return "";
        }

        if (text.length() <= maxLength) {
            return text;
        }

        return text.substring(0, maxLength) + "...";
    }
}