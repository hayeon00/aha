package com.aha.domain.ailearn.document.service.upload;

import com.aha.domain.ailearn.document.config.DocumentUploadProperties;
import com.aha.domain.ailearn.document.service.upload.model.ValidatedDocumentFile;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 업로드된 파일 목록을 받아서 검증을 거쳐
 * 통과된 검증 객체 리스트를 만들어서 반환
 */

@Component
@RequiredArgsConstructor
public class DocumentFileValidator {

    private final DocumentUploadProperties documentUploadProperties;

    public List<ValidatedDocumentFile> validate(List<MultipartFile> files) {

        validateFileCount(files);

        long totalFileSize = 0L;

        List<ValidatedDocumentFile> validatedFiles = new ArrayList<>(files.size());

        for (MultipartFile file : files) {
            ValidatedDocumentFile validatedFile = validateFile(file);

            validatedFiles.add(validatedFile);
            totalFileSize += validatedFile.fileSize();
        }

        validateTotalFileSize(totalFileSize);

        return List.copyOf(validatedFiles);
    }

    // 파일 총 개수 검증
    private void validateFileCount(List<MultipartFile> files) {

        if (files == null || files.isEmpty()) {
            throw new BusinessException(ErrorCode.DOCUMENT_FILE_REQUIRED);
        }

        if (files.size() > documentUploadProperties.getMaxFileCount()) {
            throw new BusinessException(ErrorCode.DOCUMENT_FILE_COUNT_EXCEEDED);
        }
    }

    // 파일 한개 검증
    private ValidatedDocumentFile validateFile(MultipartFile file) {
        if (file == null) {
            throw new BusinessException(ErrorCode.DOCUMENT_FILE_REQUIRED);
        }

        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.DOCUMENT_FILE_EMPTY);
        }

        if (file.getSize() > documentUploadProperties.getMaxFileSizeBytes()) {
            throw new BusinessException(ErrorCode.DOCUMENT_FILE_SIZE_EXCEEDED);
        }

        String originalFileName = resolveOriginalFileName(file);

        String fileExtension = extractExtension(originalFileName);

        String mimeType = requireMimeType(file);

        validateMimeType(fileExtension, mimeType);

        return new ValidatedDocumentFile(
                file,
                originalFileName,
                fileExtension,
                mimeType,
                file.getSize()
        );
    }

    // 파일 이름 검증
    private String resolveOriginalFileName(MultipartFile file) {

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_FILE_NAME
            );
        }

        String normalizedPath = originalFileName
                .replace("\\", "/")
                .trim();

        int separatorIndex = normalizedPath.lastIndexOf('/');

        String normalizedFileName = normalizedPath
                .substring(separatorIndex + 1)
                .trim();

        if (normalizedFileName.isBlank() || normalizedFileName.length()
                > documentUploadProperties.getMaxFileNameLength()) {
            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_FILE_NAME);
        }

        return normalizedFileName;
    }

    // 확장자 검증
    private String extractExtension(String originalFileName) {

        int extensionIndex = originalFileName.lastIndexOf('.');

        if (extensionIndex <= 0 || extensionIndex == originalFileName.length() - 1) {
            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_FILE_EXTENSION);
        }

        String extension = originalFileName
                .substring(extensionIndex + 1)
                .toLowerCase(Locale.ROOT);

        if (!DocumentFileTypePolicy.supportsExtension(extension)) {
            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_FILE_EXTENSION);
        }

        return extension;
    }

    // 파일 형식 가져오기
    private String requireMimeType(MultipartFile file) {

        String mimeType = file.getContentType();

        if (mimeType == null || mimeType.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_MIME_TYPE);
        }

        return mimeType;
    }

    // 파일 형식 검증
    private void validateMimeType(String fileExtension, String mimeType) {

        Set<String> allowedMimeTypes = DocumentFileTypePolicy.getAllowedMimeTypes(fileExtension);

        if (allowedMimeTypes == null || !allowedMimeTypes.contains(mimeType)) {
            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_MIME_TYPE);
        }
    }

    // 총 파일 크기 검증
    private void validateTotalFileSize(long totalFileSize) {
        if (totalFileSize > documentUploadProperties.getMaxTotalSizeBytes()) {
            throw new BusinessException(ErrorCode.DOCUMENT_TOTAL_FILE_SIZE_EXCEEDED);
        }
    }
}