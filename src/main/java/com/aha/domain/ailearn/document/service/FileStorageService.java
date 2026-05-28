package com.aha.domain.ailearn.document.service;

import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadPath = Paths.get("uploads", "learning-documents")
            .toAbsolutePath()
            .normalize();

    public StoredFileInfo store(MultipartFile file) {
        validateFile(file);

        String originalFileName = file.getOriginalFilename();
        String extension = getExtension(originalFileName);
        validateSupportedExtension(extension);

        try {
            Files.createDirectories(uploadPath);

            String storedFileName = UUID.randomUUID() + buildExtensionSuffix(extension);
            Path targetPath = uploadPath.resolve(storedFileName).normalize();

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return new StoredFileInfo(
                    originalFileName,
                    storedFileName,
                    targetPath.toString(),
                    extension,
                    file.getContentType(),
                    file.getSize()
            );
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_SAVE_FAILED);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }
    }

    private void validateSupportedExtension(String extension) {
        if (!"pdf".equals(extension)) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_DOCUMENT_TYPE);
        }
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }

        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private String buildExtensionSuffix(String extension) {
        if (extension == null || extension.isBlank()) {
            return "";
        }

        return "." + extension;
    }

    public record StoredFileInfo(
            String originalFileName,
            String storedFileName,
            String filePath,
            String fileExtension,
            String mimeType,
            Long fileSize
    ) {
    }
}