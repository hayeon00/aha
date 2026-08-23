package com.aha.domain.document.service.storage;

import com.aha.domain.document.config.DocumentUploadProperties;
import com.aha.domain.document.enums.DocumentFileExtension;
import com.aha.domain.document.model.StoredDocumentFile;
import com.aha.domain.document.model.ValidatedDocumentFile;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
public class DocumentFileStorageService {

    private final Path baseUploadDirectory;
    private final long maxFileSizeBytes;
    private final Tika tika;

    public DocumentFileStorageService(
            DocumentUploadProperties documentUploadProperties
    ) {
        this.baseUploadDirectory =
                Paths.get(
                                documentUploadProperties
                                        .getDirectory()
                        )
                        .toAbsolutePath()
                        .normalize();

        this.maxFileSizeBytes =
                documentUploadProperties
                        .getMaxFileSizeBytes();

        this.tika = new Tika();
    }

    public StoredDocumentFile store(
            Long userId,
            Long userExamId,
            ValidatedDocumentFile validatedFile
    ) {
        validateStoreArguments(
                userId,
                userExamId,
                validatedFile
        );

        String storedFileName =
                generateStoredFileName(
                        validatedFile.fileExtension()
                );

        String storageKey =
                generateStorageKey(
                        userId,
                        userExamId,
                        storedFileName
                );

        Path storedFilePath =
                resolveStoragePath(
                        storageKey
                );

        try {
            createParentDirectory(
                    storedFilePath
            );

            saveFile(
                    validatedFile,
                    storedFilePath
            );

            validateStoredFile(
                    validatedFile,
                    storedFilePath
            );

            return new StoredDocumentFile(
                    storedFileName,
                    storageKey
            );

        } catch (BusinessException exception) {
            deleteFileQuietly(
                    storedFilePath
            );

            throw exception;

        } catch (IOException | RuntimeException exception) {
            deleteFileQuietly(
                    storedFilePath
            );

            log.error(
                    "문서 파일 저장에 실패했습니다. userId={}, userExamId={}, storageKey={}",
                    userId,
                    userExamId,
                    storageKey,
                    exception
            );

            throw new BusinessException(
                    ErrorCode.DOCUMENT_STORAGE_FAILED
            );
        }
    }

    public void delete(
            String storageKey
    ) {
        Path storedFilePath =
                resolveStoragePath(
                        storageKey
                );

        try {
            Files.deleteIfExists(
                    storedFilePath
            );

            deleteEmptyParentDirectories(
                    storedFilePath
            );

        } catch (IOException exception) {
            log.error(
                    "문서 파일 삭제에 실패했습니다. storageKey={}, path={}",
                    storageKey,
                    storedFilePath,
                    exception
            );

            throw new BusinessException(
                    ErrorCode.DOCUMENT_STORAGE_FAILED
            );
        }
    }

