package com.aha.domain.ailearn.document.service.upload;

import com.aha.domain.ailearn.document.config.DocumentUploadProperties;
import com.aha.domain.ailearn.document.service.upload.model.ValidatedDocumentFile;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Set;

/**
 * 문서 파일의 실제 저장과 삭제를 담당한다.
 *
 * 검증된 업로드 파일을 처리 그룹별 임시 폴더에 저장하고,
 * 저장된 파일의 크기와 MIME 타입을 다시 검증한다.
 *
 * 배치에 포함된 모든 파일의 저장과 재검증이 성공하면
 * 임시 폴더 전체를 최종 저장 경로로 이동한다.
 *
 * 업로드 과정에서 오류가 발생한 경우에는
 * 실패 시점에 따라 임시 폴더 또는 최종 폴더를 삭제한다.
 *
 * 주요 역할
 * - 업로드 파일 임시 저장
 * - 저장된 실제 파일의 크기 및 MIME 타입 재검증
 * - 임시 배치 폴더를 최종 저장 폴더로 이동
 * - 업로드 실패 시 임시 또는 최종 파일 정리
 * - 저장 경로가 허용된 업로드 디렉터리 내부인지 검증
 */

@Slf4j
@Service
public class DocumentFileStorageService {

    private final long maxFileSizeBytes;
    private final Tika tika;
    private final Path baseUploadDirectory;
    private final Path temporaryUploadDirectory;

    public DocumentFileStorageService(@Value("${file.document-upload-dir:uploads}") String documentUploadDir
            , DocumentUploadProperties documentUploadProperties) {

        this.tika = new Tika();

        this.baseUploadDirectory =
                Paths.get(documentUploadDir)
                        .toAbsolutePath()
                        .normalize();

        this.temporaryUploadDirectory =
                this.baseUploadDirectory
                        .resolve("temp")
                        .normalize();

        this.maxFileSizeBytes = documentUploadProperties.getMaxFileSizeBytes();
    }

    /**
     * 검증된 파일을 처리 그룹의 임시 폴더에 저장하고,
     * 실제 저장된 파일을 다시 검증한다.
     */
    public void storeTemporaryFile(Long processingGroupId, ValidatedDocumentFile validatedFile, String storageKey) {

        validateTemporaryFileArguments(validatedFile);

        MultipartFile file = validatedFile.file();

        String storedFileName = extractStoredFileName(storageKey);

        Path temporaryBatchDirectory =
                resolveTemporaryBatchDirectory(
                        processingGroupId
                );

        Path temporaryFilePath =
                temporaryBatchDirectory
                        .resolve(storedFileName)
                        .toAbsolutePath()
                        .normalize();

        validatePathInsideDirectory(temporaryBatchDirectory, temporaryFilePath);

        try {
            Files.createDirectories(temporaryBatchDirectory);

            file.transferTo(temporaryFilePath);

            validateStoredFile(validatedFile, temporaryFilePath);

        }  catch (IOException exception) {
            deleteFileQuietly(temporaryFilePath);

            log.error(
                    "임시 문서 파일 저장 실패. processingGroupId={}, storageKey={}",
                    processingGroupId,
                    storageKey,
                    exception
            );

            throw new BusinessException(
                    ErrorCode.DOCUMENT_UPLOAD_FAILED
            );

        } catch (RuntimeException exception) {
            deleteFileQuietly(temporaryFilePath);

            throw exception;
        }
    }

