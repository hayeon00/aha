package com.aha.domain.learningnote.service.coverage;

import com.aha.domain.document.repository.DocumentScopeMappingRepository;
import com.aha.domain.document.repository.projection.TopicMappingCountProjection;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.exam.repository.ExamScopeNodeRepository;
import com.aha.domain.learningnote.dto.response.LearningNoteCoverageResponse;
import com.aha.domain.learningnote.dto.response.TopicCoverageResponse;
import com.aha.domain.learningnote.entity.LearningNote;
import com.aha.domain.learningnote.enums.TopicCoverageStatus;
import com.aha.domain.learningnote.repository.LearningNoteRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningNoteCoverageService {

    private static final int COVERAGE_RATE_SCALE = 2;

    private final LearningNoteRepository learningNoteRepository;
    private final ExamScopeNodeRepository examScopeNodeRepository;
    private final DocumentScopeMappingRepository documentScopeMappingRepository;

    public LearningNoteCoverageResponse getCoverage(
            Long learningNoteId
    ) {
        validateLearningNoteId(
                learningNoteId
        );

        LearningNote learningNote =
                learningNoteRepository
                        .findByIdWithExamVersion(
                                learningNoteId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.LEARNING_NOTE_NOT_FOUND
                                )
                        );

        Long examVersionId =
                getExamVersionId(
                        learningNote
                );

        /*
         * Coverage 대상은 실제 사용자 학습 단위인
         * active leaf Topic만 사용한다.
         */
        List<ExamScopeNode> targetTopics =
                examScopeNodeRepository
                        .findAllByExamVersion_IdAndIsLeafTrueAndIsActiveTrueOrderByDepthAscDisplayOrderAsc(
                                examVersionId
                        );

        if (targetTopics.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED
            );
        }

        /*
         * Topic마다 count query를 호출하지 않고
         * GROUP BY 한 번으로 해당 LearningNote의 매핑 수를 조회한다.
         */
        Map<Long, Long> mappingCountByScopeNodeId =
                documentScopeMappingRepository
                        .findTopicMappingCountsByLearningNoteId(
                                learningNoteId
                        )
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        TopicMappingCountProjection::getScopeNodeId,
                                        TopicMappingCountProjection::getMappingCount
                                )
                        );

        List<TopicCoverageResponse> topicResponses =
                targetTopics.stream()
                        .map(topic ->
                                createTopicCoverageResponse(
                                        topic,
                                        mappingCountByScopeNodeId
                                )
                        )
                        .toList();

        int coveredTopicCount =
                (int) topicResponses.stream()
                        .filter(response ->
                                response.getStatus()
                                        == TopicCoverageStatus.COVERED
                        )
                        .count();

        int totalTopicCount =
                topicResponses.size();

        BigDecimal coverageRate =
                calculateCoverageRate(
                        coveredTopicCount,
                        totalTopicCount
                );

        return LearningNoteCoverageResponse.builder()
                .learningNoteId(learningNoteId)
                .totalTopicCount(totalTopicCount)
                .coveredTopicCount(coveredTopicCount)
                .coverageRate(coverageRate)
                .topics(topicResponses)
                .build();
    }

    private TopicCoverageResponse createTopicCoverageResponse(
            ExamScopeNode topic,
            Map<Long, Long> mappingCountByScopeNodeId
    ) {
        long mappedChunkCount =
                mappingCountByScopeNodeId.getOrDefault(
                        topic.getId(),
                        0L
                );

        TopicCoverageStatus status =
                mappedChunkCount > 0
                        ? TopicCoverageStatus.COVERED
                        : TopicCoverageStatus.NOT_COVERED;

        return TopicCoverageResponse.builder()
                .scopeNodeId(topic.getId())
                .code(topic.getCode())
                .title(topic.getTitle())
                .status(status)
                .mappedChunkCount(
                        Math.toIntExact(mappedChunkCount)
                )
                .build();
    }

    private BigDecimal calculateCoverageRate(
            int coveredTopicCount,
            int totalTopicCount
    ) {
        if (totalTopicCount <= 0) {
            return BigDecimal.ZERO
                    .setScale(
                            COVERAGE_RATE_SCALE,
                            RoundingMode.HALF_UP
                    );
        }

        return BigDecimal
                .valueOf(
                        coveredTopicCount
                )
                .multiply(
                        BigDecimal.valueOf(100)
                )
                .divide(
                        BigDecimal.valueOf(
                                totalTopicCount
                        ),
                        COVERAGE_RATE_SCALE,
                        RoundingMode.HALF_UP
                );
    }

    private Long getExamVersionId(
            LearningNote learningNote
    ) {
        if (learningNote.getUserExam() == null
                || learningNote.getUserExam().getExamVersion() == null
                || learningNote.getUserExam().getExamVersion().getId() == null) {

            throw new BusinessException(
                    ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED
            );
        }

        return learningNote
                .getUserExam()
                .getExamVersion()
                .getId();
    }

    private void validateLearningNoteId(
            Long learningNoteId
    ) {
        if (learningNoteId == null
                || learningNoteId <= 0) {

            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
    }
}