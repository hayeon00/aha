package com.aha.domain.ailearn.document.service.upload;

import com.aha.domain.ailearn.document.dto.upload.response.BatchUploadResponseDto;
import com.aha.domain.ailearn.document.service.upload.model.PendingDocumentBatch;
import com.aha.domain.ailearn.document.service.upload.model.PendingDocumentFile;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiLearnDocumentService {

    private final DocumentFileStorageService documentFileStorageService;
    private final DocumentUploadPersistenceService documentUploadPersistenceService;


    public BatchUploadResponseDto uploadDocumentsBatch(
            Long userId,
            Long userExamId,
            List<MultipartFile> files
    ) {
        List<MultipartFile> validatedFiles =
                documentFileStorageService.validateFiles(files);

        PendingDocumentBatch pendingBatch =
                documentUploadPersistenceService.createPendingBatch(
                        userId,
                        userExamId,
                        validatedFiles
                );

        Long processingGroupId = pendingBatch.getProcessingGroupId();

        String firstStorageKey = pendingBatch.getDocuments().get(0).getStorageKey();

        boolean batchCommitted = false;

        try{
            storePendingFiles(
                    pendingBatch,
                    validatedFiles
            );

            documentFileStorageService.commitTemporaryBatch(
                    processingGroupId,
                    firstStorageKey);

            batchCommitted = true;

            return documentUploadPersistenceService.completeUploadBatch(userId, processingGroupId);

        }catch (RuntimeException exception){

            log.error(
                    "문서 배치 업로드 실패. userId={}, userExamId={}, processingGroupId={}",
                    userId,
                    userExamId,
                    processingGroupId,
                    exception
            );

            cleanupFailedBatch(batchCommitted, processingGroupId, firstStorageKey);

            try{
                documentUploadPersistenceService.failUploadBatch(
                        userId,
                        processingGroupId,
                        resolveUploadErrorMessage(exception)
                );

            }catch (RuntimeException statusException){
                log.error(
                        "업로드 실패 상태 저장 실패. processingGroupId={}",
                        processingGroupId,
                        statusException
                );

            }

            throw exception;
        }

    }


    // ================ 내부 Method ===========================

    private void storePendingFiles(PendingDocumentBatch pendingBatch, List<MultipartFile> files) {
        List<PendingDocumentFile> pendingFiles = pendingBatch.getDocuments();

        validatePendingFileCount(pendingFiles, files);

        Long processingGroupId = pendingBatch.getProcessingGroupId();

            for(int index=0; index<files.size(); index++) {
                MultipartFile file = files.get(index);

                PendingDocumentFile pendingFile = pendingFiles.get(index);

                documentFileStorageService.storeTemporaryFile(
                        processingGroupId,
                        file,
                        pendingFile.getStorageKey()
                );
            }
    }

    private void validatePendingFileCount(List<PendingDocumentFile> pendingFiles, List<MultipartFile> files) {

        if(pendingFiles == null || files == null || pendingFiles.isEmpty() || files.isEmpty() || pendingFiles.size() != files.size()) {
            throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);
        }
    }

    private String resolveUploadErrorMessage(Exception exception) {

        if(exception instanceof BusinessException businessException) {
            return businessException
                    .getErrorCode()
                    .getMessage();
        }

        if(exception.getMessage() == null || exception.getMessage().isBlank()) {
            return ErrorCode.DOCUMENT_UPLOAD_FAILED.getMessage();
        }

        return exception.getMessage();
    }

    private void cleanupFailedBatch(boolean batchCommitted, Long processingGroupId, String firstStorageKey) {
        try{
            if(batchCommitted){
                documentFileStorageService.deleteFinalBatch(firstStorageKey);
                return;
            }

            documentFileStorageService.deleteTemporaryBatch(processingGroupId);


        }catch (RuntimeException cleanupException){
            log.error(
                    "업로드 실패 파일 정리 실패. processingGroupId={}, batchCommitted={}",
                    processingGroupId,
                    batchCommitted,
                    cleanupException
            );
        }
    }
}