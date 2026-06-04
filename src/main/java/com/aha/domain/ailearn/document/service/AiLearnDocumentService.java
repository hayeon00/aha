package com.aha.domain.ailearn.document.service;

import com.aha.domain.ailearn.document.dto.response.DocumentUploadResponseDto;
import com.aha.domain.ailearn.document.entity.DocumentProcessing;
import com.aha.domain.ailearn.document.entity.SourceDocument;
import com.aha.domain.ailearn.document.repository.DocumentProcessingRepository;
import com.aha.domain.ailearn.document.repository.SourceDocumentRepository;
import com.aha.domain.user.entity.UserExam;
import com.aha.domain.user.repository.UserExamRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiLearnDocumentService {

    private static final long MAX_DOCUMENT_FILE_SIZE = 50L * 1024 * 1024;
    private static final Set<String> ALLOWED_DOCUMENT_EXTENSIONS =
            Set.of("pdf", "docx", "txt");

    private final UserExamRepository userExamRepository;
    private final SourceDocumentRepository sourceDocumentRepository;
    private final DocumentProcessingRepository documentProcessingRepository;

    @Value("${file.document-upload-dir:uploads/documents}")
    private String documentUploadDir;

    @Transactional
    public DocumentUploadResponseDto uploadDocument(
            Long userId,
            Long userExamId,
            MultipartFile file
    ) {
        UserExam userExam = userExamRepository.findByIdAndUser_Id(userExamId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_EXAM_NOT_FOUND));

        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_FILE);
        }

        if (file.getSize() > MAX_DOCUMENT_FILE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_FILE);
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank() || !originalFileName.contains(".")) {
            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_FILE);
        }

        String extension = originalFileName
                .substring(originalFileName.lastIndexOf(".") + 1)
                .toLowerCase();

        if (!ALLOWED_DOCUMENT_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_FILE);
        }

        try {
            String storedFileName = UUID.randomUUID() + "." + extension;

            Path uploadPath = Paths
                    .get(documentUploadDir, String.valueOf(userId), String.valueOf(userExamId))
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(uploadPath);

            Path targetPath = uploadPath
                    .resolve(storedFileName)
                    .toAbsolutePath()
                    .normalize();

            file.transferTo(targetPath.toFile());

            String storageKey = "documents/" + userId + "/" + userExamId + "/" + storedFileName;

            SourceDocument sourceDocument = SourceDocument.builder()
                    .userExam(userExam)
                    .originalFileName(originalFileName)
                    .storedFileName(storedFileName)
                    .storageKey(storageKey)
                    .fileExtension(extension.toUpperCase())
                    .mimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                    .fileSize(file.getSize())
                    .status("UPLOADED")
                    .isActive(true)
                    .build();

            SourceDocument savedSourceDocument = sourceDocumentRepository.save(sourceDocument);

            DocumentProcessing documentProcessing = DocumentProcessing.builder()
                    .sourceDocument(savedSourceDocument)
                    .status("PENDING")
                    .currentStep("FILE_UPLOADED")
                    .progressRate(0)
                    .build();

            DocumentProcessing savedDocumentProcessing =
                    documentProcessingRepository.save(documentProcessing);

            return DocumentUploadResponseDto.of(
                    savedSourceDocument,
                    savedDocumentProcessing
            );
        } catch (Exception e) {
            log.error(
                    "학습 문서 업로드 실패. userId={}, userExamId={}, originalFileName={}, uploadDir={}",
                    userId,
                    userExamId,
                    originalFileName,
                    documentUploadDir,
                    e
            );

            throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);
        }
    }
}