package com.aha.domain.ailearn.document.service;

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

        try {
            Files.createDirectories(uploadPath);

            String originalFileName = file.getOriginalFilename();
            String extension = getExtension(originalFileName);
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
            throw new IllegalStateException("파일 저장 중 오류가 발생했습니다.", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
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