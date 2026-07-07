package com.aha.domain.ailearn.document.service.upload;

import com.aha.domain.ailearn.document.dto.upload.response.BatchUploadResponseDto;
import com.aha.domain.ailearn.document.dto.upload.response.UploadedResponseDto;
import com.aha.domain.ailearn.document.event.DocumentUploadCompletedEvent;
import com.aha.domain.ailearn.document.service.upload.model.PendingDocumentBatch;
import com.aha.domain.ailearn.document.service.upload.model.PendingDocumentFile;
import com.aha.domain.ailearn.document.entity.DocumentProcessingGroup;
import com.aha.domain.ailearn.document.entity.SourceDocument;
import com.aha.domain.ailearn.document.repository.DocumentProcessingGroupRepository;
import com.aha.domain.ailearn.document.repository.SourceDocumentRepository;
import com.aha.domain.user.entity.UserExam;
import com.aha.domain.user.repository.UserExamRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class DocumentUploadPersistenceService {

    private final UserExamRepository userExamRepository;
    private final DocumentProcessingGroupRepository documentProcessingGroupRepository;
    private final SourceDocumentRepository sourceDocumentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public PendingDocumentBatch createPendingBatch(
            Long userId,
            Long userExamId,
            List<MultipartFile> files) {

        UserExam userExam=userExamRepository.findByIdAndUser_Id(userExamId,userId)
                .orElseThrow(()-> new BusinessException(
                        ErrorCode.USER_EXAM_NOT_FOUND
                ));

        DocumentProcessingGroup processingGroup =
                DocumentProcessingGroup.createPending(
                        userExam,
                        files.size()
                );

        DocumentProcessingGroup savedProcessingGroup =
                documentProcessingGroupRepository.save(processingGroup);

        Long processingGroupId = savedProcessingGroup.getId();

        List<PendingDocumentFile> pendingDocuments = new ArrayList<>(files.size());

        for (MultipartFile file : files) {
            String originalFileName=requireOriginalFileName(file);

            String fileExtension = extractExtension(originalFileName);
            String storedFileName = createStoredFileName(fileExtension);

            String storageKey=createStorageKey(
                    userId,
                    userExamId,
                    processingGroupId,
                    storedFileName
            );

            String mimeType = requireMimeType(file);

            SourceDocument sourceDocument = SourceDocument.createPending(
                    savedProcessingGroup,
                    originalFileName,
                    storedFileName,
                    storageKey,
                    fileExtension,
                    mimeType,
                    file.getSize()
            );

            SourceDocument savedSourceDocument = sourceDocumentRepository.save(sourceDocument);

            pendingDocuments.add(new PendingDocumentFile(
                    savedSourceDocument.getId(),
                    originalFileName,
                    storageKey
            ));

        }

        return new PendingDocumentBatch(
                processingGroupId,
                List.copyOf(pendingDocuments)
        );
    }



    // ===================== 내부 Method ==============================

    private String requireOriginalFileName(MultipartFile file) {

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {

            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_FILE_NAME);
        }

        String normalizedPath = originalFileName
                        .replace("\\", "/")
                        .trim();

        int separatorIndex = normalizedPath.lastIndexOf('/');

        String normalizedFileName = normalizedPath
                        .substring(separatorIndex + 1)
                        .trim();

        if (normalizedFileName.isBlank()
                || normalizedFileName.length() > 255) {
            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_FILE);

        }

        return normalizedFileName;
    }

    private String createStoredFileName(String fileExtension) {

        return UUID.randomUUID()+"."+fileExtension;
    }

    private String requireMimeType(MultipartFile file) {

        String mimeType=file.getContentType();
        if(mimeType==null || mimeType.isBlank()){
            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_MIME_TYPE);
        }

        return mimeType;
    }

    private String createStorageKey(Long userId, Long userExamId, Long processingGroupId, String storedFileName) {

        return "documents/%d/%d/%d/%s".formatted(
                userId,
                userExamId,
                processingGroupId,
                storedFileName
        );
    }

    private String extractExtension(String originalFileName) {

        int extensionIndex = originalFileName.lastIndexOf('.');

        if(extensionIndex <= 0 || extensionIndex == originalFileName.length()-1 ){
            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_FILE_EXTENSION);
        }

        return originalFileName.substring(extensionIndex + 1).toLowerCase(Locale.ROOT);
    }


    @Transactional
    public BatchUploadResponseDto completeUploadBatch(Long userId, Long processingGroupId) {

        DocumentProcessingGroup processingGroup = findProcessingGroup(userId, processingGroupId);

        processingGroup.markFilesUploaded();

        List<SourceDocument> sourceDocuments = sourceDocumentRepository.findAllByProcessingGroup_Id(processingGroupId);

        List<UploadedResponseDto> documents = sourceDocuments.stream().map(UploadedResponseDto::from).toList();

        eventPublisher.publishEvent(new DocumentUploadCompletedEvent(processingGroupId));

        return BatchUploadResponseDto.of(
                processingGroup,
                documents
        );

    }

    private DocumentProcessingGroup findProcessingGroup(Long userId, Long processingGroupId) {

        if(userId==null || processingGroupId==null){
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return documentProcessingGroupRepository.findByIdAndUserExam_User_Id(processingGroupId, userId)
                .orElseThrow(()-> new BusinessException(ErrorCode.DOCUMENT_PROCESSING_GROUP_NOT_FOUND));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failUploadBatch(Long userId, Long processingGroupId, String errorMessage) {

        DocumentProcessingGroup processingGroup = findProcessingGroup(userId, processingGroupId);

        String resolvedErrorMessage = resolveErrorMessage(errorMessage);

        List<SourceDocument> sourceDocuments = sourceDocumentRepository.findAllByProcessingGroup_Id(processingGroupId);

        for(SourceDocument sourceDocument : sourceDocuments){
            sourceDocument.markUploadFailed(resolvedErrorMessage);
        }

        processingGroup.fail(resolvedErrorMessage);
    }

    private String resolveErrorMessage(String errorMessage) {

        String resolvedMessage = errorMessage ==null || errorMessage.isBlank() ? ErrorCode.DOCUMENT_UPLOAD_FAILED.getMessage() : errorMessage;

        int maxLength = 1000;

        return resolvedMessage.length() > maxLength ? resolvedMessage.substring(0, maxLength) : resolvedMessage;
    }
}
