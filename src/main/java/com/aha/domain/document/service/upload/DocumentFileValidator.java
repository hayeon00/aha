package com.aha.domain.document.service.upload;

import com.aha.domain.document.config.DocumentUploadProperties;
import com.aha.domain.document.enums.DocumentFileExtension;
import com.aha.domain.document.model.ValidatedDocumentFile;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class DocumentFileValidator {

    private static final String GENERIC_BINARY_MIME_TYPE =
            "application/octet-stream";

    private final DocumentUploadProperties documentUploadProperties;

    private final Tika tika = new Tika();

    public ValidatedDocumentFile validate(
            MultipartFile file
    ) {
        validateFileRequired(file);
        validateFileNotEmpty(file);
        validateFileSize(file);

        String originalFileName =
                resolveOriginalFileName(file);

        DocumentFileExtension fileExtension =
                extractAndValidateExtension(
                        originalFileName
                );

        String requestedMimeType =
                resolveRequestedMimeType(file);

        validateRequestedMimeType(
                fileExtension,
                requestedMimeType
        );

        String detectedMimeType =
                detectAndValidateMimeType(
                        file,
                        originalFileName,
                        fileExtension
                );

        return new ValidatedDocumentFile(
                file,
                originalFileName,
                fileExtension,
                detectedMimeType,
                file.getSize()
        );
    }

    private void validateFileRequired(
            MultipartFile file
    ) {
        if (file == null) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_FILE_REQUIRED
            );
        }
    }

    private void validateFileNotEmpty(
            MultipartFile file
    ) {
        if (file.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_FILE_EMPTY
            );
        }
    }

    private void validateFileSize(
            MultipartFile file
    ) {
        if (file.getSize()
                > documentUploadProperties
                .getMaxFileSizeBytes()) {

            throw new BusinessException(
                    ErrorCode.DOCUMENT_FILE_SIZE_EXCEEDED
            );
        }
    }

    private String resolveOriginalFileName(
            MultipartFile file
    ) {
        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null
                || originalFileName.isBlank()) {

            throw new BusinessException(
                    ErrorCode.DOCUMENT_FILE_NAME_INVALID
            );
        }

        String normalizedPath =
                originalFileName
                        .replace("\\", "/")
                        .trim();

        int separatorIndex =
                normalizedPath.lastIndexOf('/');

        String normalizedFileName =
                normalizedPath
                        .substring(separatorIndex + 1)
                        .trim();

        if (normalizedFileName.isBlank()) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_FILE_NAME_INVALID
            );
        }

        if (normalizedFileName.length()
                > documentUploadProperties
                .getMaxFileNameLength()) {

            throw new BusinessException(
                    ErrorCode.DOCUMENT_FILE_NAME_INVALID
            );
        }

        return normalizedFileName;
    }

    private DocumentFileExtension extractAndValidateExtension(
            String originalFileName
    ) {
        int extensionIndex =
                originalFileName.lastIndexOf('.');

        if (extensionIndex <= 0
                || extensionIndex
                == originalFileName.length() - 1) {

            throw new BusinessException(
                    ErrorCode.DOCUMENT_FILE_EXTENSION_INVALID
            );
        }

        String extension =
                originalFileName
                        .substring(extensionIndex + 1)
                        .trim()
                        .toLowerCase(Locale.ROOT);

        try {
            return DocumentFileExtension.from(
                    extension
            );

        } catch (IllegalArgumentException exception) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_FILE_EXTENSION_INVALID
            );
        }
    }

    private String resolveRequestedMimeType(
            MultipartFile file
    ) {
        String mimeType =
                file.getContentType();

        if (mimeType == null
                || mimeType.isBlank()) {

            throw new BusinessException(
                    ErrorCode.DOCUMENT_FILE_MIME_TYPE_INVALID
            );
        }

        int parameterIndex =
                mimeType.indexOf(';');

        String normalizedMimeType =
                (
                        parameterIndex >= 0
                                ? mimeType.substring(
                                0,
                                parameterIndex
                        )
                                : mimeType
                )
                        .trim()
                        .toLowerCase(Locale.ROOT);

        if (normalizedMimeType.isBlank()) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_FILE_MIME_TYPE_INVALID
            );
        }

        return normalizedMimeType;
    }

    private void validateRequestedMimeType(
            DocumentFileExtension fileExtension,
            String requestedMimeType
    ) {
        if (GENERIC_BINARY_MIME_TYPE.equals(
                requestedMimeType
        )) {
            return;
        }

        if (!fileExtension.supportsMimeType(
                requestedMimeType
        )) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_FILE_MIME_TYPE_INVALID
            );
        }
    }

    private String detectAndValidateMimeType(
            MultipartFile file,
            String originalFileName,
            DocumentFileExtension fileExtension
    ) {
        try (
                InputStream inputStream =
                        file.getInputStream()
        ) {
            String detectedMimeType =
                    tika.detect(
                            inputStream,
                            originalFileName
                    );

            String normalizedDetectedMimeType =
                    detectedMimeType
                            .trim()
                            .toLowerCase(Locale.ROOT);

            if (!fileExtension.supportsMimeType(
                    normalizedDetectedMimeType
            )) {
                throw new BusinessException(
                        ErrorCode.DOCUMENT_FILE_MIME_TYPE_INVALID
                );
            }

            return normalizedDetectedMimeType;

        } catch (IOException exception) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_FILE_UNREADABLE
            );
        }
    }
}