package com.aha.domain.ailearn.document.service.upload;

import com.aha.domain.ailearn.document.dto.api.response.DocumentBatchUploadResponseDto;
import com.aha.domain.ailearn.document.dto.upload.DocumentFileUploadResult;
import com.aha.domain.ailearn.document.dto.upload.PendingDocumentBatch;
import com.aha.domain.ailearn.document.dto.upload.PendingDocumentFile;
import com.aha.domain.ailearn.document.entity.DocumentProcessingGroup;
import com.aha.domain.ailearn.document.entity.SourceDocument;
import com.aha.domain.ailearn.document.repository.DocumentProcessingGroupRepository;
import com.aha.domain.ailearn.document.repository.SourceDocumentRepository;
import com.aha.domain.user.entity.UserExam;
import com.aha.domain.user.repository.UserExamRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

    // 이 함수에서는 처리그룹,문서,storageKey를 생성하고 그룹과 문서 상태를 Pending으로 저장한다.
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

        String originalFileName=file.getOriginalFilename();

        if(originalFileName==null ||  originalFileName.isBlank()){
            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_FILE_NAME);
        }

        return originalFileName;
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

        if(extensionIndex < 0 || extensionIndex == originalFileName.length()-1 ){
            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_FILE_EXTENSION);
        }

        return originalFileName.substring(extensionIndex+1).toLowerCase(Locale.ROOT);
    }


    public DocumentBatchUploadResponseDto completeUploadBatch(Long userId, Long processingGroupId, List<DocumentFileUploadResult> uploadResults) {
        return null;
    }
}
