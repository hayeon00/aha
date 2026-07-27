package com.aha.domain.notestudio.document.service.content;

import com.aha.domain.notestudio.document.service.content.model.GeneratedLearningContent;
import com.aha.domain.notestudio.document.service.content.model.TopicDocumentContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LearningContentBatchGenerationService {

    private final DocumentContentContextQueryService
            contextQueryService;

    private final LearningContentGenerationService
            generationService;

    private final LearningContentPersistenceService
            persistenceService;

    /**
     * 처리 그룹의 매핑 결과를 기반으로
     * Topic별 개념설명을 생성하고 저장한다.
     */
    public void generate(Long processingGroupId) {
        List<TopicDocumentContext> contexts =
                contextQueryService
                        .findTopicContexts(
                                processingGroupId
                        );

        for (TopicDocumentContext context : contexts) {
            log.info(
                    "Topic 개념설명 생성 시작. processingGroupId={}, topicId={}, topicTitle={}",
                    processingGroupId,
                    context.topicId(),
                    context.topicTitle()
            );

            GeneratedLearningContent generatedContent =
                    generationService.generate(
                            context
                    );

            persistenceService.saveOrReplace(
                    processingGroupId,
                    context.topicId(),
                    generatedContent
            );

            log.info(
                    "Topic 개념설명 생성 완료. processingGroupId={}, topicId={}",
                    processingGroupId,
                    context.topicId()
            );
        }
    }
}