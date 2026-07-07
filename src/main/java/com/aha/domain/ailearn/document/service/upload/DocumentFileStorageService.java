package com.aha.domain.ailearn.document.service.upload;

import com.aha.domain.ailearn.document.config.DocumentUploadProperties;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


@Slf4j
@Service
public class DocumentFileStorageService {

    private final long maxFileSizeBytes;
    private final long maxTotalFileSizeBytes;
    private final int maxFileCount;
    private final int maxFileNameLength;

    private static final Map<String, Set<String>> ALLOWED_MIME_TYPES =
            Map.of(
                "pdf", Set.of("application/pdf"),
                "docx", Set.of(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                ),
                "txt", Set.of("text/plain")
            );

    private final Tika tika;
    private final Path baseUploadDirectory;
    private final Path temporaryUploadDirectory;

    public DocumentFileStorageService(@Value("${file.document-upload-dir:uploads}") String documentUploadDir, DocumentUploadProperties documentUploadProperties) {

        this.tika = new Tika();
        this.baseUploadDirectory = Paths.get(documentUploadDir)
                .toAbsolutePath()
                .normalize();

        this.temporaryUploadDirectory = baseUploadDirectory
                .resolve("temp")
                .normalize();

        this.maxFileSizeBytes = documentUploadProperties.getMaxFileSizeBytes();
        this.maxTotalFileSizeBytes = documentUploadProperties.getMaxTotalSizeBytes();
        this.maxFileCount = documentUploadProperties.getMaxFileCount();
        this.maxFileNameLength = documentUploadProperties.getMaxFileNameLength();
    }

    public List<MultipartFile> validateFiles(List<MultipartFile> files) {

        validateFileCount(files);

        long totalFileSize = 0L;

        for (MultipartFile file : files) {
            validateFile(file);
            totalFileSize += file.getSize();
        }

        if (totalFileSize > maxTotalFileSizeBytes) {
            throw invalidDocumentFile();
        }

        return List.copyOf(files);
    }

