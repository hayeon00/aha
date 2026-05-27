package com.aha.domain.ailearn.document.service;

import com.aha.domain.ailearn.document.dto.response.LearningSourceDocumentUploadResponseDto;
import com.aha.domain.ailearn.document.entity.DocumentProcessing;
import com.aha.domain.ailearn.document.entity.LearningSourceDocument;
import com.aha.domain.ailearn.document.repository.DocumentProcessingRepository;
import com.aha.domain.ailearn.document.repository.LearningSourceDocumentRepository;
import com.aha.domain.ailearn.document.type.ProcessingStatus;
import com.aha.domain.ailearn.document.type.ProcessingType;
import com.aha.domain.ailearn.document.type.SourceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class LearningSourceDocumentService {

    private final LearningSourceDocumentRepository sourceDocumentRepository;
    private final DocumentProcessingRepository processingRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public LearningSourceDocumentUploadResponseDto uploadDocument(
            Long examId,
            Long examPartId,
            Long examScopeNodeId,
            String title,
            SourceType sourceType,
            String description,
            MultipartFile file,
            Long requestedBy
    ) {
        FileStorageService.StoredFileInfo storedFile = fileStorageService.store(file);

        LearningSourceDocument sourceDocument = LearningSourceDocument.builder()
                .examId(examId)
                .examPartId(examPartId)
                .examScopeNodeId(examScopeNodeId)
                .title(title)
                .sourceType(sourceType)
                .originalFileName(storedFile.originalFileName())
                .storedFileName(storedFile.storedFileName())
                .filePath(storedFile.filePath())
                .fileExtension(storedFile.fileExtension())
                .mimeType(storedFile.mimeType())
                .fileSize(storedFile.fileSize())
                .description(description)
                .isActive(true)
                .build();

        LearningSourceDocument savedDocument = sourceDocumentRepository.save(sourceDocument);

        DocumentProcessing processing = DocumentProcessing.builder()
                .sourceDocumentId(savedDocument.getId())
                .processingType(ProcessingType.TEXT_EXTRACTION)
                .status(ProcessingStatus.PENDING)
                .requestedBy(requestedBy)
                .build();

        DocumentProcessing savedProcessing = processingRepository.save(processing);

        return new LearningSourceDocumentUploadResponseDto(
                savedDocument.getId(),
                savedProcessing.getId(),
                savedProcessing.getStatus().name()
        );
    }
}