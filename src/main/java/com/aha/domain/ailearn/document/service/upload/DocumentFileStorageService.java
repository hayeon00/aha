package com.aha.domain.ailearn.document.service.upload;

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

// 저장된 storageKey를 물리 저장소에 저장


@Slf4j
@Service
public class DocumentFileStorageService {

    private static final long MAX_FILE_SIZE_BYTES = 20L * 1024 * 1024;
    private static final long MAX_TOTAL_FILE_SIZE_BYTES = 100L * 1024 * 1024;
    private static final int MAX_FILE_COUNT = 5;
    private static final int MAX_FILE_NAME_LENGTH = 255;

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

    public DocumentFileStorageService(@Value("${file.document-upload-dir:uploads}") String documentUploadDir) {

        this.tika = new Tika();

        this.baseUploadDirectory = Paths.get(documentUploadDir)
                                        .toAbsolutePath()
                                        .normalize();
    }

    public List<MultipartFile> validateFiles(List<MultipartFile> files) {

        validateFileCount(files);

        long totalFileSize = 0L;

        for (MultipartFile file : files) {
            validateFile(file);
            totalFileSize += file.getSize();
        }

        if (totalFileSize > MAX_TOTAL_FILE_SIZE_BYTES) {
            throw invalidDocumentFile();
        }

        return List.copyOf(files);
    }

    public void storeFile(MultipartFile file, String storageKey) {

        validateFile(file);

        Path targetPath = resolveStoragePath(storageKey);

        try{
            Files.createDirectories(targetPath.getParent());

            file.transferTo(targetPath);

            validateStoredFile(
                    file,
                    targetPath
            );

        }catch (BusinessException exception){
            deleteFile(targetPath);
            throw exception;

        }catch (IOException exception){
            deleteFile(targetPath);

            log.error(
                    "문서 파일 저장 실패. storageKey={}",
                    storageKey,
                    exception
            );

            throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);
        }catch (RuntimeException exception){
            deleteFile(targetPath);
            throw exception;
        }
    }

    private void validateStoredFile(MultipartFile file, Path targetPath) throws IOException{

        String originalFilename = resolveOriginalFileName(file);

        String extension = extractExtension(originalFilename);

        long storedFileSize = Files.size(targetPath);

        if(storedFileSize <= 0 || storedFileSize > MAX_FILE_SIZE_BYTES){
            throw invalidDocumentFile();
        }

        detectMimeType(
                targetPath,
                originalFilename,
                extension
        );


    }




    // ==================== File Validation ====================

    private void validateFileCount(List<MultipartFile> files) {

        if (files == null || files.isEmpty() || files.size() > MAX_FILE_COUNT) {
            throw invalidDocumentFile();
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null
                || file.isEmpty()
                || file.getSize() <= 0
                || file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw invalidDocumentFile();
        }

        String originalFileName = resolveOriginalFileName(file);
        extractExtension(originalFileName);
    }

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
                || normalizedFileName.length() > MAX_FILE_NAME_LENGTH) {
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

        if (extensionIndex <= 0
                || extensionIndex == originalFileName.length() - 1) {
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

    private String detectMimeType(Path targetPath, String originalFileName, String extension) throws IOException {

        Set<String> allowedMimeTypes = ALLOWED_MIME_TYPES.get(extension);

        if(allowedMimeTypes == null) {
            throw invalidDocumentFile();
        }

        try(InputStream inputStream = Files.newInputStream(targetPath)) {
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

            return detectedMimeType;

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

    private void deleteDirectoryIfEmpty(Path directory) {
        if (directory == null || !Files.isDirectory(directory)) {
            return;
        }

        try (var paths = Files.list(directory)) {
            if (paths.findAny().isEmpty()) {
                Files.deleteIfExists(directory);
            }
        } catch (IOException exception) {
            log.warn(
                    "빈 업로드 디렉터리 삭제 실패. path={}",
                    directory,
                    exception
            );
        }
    }

    private BusinessException invalidDocumentFile() {
        return new BusinessException(ErrorCode.INVALID_DOCUMENT_FILE);
    }


}
