package com.aha.domain.document.service;

import com.aha.domain.document.config.DocumentUploadProperties;
import com.aha.domain.document.model.StoredDocumentFile;
import com.aha.domain.document.model.ValidatedDocumentFile;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class DocumentFileStorageService {

    private final Path baseUploadDirectory;
    private final long maxFileSizeBytes;
    private final Tika tika;

    public DocumentFileStorageService(
            @Value("${file.document-upload-dir:uploads}")
            String documentUploadDirectory,
            DocumentUploadProperties documentUploadProperties
    ) {
        this.baseUploadDirectory = Paths.get(documentUploadDirectory)
                .toAbsolutePath()
                .normalize();

        this.maxFileSizeBytes =
                documentUploadProperties.getMaxFileSizeBytes();

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
                resolveStoragePath(storageKey);

        try {
            createParentDirectory(storedFilePath);

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
            deleteFileQuietly(storedFilePath);
            throw exception;

        } catch (IOException | RuntimeException exception) {
            deleteFileQuietly(storedFilePath);

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

    /**
     * storageKey에 해당하는 파일을 삭제한다.
     *
     * LearningNoteCreateService에서 DB 생성이 실패했을 때
     * 이미 저장된 파일을 정리하기 위해 사용한다.
     */
    public void delete(String storageKey) {
        Path storedFilePath =
                resolveStoragePath(storageKey);

        try {
            Files.deleteIfExists(storedFilePath);
            deleteEmptyParentDirectories(storedFilePath);

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

        if (validatedFile.fileExtension() == null
                || validatedFile.fileExtension().isBlank()) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_STORAGE_FAILED
            );
        }
    }

    private String generateStoredFileName(
            String fileExtension
    ) {
        String normalizedExtension =
                fileExtension
                        .trim()
                        .toLowerCase(Locale.ROOT);

        return UUID.randomUUID()
                + "."
                + normalizedExtension;
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

        try (InputStream inputStream =
                     validatedFile.file().getInputStream()) {

            Files.copy(
                    inputStream,
                    storedFilePath
            );
        }
    }

    /**
     * 실제 디스크에 저장된 파일을 다시 검증한다.
     *
     * MultipartFile 검증 이후 파일 저장 과정에서
     * 데이터가 누락되거나 변경되지 않았는지 확인한다.
     */
    private void validateStoredFile(
            ValidatedDocumentFile validatedFile,
            Path storedFilePath
    ) throws IOException {
        if (!Files.isRegularFile(storedFilePath)) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_STORAGE_FAILED
            );
        }

        long storedFileSize =
                Files.size(storedFilePath);

        if (storedFileSize <= 0) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_FILE_EMPTY
            );
        }

        if (storedFileSize > maxFileSizeBytes) {
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
        Set<String> allowedMimeTypes =
                DocumentFileTypePolicy.getAllowedMimeTypes(
                        validatedFile.fileExtension()
                );

        if (allowedMimeTypes == null
                || allowedMimeTypes.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_STORAGE_FAILED
            );
        }

        try (InputStream inputStream =
                     Files.newInputStream(
                             storedFilePath
                     )) {

            String detectedMimeType =
                    tika.detect(
                                    inputStream,
                                    validatedFile.originalFileName()
                            )
                            .trim()
                            .toLowerCase(Locale.ROOT);

            if (!allowedMimeTypes.contains(
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
                        ErrorCode.DOCUMENT_FILE_MIME_TYPE_INVALID
                );
            }
        }
    }

    /**
     * 개별 파일 저장 도중 실패한 경우 부분적으로 생성된 파일을 삭제한다.
     *
     * 원래 발생한 저장 예외를 유지하기 위해 삭제 실패는 로그만 남긴다.
     */
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

    /**
     * 파일 삭제 후 비어 있는 userExam 및 user 디렉터리를 정리한다.
     *
     * 기본 업로드 디렉터리 자체는 삭제하지 않는다.
     */
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
                        currentDirectory.getParent();

                continue;
            }

            boolean isEmpty;

            try (var children =
                         Files.list(currentDirectory)) {
                isEmpty =
                        children.findAny().isEmpty();
            }

            if (!isEmpty) {
                return;
            }

            Files.deleteIfExists(
                    currentDirectory
            );

            currentDirectory =
                    currentDirectory.getParent();
        }
    }
}