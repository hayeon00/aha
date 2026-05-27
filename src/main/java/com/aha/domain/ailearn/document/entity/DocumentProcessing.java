package com.aha.domain.ailearn.document.entity;

import com.aha.domain.ailearn.document.type.ProcessingStatus;
import com.aha.domain.ailearn.document.type.ProcessingType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_processing")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DocumentProcessing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_document_id", nullable = false)
    private Long sourceDocumentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_type", nullable = false, length = 50)
    private ProcessingType processingType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProcessingStatus status;

    @Column(name = "requested_by")
    private Long requestedBy;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (processingType == null) {
            processingType = ProcessingType.TEXT_EXTRACTION;
        }

        if (status == null) {
            status = ProcessingStatus.PENDING;
        }

        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void start() {
        this.status = ProcessingStatus.PROCESSING;
        this.startedAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = ProcessingStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        this.status = ProcessingStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }
}