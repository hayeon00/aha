package com.aha.domain.ailearn.document.service;

import com.aha.domain.ailearn.document.enums.DocumentProcessingStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentProcessingWorkerService {

    private final DocumentProcessingStatusService documentProcessingStatusService;

    @Async
    public void process(Long processingGroupId) {
        try {
            documentProcessingStatusService.start(processingGroupId);

            extractText(processingGroupId);
            documentProcessingStatusService.updateStep(
                    processingGroupId,
                    DocumentProcessingStep.CONTENT_ANALYZING
            );

            analyzeContent(processingGroupId);
            documentProcessingStatusService.updateStep(
                    processingGroupId,
                    DocumentProcessingStep.SCOPE_MAPPING
            );

            mapExamScope(processingGroupId);
            documentProcessingStatusService.updateStep(
                    processingGroupId,
                    DocumentProcessingStep.LEARNING_CONTENT_GENERATING
            );

            generateLearningContent(processingGroupId);
            documentProcessingStatusService.complete(processingGroupId);
        } catch (Exception e) {
            log.error("Document processing failed. processingGroupId={}", processingGroupId, e);
            documentProcessingStatusService.fail(
                    processingGroupId,
                    "문서 처리 중 오류가 발생했습니다."
            );
        }
    }

    private void extractText(Long processingGroupId) {
        log.info("Extracting text. processingGroupId={}", processingGroupId);
    }

    private void analyzeContent(Long processingGroupId) {
        log.info("Analyzing content. processingGroupId={}", processingGroupId);
    }

    private void mapExamScope(Long processingGroupId) {
        log.info("Mapping exam scope. processingGroupId={}", processingGroupId);
    }

    private void generateLearningContent(Long processingGroupId) {
        log.info("Generating learning content. processingGroupId={}", processingGroupId);
    }
}
