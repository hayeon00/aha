package com.aha.domain.document.entity;

import com.aha.domain.document.enums.DocumentFileExtension;
import com.aha.domain.user.entity.User;
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
                        name = "idx_source_document_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_source_document_user_active",
                        columnList = "user_id, is_active"
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_source_document_user"
            )
    )
    private User user;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "stored_file_name", nullable = false, length = 255)
    private String storedFileName;

    @Column(name = "storage_key", nullable = false, length = 500)
    private String storageKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_extension", nullable = false, length = 20)
    private DocumentFileExtension fileExtension;

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
            User user,
            String originalFileName,
            String storedFileName,
            String storageKey,
            DocumentFileExtension fileExtension,
            String mimeType,
            long fileSize
    ) {

        validateUser(user);
        validateFileExtension(fileExtension);
        validateFileSize(fileSize);

        return SourceDocument.builder()
                .user(user)
                .originalFileName(
                        normalizeRequiredText(
                                originalFileName,
                                "원본 파일명"
                        )
                )
                .storedFileName(
                        normalizeRequiredText(
                                storedFileName,
                                "저장 파일명"
                        )
                )
                .storageKey(
                        normalizeRequiredText(
                                storageKey,
                                "파일 저장 경로"
                        )
                )
                .fileExtension(fileExtension)
                .mimeType(
                        normalizeRequiredText(
                                mimeType,
                                "MIME 타입"
                        )
                )
                .fileSize(fileSize)
                .isActive(true)
                .build();
    }

    public void deactivate() {
        this.isActive = false;
    }

    private static void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException(
                    "문서 소유자는 필수입니다."
            );
        }
    }

    private static void validateFileExtension(
            DocumentFileExtension fileExtension
    ) {
        if (fileExtension == null) {
            throw new IllegalArgumentException(
                    "파일 확장자는 필수입니다."
            );
        }
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

    private static void validateFileSize(long fileSize) {
        if (fileSize <= 0) {
            throw new IllegalArgumentException(
                    "파일 크기는 0보다 커야 합니다."
            );
        }
    }
}