    private void validateStoreArguments(
            Long userId,
            Long userExamId,
            ValidatedDocumentFile validatedFile
    ) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_STORAGE_FAILED
            );
        }

        if (userExamId == null || userExamId <= 0) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_STORAGE_FAILED
            );
        }

        if (validatedFile == null
                || validatedFile.file() == null) {

            throw new BusinessException(
                    ErrorCode.DOCUMENT_STORAGE_FAILED
            );
        }

        if (validatedFile.fileExtension() == null) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_STORAGE_FAILED
            );
        }
    }

    private String generateStoredFileName(
            DocumentFileExtension fileExtension
    ) {
        return UUID.randomUUID()
                + "."
                + fileExtension.getValue();
    }

    private String generateStorageKey(
            Long userId,
            Long userExamId,
            String storedFileName
    ) {
        return Path.of(
                        userId.toString(),
                        userExamId.toString(),
                        storedFileName
                )
                .toString()
                .replace("\\", "/");
    }

    private Path resolveStoragePath(
            String storageKey
    ) {
        if (storageKey == null
                || storageKey.isBlank()) {

            throw new BusinessException(
                    ErrorCode.DOCUMENT_STORAGE_FAILED
            );
        }

        try {
            Path resolvedPath =
                    baseUploadDirectory
                            .resolve(storageKey)
                            .toAbsolutePath()
                            .normalize();

            validatePathInsideUploadDirectory(
                    resolvedPath
            );

            return resolvedPath;

        } catch (BusinessException exception) {
            throw exception;

        } catch (RuntimeException exception) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_STORAGE_FAILED
            );
        }
    }

    private void validatePathInsideUploadDirectory(
            Path targetPath
    ) {
        if (targetPath == null
                || !targetPath.startsWith(
                baseUploadDirectory
        )) {

            throw new BusinessException(
                    ErrorCode.DOCUMENT_STORAGE_FAILED
            );
        }
    }

    private void createParentDirectory(
            Path storedFilePath
    ) throws IOException {
        Path parentDirectory =
                storedFilePath.getParent();

        if (parentDirectory == null) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_STORAGE_FAILED
            );
        }

        Files.createDirectories(
                parentDirectory
        );
    }

    private void saveFile(
            ValidatedDocumentFile validatedFile,
            Path storedFilePath
    ) throws IOException {
        if (Files.exists(storedFilePath)) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_STORAGE_FAILED
            );
        }

        try (
                InputStream inputStream =
                        validatedFile
                                .file()
                                .getInputStream()
        ) {
            Files.copy(
                    inputStream,
                    storedFilePath
            );
        }
    }

    private void validateStoredFile(
            ValidatedDocumentFile validatedFile,
            Path storedFilePath
    ) throws IOException {
        if (!Files.isRegularFile(
                storedFilePath
        )) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_STORAGE_FAILED
            );
        }

        long storedFileSize =
                Files.size(
                        storedFilePath
                );

        if (storedFileSize <= 0) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_FILE_EMPTY
            );
        }

        if (storedFileSize
                > maxFileSizeBytes) {

            throw new BusinessException(
                    ErrorCode.DOCUMENT_FILE_SIZE_EXCEEDED
            );
        }

        if (storedFileSize
                != validatedFile.fileSize()) {

            throw new BusinessException(
                    ErrorCode.DOCUMENT_STORAGE_FAILED
            );
        }

        validateStoredMimeType(
                validatedFile,
                storedFilePath
        );
    }

    private void validateStoredMimeType(
            ValidatedDocumentFile validatedFile,
            Path storedFilePath
    ) throws IOException {
        try (
                InputStream inputStream =
                        Files.newInputStream(
                                storedFilePath
                        )
        ) {
            String detectedMimeType =
                    tika.detect(
                                    inputStream,
                                    validatedFile
                                            .originalFileName()
                            )
                            .trim()
                            .toLowerCase(
                                    Locale.ROOT
                            );

            if (!validatedFile
                    .fileExtension()
                    .supportsMimeType(
                            detectedMimeType
                    )) {

                log.warn(
                        "저장된 파일의 MIME 타입이 확장자와 일치하지 않습니다. "
                                + "fileName={}, extension={}, detectedMimeType={}",
                        validatedFile.originalFileName(),
                        validatedFile.fileExtension(),
                        detectedMimeType
                );

                throw new BusinessException(
                        ErrorCode
                                .DOCUMENT_FILE_MIME_TYPE_INVALID
                );
            }
        }
    }

    private void deleteFileQuietly(
            Path storedFilePath
    ) {
        if (storedFilePath == null) {
            return;
        }

        try {
            Files.deleteIfExists(
                    storedFilePath
            );

            deleteEmptyParentDirectories(
                    storedFilePath
            );

        } catch (IOException cleanupException) {
            log.error(
                    "저장 실패 후 부분 파일 정리에 실패했습니다. path={}",
                    storedFilePath,
                    cleanupException
            );
        }
    }

    private void deleteEmptyParentDirectories(
            Path storedFilePath
    ) throws IOException {
        Path currentDirectory =
                storedFilePath.getParent();

        while (currentDirectory != null
                && !currentDirectory.equals(
                baseUploadDirectory
        )) {

            if (!Files.isDirectory(
                    currentDirectory
            )) {
                currentDirectory =
                        currentDirectory
                                .getParent();

                continue;
            }

            boolean isEmpty;

            try (
                    var children =
                            Files.list(
                                    currentDirectory
                            )
            ) {
                isEmpty =
                        children.findAny()
                                .isEmpty();
            }

            if (!isEmpty) {
                return;
            }

            Files.deleteIfExists(
                    currentDirectory
            );

            currentDirectory =
                    currentDirectory
                            .getParent();
        }
    }

    public Path resolveForRead(
            String storageKey
    ) {
        Path filePath =
                resolveStoragePath(
                        storageKey
                );

        if (!Files.isRegularFile(
                filePath
        )) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_STORAGE_FAILED
            );
        }

        return filePath;
    }
}