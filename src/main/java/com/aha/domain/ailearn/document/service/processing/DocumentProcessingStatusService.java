package com.aha.domain.ailearn.document.service.processing;

import com.aha.domain.ailearn.document.entity.DocumentProcessingGroup;
import com.aha.domain.ailearn.document.enums.DocumentProcessingStatus;
import com.aha.domain.ailearn.document.enums.DocumentProcessingStep;
import com.aha.domain.ailearn.document.repository.DocumentProcessingGroupRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentProcessingStatusService {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;
    private static final String DEFAULT_ERROR_MESSAGE = "문서 처리 중 오류가 발생했습니다.";

    private final DocumentProcessingGroupRepository processingGroupRepository;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean start(Long processingGroupId) {
        DocumentProcessingGroup processingGroup = getProcessingGroup(processingGroupId);

        if (processingGroup.getStatus() != DocumentProcessingStatus.PENDING) {
            log.warn(
                    "문서 처리 시작 요청을 건너뜁니다. processingGroupId={}, status={}",
                    processingGroupId,
                    processingGroup.getStatus()
            );

            return false;
        }
        processingGroup.startProcessing();

        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(Long processingGroupId, String errorMessage) {

        DocumentProcessingGroup processingGroup = getProcessingGroup(processingGroupId);

        String resolvedErrorMessage = resolveErrorMessage(errorMessage);

        processingGroup.fail(resolvedErrorMessage);

        log.warn(
                "문서 처리 실패 상태 반영 완료. processingGroupId={}, errorMessage={}",
                processingGroupId,
                resolvedErrorMessage
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStep(Long processingGroupId, DocumentProcessingStep nextStep) {

        if(nextStep == null){
            throw new IllegalArgumentException("문서 처리 단계는 필수입니다.");
        }

        DocumentProcessingGroup processingGroup = getProcessingGroup(processingGroupId);

        processingGroup.updateStep(nextStep);

        log.info(
                "문서 처리 단계 변경. processingGroupId={}, step={}",
                processingGroupId,
                nextStep
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(Long processingGroupId) {

        DocumentProcessingGroup processingGroup = getProcessingGroup(processingGroupId);

        processingGroup.complete();

        log.info(
                "문서 처리 완료 상태 반영. processingGroupId={}, status={}",
                processingGroupId,
                processingGroup.getStatus()
        );
    }



    private DocumentProcessingGroup getProcessingGroup(Long processingGroupId) {

        if(processingGroupId == null){
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return processingGroupRepository.findById(processingGroupId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DOCUMENT_PROCESSING_GROUP_NOT_FOUND
                ));
    }

    private String resolveErrorMessage(String errorMessage) {

        if (errorMessage == null || errorMessage.isBlank()) {
            return DEFAULT_ERROR_MESSAGE;
        }

        String trimmedMessage = errorMessage.trim();

        if (trimmedMessage.length() > MAX_ERROR_MESSAGE_LENGTH) {
            return trimmedMessage.substring(
                    0,
                    MAX_ERROR_MESSAGE_LENGTH
            );
        }

        return trimmedMessage;
    }

}