    public void storeTemporaryFile(Long processingGroupId, MultipartFile file, String storageKey){

        validateFile(file);

        String storedFileName = extractStoredFileName(storageKey);

        Path temporaryBatchDirectory = resolveTemporaryBatchDirectory(processingGroupId);

        Path temporaryFilePath = temporaryBatchDirectory
                .resolve(storedFileName)
                .toAbsolutePath()
                .normalize();

        validatePathInsideDirectory(
                temporaryBatchDirectory,
                temporaryFilePath
        );

        try{
            Files.createDirectories(temporaryBatchDirectory);

            file.transferTo(temporaryFilePath);

            validateStoredFile(
                    file,
                    temporaryFilePath
            );

        }catch (BusinessException exception){
            deleteFile(temporaryFilePath);
            throw exception;

        }catch (IOException exception){
            deleteFile(temporaryFilePath);

            log.error(
                    "임시 문서 파일 저장 실패. processingGroupId={}, storageKey={}",
                    processingGroupId,
                    storageKey,
                    exception
            );

            throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);

        }catch (RuntimeException exception){
            deleteFile(temporaryFilePath);
            throw exception;
        }

    }


    // ============== 내부 Method ===================

    // 실제 파일명만 꺼냄
    private String extractStoredFileName(String storageKey) {

        if(storageKey == null || storageKey.isBlank()){
            throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);
        }

        Path storagePath = Path.of(storageKey).normalize();

        Path storedFileName = storagePath.getFileName();

        if(storedFileName == null || storedFileName.toString().isBlank()){
            throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);
        }

        String fileName = storedFileName.toString();
        extractExtension(fileName);

        return fileName;

    }


    // 임시 폴더 경로생성
    private Path resolveTemporaryBatchDirectory(Long processingGroupId) {

        if(processingGroupId == null) {
            throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);
        }

        Path temporaryBatchDirectory = temporaryUploadDirectory
                                        .resolve(String.valueOf(processingGroupId))
                                        .toAbsolutePath()
                                        .normalize();

        if (!temporaryBatchDirectory.startsWith(temporaryUploadDirectory)) {
            throw invalidDocumentFile();
        }

        return temporaryBatchDirectory;
    }

    // 전체 경로가 내부에 있는지 검사
    private void validatePathInsideDirectory(Path parentDirectory, Path targetPath) {

        if(parentDirectory == null || targetPath == null){
            throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);
        }

        Path normalizedParentDirectory = parentDirectory
                                        .toAbsolutePath()
                                        .normalize();

        Path normalizedTargetPath = targetPath
                                        .toAbsolutePath()
                                        .normalize();

        if(!normalizedTargetPath.startsWith(normalizedParentDirectory)){
            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_FILE);
        }
    }


    private void validateStoredFile(MultipartFile file, Path storedFilePath) throws IOException{

        String originalFilename = resolveOriginalFileName(file);

        String extension = extractExtension(originalFilename);

        long storedFileSize = Files.size(storedFilePath);

        if(storedFileSize <= 0 || storedFileSize > maxFileSizeBytes){
            throw invalidDocumentFile();
        }

        if(storedFileSize != file.getSize()){
            throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);
        }

        validateMimeType(
                storedFilePath,
                originalFilename,
                extension
        );


    }


    // ==================== File Validation ====================

    private void validateFileCount(List<MultipartFile> files) {

        if (files == null || files.isEmpty() || files.size() > maxFileCount) {
            throw invalidDocumentFile();
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null
                || file.isEmpty()
                || file.getSize() > maxFileSizeBytes) {
            throw invalidDocumentFile();
        }

        String originalFileName = resolveOriginalFileName(file);
        extractExtension(originalFileName);
    }

    // 원본 파일명 검증 및 정규화 C:\fakepath\sqld.pdf-> sqld.pdf
    private String resolveOriginalFileName(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {
            throw invalidDocumentFile();
        }

        String normalizedPath = originalFileName
                .replace("\\", "/")
                .trim();

        int separatorIndex = normalizedPath.lastIndexOf('/');
        String normalizedFileName = normalizedPath
                .substring(separatorIndex + 1)
                .trim();

        if (normalizedFileName.isBlank()
                || normalizedFileName.length() > maxFileNameLength) {
            throw invalidDocumentFile();
        }

        return normalizedFileName;
    }

    // storageKey를 실제 저장 경로로 변환
    private Path resolveStoragePath(String storageKey) {

        if(storageKey == null || storageKey.isBlank()) {
            throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);
        }

        Path targetPath = baseUploadDirectory.resolve(storageKey).toAbsolutePath().normalize();

        if(!targetPath.startsWith(baseUploadDirectory)) {
            throw invalidDocumentFile();
        }

        return targetPath;
    }



    private String extractExtension(String originalFileName) {
        int extensionIndex = originalFileName.lastIndexOf('.');

        if (extensionIndex <= 0 || extensionIndex == originalFileName.length() - 1) {
            throw invalidDocumentFile();
        }

        String extension = originalFileName
                .substring(extensionIndex + 1)
                .toLowerCase(Locale.ROOT);

        if (!ALLOWED_MIME_TYPES.containsKey(extension)) {
            throw invalidDocumentFile();
        }

        return extension;
    }

    private void validateMimeType(Path storedFilePath, String originalFileName, String extension) throws IOException {

        Set<String> allowedMimeTypes = ALLOWED_MIME_TYPES.get(extension);

        if(allowedMimeTypes == null) {
            throw invalidDocumentFile();
        }

        try(InputStream inputStream = Files.newInputStream(storedFilePath)) {
            String detectedMimeType = tika.detect(inputStream, originalFileName);

            if(!allowedMimeTypes.contains(detectedMimeType)) {
                log.warn(
                        "파일 확장자와 MIME 타입 불일치. fileName={}, extension={}, detectedMimeType={}",
                        originalFileName,
                        extension,
                        detectedMimeType
                );

                throw invalidDocumentFile();

            }
        }
    }



    // ==================== Cleanup ====================

    private void deleteFile(Path filePath) {
        if (filePath == null) {
            return;
        }

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


    private BusinessException invalidDocumentFile() {

        return new BusinessException(ErrorCode.INVALID_DOCUMENT_FILE);
    }


    // 임시 폴더에 모두 저장된 파일 묶음을 최종 저장 경로로 이동
    public void commitTemporaryBatch(Long processingGroupId, String firstStorageKey) {

        Path temporaryBatchDirectory = resolveTemporaryBatchDirectory(processingGroupId);

        Path finalFilePath = resolveStoragePath(firstStorageKey);

        Path finalBatchDirectory = finalFilePath.getParent();

        if(finalBatchDirectory == null || finalBatchDirectory.getParent() == null) {
            throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);
        }

        if(!Files.isDirectory(temporaryBatchDirectory)) {
            throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);
        }

        Path finalParentDirectory = finalBatchDirectory.getParent();

        try{
            Files.createDirectories(finalParentDirectory);

            if(Files.exists(finalBatchDirectory)) {
                throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);
            }

            Files.move(
                    temporaryBatchDirectory,
                    finalBatchDirectory
            );

        }catch (IOException exception){
            log.error(
                    "임시 배치 최종 이동 실패. processingGroupId={}, finalDirectory={}",
                    processingGroupId,
                    finalBatchDirectory,
                    exception
            );

            throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);
        }

    }

    public void deleteTemporaryBatch(Long processingGroupId) {
        Path temporaryBatchDirectory = resolveTemporaryBatchDirectory(processingGroupId);

        deleteDirectoryRecursively(temporaryBatchDirectory);
    }

    private void deleteDirectoryRecursively(Path directory) {

        if (directory == null || !Files.exists(directory)) {
            return;
        }

        try (var paths = Files.walk(directory)) {
            paths.sorted(
                            (first, second) ->
                                    second.getNameCount()
                                            - first.getNameCount()
                    )
                    .forEach(this::deleteFile);

        } catch (IOException exception) {
            log.error(
                    "업로드 디렉터리 삭제 실패. path={}",
                    directory,
                    exception
            );
        }
    }

    public void deleteFinalBatch(String firstStorageKey) {

        Path finalFilePath = resolveStoragePath(firstStorageKey);

        Path finalBatchDirectory = finalFilePath.getParent();

        deleteDirectoryRecursively(finalBatchDirectory);
    }

}
