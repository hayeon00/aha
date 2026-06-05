package com.aha.domain.ailearn.document.entity;

import com.aha.domain.ailearn.document.enums.DocumentProcessingStatus;
import com.aha.domain.ailearn.document.enums.DocumentProcessingStep;
import com.aha.domain.user.entity.UserExam;
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
@Table(name = "document_processing_group")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentProcessingGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_exam_id", nullable = false)
    private UserExam userExam;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DocumentProcessingStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", nullable = false, length = 50)
    private DocumentProcessingStep currentStep;

    @Column(name = "progress_rate", nullable = false)
    private Integer progressRate;

    @Column(name = "total_file_count", nullable = false)
    private Integer totalFileCount;

    @Column(name = "completed_file_count", nullable = false)
    private Integer completedFileCount;

    @Column(name = "failed_file_count", nullable = false)
    private Integer failedFileCount;

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
    private DocumentProcessingGroup(
            UserExam userExam,
            DocumentProcessingStatus status,
            DocumentProcessingStep currentStep,
            Integer progressRate,
            Integer totalFileCount,
            Integer completedFileCount,
            Integer failedFileCount,
            String errorMessage,
            LocalDateTime startedAt,
            LocalDateTime completedAt
    ) {
        this.userExam = userExam;
        this.status = status != null ? status : DocumentProcessingStatus.PENDING;
        this.currentStep = currentStep != null ? currentStep : DocumentProcessingStep.FILE_UPLOADED;
        this.progressRate = progressRate != null ? progressRate : 0;
        this.totalFileCount = totalFileCount != null ? totalFileCount : 0;
        this.completedFileCount = completedFileCount != null ? completedFileCount : 0;
        this.failedFileCount = failedFileCount != null ? failedFileCount : 0;
        this.errorMessage = errorMessage;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    public void startProcessing() {
        this.status = DocumentProcessingStatus.PROCESSING;
        this.currentStep = DocumentProcessingStep.TEXT_EXTRACTING;
        this.progressRate = DocumentProcessingStep.TEXT_EXTRACTING.getProgressRate();
        this.startedAt = LocalDateTime.now();
    }

    public void updateStep(DocumentProcessingStep currentStep) {
        this.currentStep = currentStep;
        this.progressRate = currentStep.getProgressRate();
    }

    public void increaseCompletedFileCount() {
        this.completedFileCount++;
    }

    public void increaseFailedFileCount() {
        this.failedFileCount++;
    }

    public void complete() {
        this.status = DocumentProcessingStatus.COMPLETED;
        this.currentStep = DocumentProcessingStep.COMPLETED;
        this.progressRate = 100;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        this.status = DocumentProcessingStatus.FAILED;
        this.currentStep = DocumentProcessingStep.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }

    public void partialFail(String errorMessage) {
        this.status = DocumentProcessingStatus.PARTIAL_FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }
}
