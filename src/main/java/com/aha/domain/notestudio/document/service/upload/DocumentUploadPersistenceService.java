package com.aha.domain.notestudio.document.service.upload;

import com.aha.domain.notestudio.document.dto.upload.response.BatchUploadResponseDto;
import com.aha.domain.notestudio.document.dto.upload.response.UploadedResponseDto;
import com.aha.domain.notestudio.document.entity.DocumentProcessingGroup;
import com.aha.domain.notestudio.document.entity.SourceDocument;
import com.aha.domain.notestudio.document.event.DocumentUploadCompletedEvent;
import com.aha.domain.notestudio.document.repository.DocumentProcessingGroupRepository;
import com.aha.domain.notestudio.document.repository.SourceDocumentRepository;
import com.aha.domain.notestudio.document.service.upload.model.PendingDocumentBatch;
import com.aha.domain.notestudio.document.service.upload.model.PendingDocumentUpload;
import com.aha.domain.notestudio.document.service.upload.model.ValidatedDocumentFile;
import com.aha.domain.userexam.entity.UserExam;
import com.aha.domain.userexam.repository.UserExamRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentUploadPersistenceService {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;

    private final UserExamRepository userExamRepository;
    private final DocumentProcessingGroupRepository documentProcessingGroupRepository;
    private final SourceDocumentRepository sourceDocumentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public PendingDocumentBatch createPendingBatch(Long userId, Long userExamId, List<ValidatedDocumentFile> validatedFiles) {

        UserExam userExam = userExamRepository.findByIdAndUser_Id(userExamId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_EXAM_NOT_FOUND));

        DocumentProcessingGroup processingGroup =
                DocumentProcessingGroup.createPending(
                        userExam,
                        validatedFiles.size()
                );

        DocumentProcessingGroup savedProcessingGroup = documentProcessingGroupRepository.save(processingGroup);

        Long processingGroupId = savedProcessingGroup.getId();

        List<PendingDocumentUpload> pendingUploads = new ArrayList<>(validatedFiles.size());

        for (ValidatedDocumentFile validatedFile : validatedFiles) {

            String storedFileName = createStoredFileName(validatedFile.fileExtension());

            String storageKey =
                    createStorageKey(
                            userId,
                            userExamId,
                            processingGroupId,
                            storedFileName
                    );

            SourceDocument sourceDocument =
                    SourceDocument.createPending(
                            savedProcessingGroup,
                            validatedFile.originalFileName(),
                            storedFileName,
                            storageKey,
                            validatedFile.fileExtension(),
                            validatedFile.mimeType(),
                            validatedFile.fileSize()
                    );

            SourceDocument savedSourceDocument = sourceDocumentRepository.save(sourceDocument);

            pendingUploads.add(
                    new PendingDocumentUpload(
                            savedSourceDocument.getId(),
                            validatedFile,
                            storageKey
                    )
            );
        }

        return new PendingDocumentBatch(
                processingGroupId,
                pendingUploads
        );
    }

    private String createStoredFileName(String fileExtension) {
        return UUID.randomUUID()
                + "."
                + fileExtension;
    }

    private String createStorageKey(Long userId, Long userExamId, Long processingGroupId, String storedFileName) {

        return "documents/%d/%d/%d/%s".formatted(
                userId,
                userExamId,
                processingGroupId,
                storedFileName
        );
    }


    @Transactional
    public BatchUploadResponseDto completeUploadBatch(Long userId, Long processingGroupId) {

        DocumentProcessingGroup processingGroup =
                findProcessingGroup(
                        userId,
                        processingGroupId
                );

        List<SourceDocument> sourceDocuments = sourceDocumentRepository
                        .findAllByProcessingGroup_Id(
                                processingGroupId
                        );

        if (sourceDocuments.size() != processingGroup.getTotalFileCount()) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_UPLOAD_FAILED
            );
        }

        sourceDocuments.forEach(SourceDocument::markStored);

        processingGroup.markFilesUploaded();

        List<UploadedResponseDto> uploadedDocuments =
                sourceDocuments.stream()
                        .map(UploadedResponseDto::from)
                        .toList();

        eventPublisher.publishEvent(
                new DocumentUploadCompletedEvent(
                        processingGroupId
                )
        );

        return BatchUploadResponseDto.of(
                processingGroup,
                uploadedDocuments
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failUploadBatch(Long userId, Long processingGroupId, String errorMessage) {

        DocumentProcessingGroup processingGroup =
                findProcessingGroup(userId, processingGroupId);

        String resolvedErrorMessage = resolveErrorMessage(errorMessage);

        List<SourceDocument> sourceDocuments = sourceDocumentRepository
                        .findAllByProcessingGroup_Id(processingGroupId);

        for (SourceDocument sourceDocument : sourceDocuments) {
            sourceDocument.markUploadFailed(resolvedErrorMessage);
        }

        processingGroup.fail(resolvedErrorMessage);
    }

    private DocumentProcessingGroup findProcessingGroup(Long userId, Long processingGroupId) {

        if (userId == null || processingGroupId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return documentProcessingGroupRepository.findByIdAndUserExam_User_Id(processingGroupId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_PROCESSING_GROUP_NOT_FOUND));
    }

    private String resolveErrorMessage(String errorMessage) {

        String resolvedMessage = errorMessage == null || errorMessage.isBlank() ? ErrorCode.DOCUMENT_UPLOAD_FAILED
                          .getMessage() : errorMessage.trim();

        if (resolvedMessage.length() <= MAX_ERROR_MESSAGE_LENGTH) {
            return resolvedMessage;
        }

        return resolvedMessage.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }
}