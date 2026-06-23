package com.aha.domain.ailearn.document.service.generation;

import com.aha.domain.ailearn.document.client.LearningContentGenerationClient;
import com.aha.domain.ailearn.document.dto.generation.request.TopicLearningContentSourceDto;
import com.aha.domain.ailearn.document.dto.generation.response.LearningContentGenerationResultDto;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LearningContentGenerationService {

    private final LearningContentSourceQueryService
            learningContentSourceQueryService;

    private final LearningContentGenerationClient
            learningContentGenerationClient;

    private final UserLearningContentPersistenceService
            userLearningContentPersistenceService;

    public void generateLearningContents(
            Long processingGroupId
    ) {
        List<TopicLearningContentSourceDto> sources =
                learningContentSourceQueryService
                        .findSources(
                                processingGroupId
                        );

        for (TopicLearningContentSourceDto source : sources) {
            generateLearningContent(
                    processingGroupId,
                    source
            );
        }

        log.info(
                "목차별 개념 설명 생성 처리 완료. processingGroupId={}, scopeCount={}",
                processingGroupId,
                sources.size()
        );
    }

    private void generateLearningContent(
            Long processingGroupId,
            TopicLearningContentSourceDto source
    ) {
        LearningContentGenerationResultDto result =
                learningContentGenerationClient.generate(
                        source
                );

        validateGenerationResult(result);

        userLearningContentPersistenceService
                .saveOrUpdate(
                        processingGroupId,
                        source.examScopeNodeId(),
                        result
                );
    }

    private void validateGenerationResult(
            LearningContentGenerationResultDto result
    ) {
        if (result == null
                || result.title() == null
                || result.title().isBlank()
                || result.content() == null
                || result.content().isBlank()) {

            throw new BusinessException(
                    ErrorCode.LEARNING_CONTENT_GENERATION_FAILED
            );
        }
    }
}