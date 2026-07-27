package com.aha.domain.notestudio.document.service.processing;

import com.aha.domain.notestudio.document.entity.DocumentProcessingGroup;
import com.aha.domain.notestudio.document.enums.DocumentProcessingStatus;
import com.aha.domain.notestudio.document.enums.DocumentProcessingStep;
import com.aha.domain.notestudio.document.repository.DocumentProcessingGroupRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentProcessingStatusService {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;
    private static final String DEFAULT_ERROR_MESSAGE =
            "문서 처리 중 오류가 발생했습니다.";

    private final DocumentProcessingGroupRepository processingGroupRepository;

    /**
     * 업로드가 완료된 처리 그룹의 비동기 처리를 시작한다.
     *
     * PENDING / UPLOAD_COMPLETED 상태인 경우에만
     * PROCESSING / TEXT_EXTRACTING 상태로 변경한다.
     *
     * 조건부 UPDATE를 사용해 같은 처리 그룹의 중복 실행을 방지한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean start(Long processingGroupId) {

        validateProcessingGroupId(processingGroupId);

        LocalDateTime startedAt = LocalDateTime.now();

        int updatedCount =
                processingGroupRepository.startProcessingIfReady(
                        processingGroupId,
                        startedAt
                );

        if (updatedCount == 0) {
            log.warn(
                    "문서 처리 시작 요청을 건너뜁니다. " +
                            "이미 시작되었거나 시작할 수 없는 상태입니다. " +
                            "processingGroupId={}",
                    processingGroupId
            );

            return false;
        }

        log.info(
                "문서 처리 시작 상태 전환 완료. " +
                        "processingGroupId={}, status={}, currentStep={}",
                processingGroupId,
                DocumentProcessingStatus.PROCESSING,
                DocumentProcessingStep.TEXT_EXTRACTING
        );

        return true;
    }

    /**
     * 현재 문서 처리 단계를 다음 단계로 변경한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStep(
            Long processingGroupId,
            DocumentProcessingStep nextStep
    ) {
        if (nextStep == null) {
            throw new IllegalArgumentException(
                    "문서 처리 단계는 필수입니다."
            );
        }

        DocumentProcessingGroup processingGroup =
                getProcessingGroup(processingGroupId);

        processingGroup.updateStep(nextStep);

        log.info(
                "문서 처리 단계 변경. processingGroupId={}, currentStep={}",
                processingGroupId,
                nextStep
        );
    }

    /**
     * 문서 처리 작업을 완료 상태로 변경한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(Long processingGroupId) {

        DocumentProcessingGroup processingGroup =
                getProcessingGroup(processingGroupId);

        processingGroup.complete();

        log.info(
                "문서 처리 완료 상태 반영. processingGroupId={}, status={}",
                processingGroupId,
                processingGroup.getStatus()
        );
    }

    /**
     * 문서 처리 작업을 실패 상태로 변경한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(
            Long processingGroupId,
            String errorMessage
    ) {
        DocumentProcessingGroup processingGroup =
                getProcessingGroup(processingGroupId);

        String resolvedErrorMessage =
                resolveErrorMessage(errorMessage);

        processingGroup.fail(resolvedErrorMessage);

        log.warn(
                "문서 처리 실패 상태 반영 완료. " +
                        "processingGroupId={}, errorMessage={}",
                processingGroupId,
                resolvedErrorMessage
        );
    }

    private DocumentProcessingGroup getProcessingGroup(
            Long processingGroupId
    ) {
        validateProcessingGroupId(processingGroupId);

        return processingGroupRepository
                .findById(processingGroupId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.DOCUMENT_PROCESSING_GROUP_NOT_FOUND
                        )
                );
    }

    private void validateProcessingGroupId(
            Long processingGroupId
    ) {
        if (processingGroupId == null
                || processingGroupId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
    }

    private String resolveErrorMessage(
            String errorMessage
    ) {
        if (errorMessage == null
                || errorMessage.isBlank()) {
            return DEFAULT_ERROR_MESSAGE;
        }

        String trimmedMessage =
                errorMessage.trim();

        if (trimmedMessage.length()
                <= MAX_ERROR_MESSAGE_LENGTH) {
            return trimmedMessage;
        }

        return trimmedMessage.substring(
                0,
                MAX_ERROR_MESSAGE_LENGTH
        );
    }
}