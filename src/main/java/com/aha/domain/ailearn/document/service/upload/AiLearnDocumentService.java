package com.aha.domain.ailearn.document.service.upload;

import com.aha.domain.ailearn.document.dto.api.response.DocumentBatchUploadResponseDto;
import com.aha.domain.ailearn.document.dto.upload.DocumentFileUploadResult;
import com.aha.domain.ailearn.document.dto.upload.PendingDocumentBatch;
import com.aha.domain.ailearn.document.dto.upload.PendingDocumentFile;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiLearnDocumentService {

    private final DocumentFileStorageService documentFileStorageService;
    private final DocumentUploadPersistenceService documentUploadPersistenceService;

    // 여러 학습 문서 업로드

    public DocumentBatchUploadResponseDto uploadDocumentsBatch(
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

        List<DocumentFileUploadResult> uploadResults =
                storePendingFiles(
                        pendingBatch,
                        validatedFiles
                );

        return documentUploadPersistenceService.completeUploadBatch(
                userId,
                pendingBatch.getProcessingGroupId(),
                uploadResults
        );


    }



    // ================ 내부 Method ===========================

    private List<DocumentFileUploadResult> storePendingFiles(PendingDocumentBatch pendingBatch, List<MultipartFile> files) {
        List<PendingDocumentFile> pendingFiles = pendingBatch.getDocuments();

        validatePendingFileCount(pendingFiles, files);

        List<DocumentFileUploadResult> uploadResults = new ArrayList<>(files.size());

        for(int index=0; index<files.size(); index++) {
            MultipartFile file = files.get(index);

            PendingDocumentFile pendingFile = pendingFiles.get(index);

            try{
                documentFileStorageService.storeFile(
                        file,
                        pendingFile.getStorageKey()
                );

                uploadResults.add(DocumentFileUploadResult.success(
                        pendingFile.getSourceDocumentId(),
                        pendingFile.getOriginalFileName()
                ));
            }catch(BusinessException exception){
                String errorMessage = exception.getErrorCode().getMessage();

                uploadResults.add(DocumentFileUploadResult.failure(
                        pendingFile.getSourceDocumentId(),
                        pendingFile.getOriginalFileName(),
                        errorMessage
                ));

                log.error( "문서 파일 저장 실패. processingGroupId={}, sourceDocumentId={}, originalFileName={}, storageKey={}",
                        pendingBatch.getProcessingGroupId(),
                        pendingFile.getSourceDocumentId(),
                        pendingFile.getOriginalFileName(),
                        pendingFile.getStorageKey(),
                        exception
                );

            }

        }

        return List.copyOf(uploadResults);
    }

    private void validatePendingFileCount(List<PendingDocumentFile> pendingFiles, List<MultipartFile> files) {

        if(pendingFiles == null || files == null || pendingFiles.size() != files.size()) {
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




}