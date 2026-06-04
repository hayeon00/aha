package com.aha.domain.ailearn.document.entity;

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
@Table(name = "document_processing")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentProcessing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_document_id", nullable = false)
    private SourceDocument sourceDocument;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "current_step", nullable = false, length = 50)
    private String currentStep;

    @Column(name = "progress_rate", nullable = false)
    private Integer progressRate;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private DocumentProcessing(
            SourceDocument sourceDocument,
            String status,
            String currentStep,
            Integer progressRate,
            String errorMessage,
            LocalDateTime startedAt,
            LocalDateTime completedAt
    ) {
        this.sourceDocument = sourceDocument;
        this.status = status != null ? status : "PENDING";
        this.currentStep = currentStep != null ? currentStep : "FILE_UPLOADED";
        this.progressRate = progressRate != null ? progressRate : 0;
        this.errorMessage = errorMessage;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }
}