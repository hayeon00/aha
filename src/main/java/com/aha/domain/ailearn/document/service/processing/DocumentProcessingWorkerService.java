package com.aha.domain.ailearn.document.service.processing;

import com.aha.domain.ailearn.document.enums.DocumentProcessingStep;
import com.aha.domain.ailearn.document.service.content.DocumentLearningContentBatchGenerationService;
import com.aha.domain.ailearn.document.service.extraction.DocumentExtractionService;
import com.aha.domain.ailearn.document.service.mapping.DocumentScopeMappingService;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentProcessingWorkerService {

    private final DocumentProcessingStatusService processingStatusService;
    private final DocumentExtractionService extractionService;
    private final DocumentScopeMappingService documentScopeMappingService;
    private final DocumentLearningContentBatchGenerationService learningContentBatchGenerationService;

    @Async
    public void process(Long processingGroupId) {

        if (processingGroupId == null) {
            log.error("문서 처리를 시작할 수 없습니다. processingGroupId가 null입니다.");
            return;
        }

        log.info("문서 비동기 처리 시작. processingGroupId={}", processingGroupId);

        try {
            if (!processingStatusService.start(processingGroupId)) {
                log.warn(
                        "이미 시작되었거나 처리할 수 없는 작업입니다. processingGroupId={}",
                        processingGroupId
                );
                return;
            }

            extractDocuments(processingGroupId);

            mapDocumentScopes(processingGroupId);

            generateLearningContents(processingGroupId);

            processingStatusService.complete(processingGroupId);

            log.info("문서 비동기 처리 완료. processingGroupId={}", processingGroupId);

        } catch (Exception exception) {
            handleProcessingFailure(processingGroupId, exception);
        }
    }


    private void extractDocuments(Long processingGroupId) {
        log.info("문서 텍스트 추출 및 청크 저장 시작. processingGroupId={}", processingGroupId);

        extractionService.extractDocuments(processingGroupId);

        log.info("문서 텍스트 추출 및 청크 저장 완료. processingGroupId={}", processingGroupId);
    }


    private void mapDocumentScopes(Long processingGroupId) {

        processingStatusService.updateStep(processingGroupId, DocumentProcessingStep.SCOPE_MAPPING);

        log.info("문서 목차 매핑 시작. processingGroupId={}", processingGroupId);

        documentScopeMappingService.mapDocuments(processingGroupId);

        log.info("문서 목차 매핑 완료. processingGroupId={}", processingGroupId);
    }


    private void generateLearningContents(Long processingGroupId) {

        processingStatusService.updateStep(processingGroupId, DocumentProcessingStep.LEARNING_CONTENT_GENERATING);

        log.info("목차별 개념 설명 생성 시작. processingGroupId={}", processingGroupId);

        learningContentBatchGenerationService.generateLearningContents(processingGroupId);

        log.info("목차별 개념 설명 생성 완료. processingGroupId={}", processingGroupId);
    }



    private void handleProcessingFailure(Long processingGroupId, Exception exception) {

        log.error(
                "문서 비동기 처리 실패. processingGroupId={}",
                processingGroupId,
                exception
        );

        try {
            processingStatusService.fail(
                    processingGroupId,
                    resolveErrorMessage(exception)
            );
        } catch (Exception statusUpdateException) {
            log.error(
                    "문서 처리 실패 상태 저장 실패. processingGroupId={}",
                    processingGroupId,
                    statusUpdateException
            );
        }
    }

    private String resolveErrorMessage(Exception exception) {
        if (exception instanceof BusinessException businessException) {
            return businessException.getErrorCode().getMessage();
        }

        if(exception.getMessage() == null || exception.getMessage().isBlank()){
            return ErrorCode.INTERNAL_SERVER_ERROR.getMessage();
        }

        return exception.getMessage();
    }


}

