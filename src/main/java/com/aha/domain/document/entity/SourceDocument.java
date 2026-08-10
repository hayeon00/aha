package com.aha.domain.document.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

@Getter
@Entity
@Table(
        name = "source_document",
        indexes = {
                @Index(
                        name = "idx_source_document_is_active",
                        columnList = "is_active"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_source_document_storage_key",
                        columnNames = "storage_key"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class SourceDocument {

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("pdf", "docx");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "stored_file_name", nullable = false, length = 255)
    private String storedFileName;

    @Column(name = "storage_key", nullable = false, length = 500)
    private String storageKey;

    @Column(name = "file_extension", nullable = false, length = 20)
    private String fileExtension;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    public static SourceDocument create(
            String originalFileName,
            String storedFileName,
            String storageKey,
            String fileExtension,
            String mimeType,
            long fileSize
    ) {
        String normalizedOriginalFileName =
                normalizeRequiredText(originalFileName, "원본 파일명");

        String normalizedStoredFileName =
                normalizeRequiredText(storedFileName, "저장 파일명");

        String normalizedStorageKey =
                normalizeRequiredText(storageKey, "파일 저장 경로");

        String normalizedExtension =
                normalizeExtension(fileExtension);

        String normalizedMimeType =
                normalizeRequiredText(mimeType, "MIME 타입");

        validateFileSize(fileSize);

        return SourceDocument.builder()
                .originalFileName(normalizedOriginalFileName)
                .storedFileName(normalizedStoredFileName)
                .storageKey(normalizedStorageKey)
                .fileExtension(normalizedExtension)
                .mimeType(normalizedMimeType)
                .fileSize(fileSize)
                .isActive(true)
                .build();
    }

    private static String normalizeRequiredText(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + "은(는) 필수입니다."
            );
        }

        return value.trim();
    }

    private static String normalizeExtension(String fileExtension) {
        String normalizedExtension =
                normalizeRequiredText(fileExtension, "파일 확장자")
                        .toLowerCase(Locale.ROOT);

        if (!ALLOWED_EXTENSIONS.contains(normalizedExtension)) {
            throw new IllegalArgumentException(
                    "지원하지 않는 파일 확장자입니다: "
                            + normalizedExtension
            );
        }

        return normalizedExtension;
    }

    private static void validateFileSize(long fileSize) {
        if (fileSize <= 0) {
            throw new IllegalArgumentException(
                    "파일 크기는 0보다 커야 합니다."
            );
        }
    }
}