    /**
     * 모든 파일의 임시 저장과 재검증이 성공한 뒤,
     * 처리 그룹의 임시 폴더 전체를 최종 폴더로 이동한다.
     */
    public void commitTemporaryBatch(
            Long processingGroupId,
            String representativeStorageKey
    ) {
        Path temporaryBatchDirectory =
                resolveTemporaryBatchDirectory(
                        processingGroupId
                );

        Path finalFilePath =
                resolveStoragePath(
                        representativeStorageKey
                );

        Path finalBatchDirectory =
                finalFilePath.getParent();

        validateFinalBatchDirectory(
                processingGroupId,
                finalBatchDirectory
        );

        if (!Files.isDirectory(
                temporaryBatchDirectory
        )) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_UPLOAD_FAILED
            );
        }

        Path finalParentDirectory =
                finalBatchDirectory.getParent();

        if (finalParentDirectory == null) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_UPLOAD_FAILED
            );
        }

        try {
            Files.createDirectories(
                    finalParentDirectory
            );

            if (Files.exists(
                    finalBatchDirectory
            )) {
                throw new BusinessException(
                        ErrorCode.DOCUMENT_UPLOAD_FAILED
                );
            }

            Files.move(
                    temporaryBatchDirectory,
                    finalBatchDirectory
            );

        } catch (IOException exception) {
            log.error(
                    "임시 배치 최종 이동 실패. processingGroupId={}, finalDirectory={}",
                    processingGroupId,
                    finalBatchDirectory,
                    exception
            );

            throw new BusinessException(
                    ErrorCode.DOCUMENT_UPLOAD_FAILED
            );
        }
    }

    public void deleteTemporaryBatch(
            Long processingGroupId
    ) {
        Path temporaryBatchDirectory =
                resolveTemporaryBatchDirectory(
                        processingGroupId
                );

        deleteDirectoryRecursively(
                temporaryBatchDirectory
        );
    }

    public void deleteFinalBatch(
            String representativeStorageKey
    ) {
        Path finalFilePath =
                resolveStoragePath(
                        representativeStorageKey
                );

        Path finalBatchDirectory =
                finalFilePath.getParent();

        deleteDirectoryRecursively(
                finalBatchDirectory
        );
    }

    public void deleteStoredFile(String storageKey) {
        Path storedFilePath = resolveStoragePath(storageKey);
        deleteFileOrThrow(storedFilePath);

        Path batchDirectory = storedFilePath.getParent();
        if (batchDirectory == null || batchDirectory.equals(baseUploadDirectory)) {
            return;
        }

        try (var children = Files.list(batchDirectory)) {
            if (children.findAny().isEmpty()) {
                Files.deleteIfExists(batchDirectory);
            }
        } catch (IOException exception) {
            log.warn("빈 업로드 디렉터리 정리에 실패했습니다. path={}", batchDirectory, exception);
        }
    }

    private void validateStoredFile(
            ValidatedDocumentFile validatedFile,
            Path storedFilePath
    ) throws IOException {
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
                    ErrorCode.DOCUMENT_UPLOAD_FAILED
            );
        }

        validateDetectedMimeType(
                storedFilePath,
                validatedFile.originalFileName(),
                validatedFile.fileExtension()
        );
    }

    private void validateDetectedMimeType(Path storedFilePath, String originalFileName, String fileExtension
    ) throws IOException {
        Set<String> allowedMimeTypes =
                DocumentFileTypePolicy
                        .getAllowedMimeTypes(
                                fileExtension
                        );

        if (allowedMimeTypes == null
                || allowedMimeTypes.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_UPLOAD_FAILED
            );
        }

        try (InputStream inputStream =
                     Files.newInputStream(
                             storedFilePath
                     )) {

            String detectedMimeType =
                    tika.detect(
                            inputStream,
                            originalFileName
                    );

            if (!allowedMimeTypes.contains(
                    detectedMimeType
            )) {
                log.warn(
                        "파일 확장자와 실제 MIME 타입 불일치. fileName={}, extension={}, detectedMimeType={}",
                        originalFileName,
                        fileExtension,
                        detectedMimeType
                );

                throw new BusinessException(
                        ErrorCode.INVALID_DOCUMENT_MIME_TYPE
                );
            }
        }
    }

    //==============================================================================================

    private void validateTemporaryFileArguments(ValidatedDocumentFile validatedFile) {

        if (validatedFile == null || validatedFile.file() == null) {
            throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);
        }
    }

    private String extractStoredFileName(String storageKey) {

        if (storageKey == null || storageKey.isBlank()) {
            throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);
        }

        try {
            Path storagePath =
                    Path.of(storageKey).normalize();

            Path storedFileName = storagePath.getFileName();

            if (storedFileName == null || storedFileName.toString().isBlank()) {
                throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);
            }

            return storedFileName.toString();

        } catch (BusinessException exception) {
            throw exception;

        } catch (RuntimeException exception) {
            throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);
        }
    }

    private Path resolveTemporaryBatchDirectory(
            Long processingGroupId
    ) {
        if (processingGroupId == null
                || processingGroupId <= 0) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_UPLOAD_FAILED
            );
        }

        Path temporaryBatchDirectory =
                temporaryUploadDirectory
                        .resolve(
                                processingGroupId.toString()
                        )
                        .toAbsolutePath()
                        .normalize();

        validatePathInsideDirectory(
                temporaryUploadDirectory,
                temporaryBatchDirectory
        );

        return temporaryBatchDirectory;
    }

    private Path resolveStoragePath(String storageKey) {

        if (storageKey == null || storageKey.isBlank()) {
            throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);
        }

        try {
            Path targetPath = baseUploadDirectory.resolve(storageKey)
                                .toAbsolutePath()
                                .normalize();

            validatePathInsideDirectory(baseUploadDirectory, targetPath);

            return targetPath;

        } catch (BusinessException exception) {
            throw exception;

        } catch (RuntimeException exception) {
            throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);
        }
    }

    private void validateFinalBatchDirectory(Long processingGroupId, Path finalBatchDirectory) {

        if (processingGroupId == null || processingGroupId <= 0 || finalBatchDirectory == null) {
            throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);
        }

        Path directoryName = finalBatchDirectory.getFileName();

        if (directoryName == null || !directoryName.toString().equals(processingGroupId.toString())) {
            throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);
        }

        validatePathInsideDirectory(baseUploadDirectory, finalBatchDirectory);
    }

    private void validatePathInsideDirectory(Path parentDirectory, Path targetPath) {

        if (parentDirectory == null || targetPath == null) {
            throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);
        }

        Path normalizedParentDirectory = parentDirectory
                        .toAbsolutePath()
                        .normalize();

        Path normalizedTargetPath = targetPath
                        .toAbsolutePath()
                        .normalize();

        if (!normalizedTargetPath.startsWith(normalizedParentDirectory)) {
            throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);
        }
    }

    private void deleteDirectoryRecursively(Path directory) {

        if (directory == null || !Files.exists(directory)) {
            return;
        }

        try (var paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(this::deleteFileOrThrow);

        } catch (IOException exception) {
            log.error(
                    "업로드 디렉터리 삭제 실패. path={}",
                    directory,
                    exception
            );

            throw new BusinessException(
                    ErrorCode.DOCUMENT_UPLOAD_FAILED
            );
        }
    }

    /**
     * 실패 정리 과정에서 호출자에게 삭제 실패를 전달한다.
     */
    private void deleteFileOrThrow(Path filePath) {
        try {
            Files.deleteIfExists(filePath);

        } catch (IOException exception) {
            log.error(
                    "업로드 파일 삭제 실패. path={}",
                    filePath,
                    exception
            );

            throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);
        }
    }

    /**
     * 개별 파일 저장 실패 후 부분 파일을 정리할 때 사용한다.
     * 원래 저장 예외를 유지하기 위해 삭제 실패는 로그만 남긴다.
     */
    private void deleteFileQuietly(Path filePath) {

        if (filePath == null) return;

        try {
            Files.deleteIfExists(filePath);

        } catch (IOException exception) {
            log.error(
                    "업로드 파일 삭제 실패. path={}",
                    filePath,
                    exception
            );
        }
    }
}
