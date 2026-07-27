package com.aha.domain.notestudio.document.service.upload;

import com.aha.domain.notestudio.document.dto.upload.response.BatchUploadResponseDto;
import com.aha.domain.notestudio.document.service.upload.model.PendingDocumentBatch;
import com.aha.domain.notestudio.document.service.upload.model.PendingDocumentUpload;
import com.aha.domain.notestudio.document.service.upload.model.ValidatedDocumentFile;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 문서 업로드 전체 흐름을 조율한다.
 *
 * 파일 검증
 * → 처리 그룹과 SourceDocument 생성
 * → 실제 파일을 임시 폴더에 저장
 * → 저장된 실제 파일 재검증
 * → 임시 폴더를 최종 폴더로 이동
 * → 업로드 완료 상태 반영
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentUploadService {

    private final DocumentFileValidator documentFileValidator;
    private final DocumentFileStorageService documentFileStorageService;
    private final DocumentUploadPersistenceService documentUploadPersistenceService;

    public BatchUploadResponseDto uploadDocumentsBatch(Long userId, Long userExamId, List<MultipartFile> files) {

        List<ValidatedDocumentFile> validatedFiles =
                documentFileValidator.validate(files);

        PendingDocumentBatch pendingBatch =
                documentUploadPersistenceService
                        .createPendingBatch(
                                userId,
                                userExamId,
                                validatedFiles
                        );


        Long processingGroupId = pendingBatch.processingGroupId();

        String representativeStorageKey =
                pendingBatch.uploads()
                        .get(0)
                        .storageKey();

        boolean movedToFinalDirectory = false;

        try {

            // 검증된 파일을 임시폴더에 저장
            storeTemporaryFiles(pendingBatch);

            // 재검증 성공시 임시폴더->최종폴더
            documentFileStorageService.commitTemporaryBatch(
                    processingGroupId,
                    representativeStorageKey
            );

            movedToFinalDirectory = true;

            return documentUploadPersistenceService
                    .completeUploadBatch(
                            userId,
                            processingGroupId
                    );

        } catch (RuntimeException exception) {
            log.error(
                    "문서 배치 업로드 실패. userId={}, userExamId={}, processingGroupId={}",
                    userId,
                    userExamId,
                    processingGroupId,
                    exception
            );

            cleanupFailedBatch(
                    movedToFinalDirectory,
                    processingGroupId,
                    representativeStorageKey
            );

            saveUploadFailureStatus(
                    userId,
                    processingGroupId,
                    exception
            );

            throw exception;
        }
    }

   // ================================================================================

    // 임시 폴더에 저장
    private void storeTemporaryFiles(PendingDocumentBatch pendingBatch) {

        for (PendingDocumentUpload pendingUpload : pendingBatch.uploads()) {
            documentFileStorageService.storeTemporaryFile(
                    pendingBatch.processingGroupId(),
                    pendingUpload.validatedFile(),
                    pendingUpload.storageKey()
            );
        }
    }

    // 업로드 실패
    private void saveUploadFailureStatus(Long userId, Long processingGroupId, RuntimeException exception) {

        try {
            documentUploadPersistenceService.failUploadBatch(
                    userId,
                    processingGroupId,
                    resolveUploadErrorMessage(exception)
            );

        } catch (RuntimeException statusException) {
            log.error(
                    "업로드 실패 상태 저장 실패. processingGroupId={}",
                    processingGroupId,
                    statusException
            );
        }
    }

    private String resolveUploadErrorMessage(RuntimeException exception) {

        if (exception instanceof BusinessException businessException) {
            return businessException
                    .getErrorCode()
                    .getMessage();
        }

        return ErrorCode.DOCUMENT_UPLOAD_FAILED.getMessage();
    }


    // 업로드 실패시 배치 파일 삭제
    private void cleanupFailedBatch(boolean movedToFinalDirectory, Long processingGroupId, String representativeStorageKey) {

        try {
            if (movedToFinalDirectory) {
                documentFileStorageService.deleteFinalBatch(representativeStorageKey);
                return;
            }

            documentFileStorageService.deleteTemporaryBatch(processingGroupId);

        } catch (RuntimeException cleanupException) {
            log.error(
                    "업로드 실패 파일 정리 실패. processingGroupId={}, movedToFinalDirectory={}",
                    processingGroupId,
                    movedToFinalDirectory,
                    cleanupException
            );
        }
    }
}