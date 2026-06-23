package com.aha.domain.ailearn.document.entity;

import com.aha.domain.ailearn.document.enums.DocumentProcessingStatus;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "document_processing",
        indexes = {
                @Index(
                        name = "idx_dp_processing_group_id",
                        columnList = "processing_group_id"
                ),
                @Index(
                        name = "idx_dp_status",
                        columnList = "status"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentProcessing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "processing_group_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_dp_processing_group_id"
            )
    )
    private DocumentProcessingGroup processingGroup;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "source_document_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(
                    name = "fk_dp_source_document_id"
            )
    )
    private SourceDocument sourceDocument;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DocumentProcessingStatus status;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private DocumentProcessing(
            DocumentProcessingGroup processingGroup,
            SourceDocument sourceDocument
    ) {
        if (processingGroup == null) {
            throw new IllegalArgumentException(
                    "문서 처리 그룹은 필수입니다."
            );
        }

        if (sourceDocument == null) {
            throw new IllegalArgumentException(
                    "원본 문서는 필수입니다."
            );
        }

        this.processingGroup = processingGroup;
        this.sourceDocument = sourceDocument;
        this.status = DocumentProcessingStatus.PENDING;
    }

    public void start() {
        if (this.status != DocumentProcessingStatus.PENDING) {
            throw new BusinessException(
                    ErrorCode.INVALID_DOCUMENT_PROCESSING_STATUS
            );
        }

        this.status = DocumentProcessingStatus.PROCESSING;
        this.startedAt = LocalDateTime.now();
        this.errorMessage = null;
    }

    public void complete() {
        if (status != DocumentProcessingStatus.PROCESSING) {
            throw new IllegalStateException(
                    "처리 중인 문서만 완료할 수 있습니다."
            );
        }

        this.status = DocumentProcessingStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.errorMessage = null;
    }

    public void fail(String errorMessage) {
        if (errorMessage == null || errorMessage.isBlank()) {
            throw new IllegalArgumentException(
                    "실패 메시지는 필수입니다."
            );
        }

        this.status = DocumentProcessingStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }
}