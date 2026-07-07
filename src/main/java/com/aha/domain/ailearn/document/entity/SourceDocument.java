package com.aha.domain.ailearn.document.entity;

import com.aha.domain.ailearn.document.enums.DocumentUploadStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "source_document",
        indexes = {
                @Index(
                        name = "idx_source_document_processing_group_id",
                        columnList = "processing_group_id"
                ),
                @Index(
                        name = "idx_source_document_upload_status",
                        columnList = "upload_status"
                ),
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
public class SourceDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "processing_group_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_source_document_processing_group_id"
            )
    )
    private DocumentProcessingGroup processingGroup;

    @Column(
            name = "original_file_name",
            nullable = false,
            length = 255
    )
    private String originalFileName;


    @Column(
            name = "stored_file_name",
            nullable = false,
            length = 255
    )
    private String storedFileName;

    @Column(
            name = "storage_key",
            nullable = false,
            length = 500
    )
    private String storageKey;

    @Column(
            name = "file_extension",
            nullable = false,
            length = 20
    )
    private String fileExtension;

    @Column(
            name = "mime_type",
            nullable = false,
            length = 100
    )
    private String mimeType;

    @Column(
            name = "file_size",
            nullable = false
    )
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "upload_status",
            nullable = false,
            length = 30
    )
    private DocumentUploadStatus uploadStatus;

    @Column(
            name = "upload_error_message",
            length = 1000
    )
    private String uploadErrorMessage;

    @Column(
            name = "is_active",
            nullable = false
    )
    private boolean active;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    private SourceDocument(
            DocumentProcessingGroup processingGroup,
            String originalFileName,
            String storedFileName,
            String storageKey,
            String fileExtension,
            String mimeType,
            Long fileSize
    ) {
        validateRequiredFields(
                processingGroup,
                originalFileName,
                storedFileName,
                storageKey,
                fileExtension,
                mimeType,
                fileSize
        );

        this.processingGroup = processingGroup;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.storageKey = storageKey;
        this.fileExtension = fileExtension;
        this.mimeType = mimeType;
        this.fileSize = fileSize;

        this.uploadStatus = DocumentUploadStatus.PENDING;
        this.uploadErrorMessage = null;
        this.active = true;
    }


    public static SourceDocument createPending(
            DocumentProcessingGroup processingGroup,
            String originalFileName,
            String storedFileName,
            String storageKey,
            String fileExtension,
            String mimeType,
            Long fileSize
    ) {
        return new SourceDocument(
                processingGroup,
                originalFileName,
                storedFileName,
                storageKey,
                fileExtension,
                mimeType,
                fileSize
        );
    }

    private void validateRequiredFields(
            DocumentProcessingGroup processingGroup,
            String originalFileName,
            String storedFileName,
            String storageKey,
            String fileExtension,
            String mimeType,
            Long fileSize
    ) {
        if (processingGroup == null) {
            throw new IllegalArgumentException(
                    "문서 처리 그룹은 필수입니다."
            );
        }

        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException(
                    "원본 파일명은 필수입니다."
            );
        }

        if (storedFileName == null || storedFileName.isBlank()) {
            throw new IllegalArgumentException(
                    "저장 파일명은 필수입니다."
            );
        }

        if (storageKey == null || storageKey.isBlank()) {
            throw new IllegalArgumentException(
                    "파일 저장 경로는 필수입니다."
            );
        }

        if (fileExtension == null || fileExtension.isBlank()) {
            throw new IllegalArgumentException(
                    "파일 확장자는 필수입니다."
            );
        }

        if (mimeType == null || mimeType.isBlank()) {
            throw new IllegalArgumentException(
                    "MIME 타입은 필수입니다."
            );
        }

        if (fileSize == null || fileSize <= 0) {
            throw new IllegalArgumentException(
                    "파일 크기는 0보다 커야 합니다."
            );
        }
    }

    public void markStored() {
        if(this.uploadStatus != DocumentUploadStatus.PENDING){
            throw new IllegalStateException("PENDING 상태의 문서만 저장 완료 처리할 수 있습니다.");
        }

        this.uploadStatus = DocumentUploadStatus.STORED;

        this.uploadErrorMessage = null;
    }

    public void markUploadFailed(String resolvedErrorMessage) {

        this.uploadStatus = DocumentUploadStatus.FAILED;

        this.uploadErrorMessage = resolvedErrorMessage;
    }
}