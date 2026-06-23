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
@Table(
        name = "document_processing_group",
        indexes = {
                @Index(
                        name = "idx_dpg_user_exam_id",
                        columnList = "user_exam_id"
                ),
                @Index(
                        name = "idx_dpg_status",
                        columnList = "status"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentProcessingGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_exam_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_dpg_user_exam_id"
            )
    )
    private UserExam userExam;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DocumentProcessingStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", nullable = false, length = 50)
    private DocumentProcessingStep currentStep;

    @Column(name = "progress_rate", nullable = false)
    private int progressRate;

    @Column(name = "total_file_count", nullable = false)
    private int totalFileCount;

    @Column(name = "completed_file_count", nullable = false)
    private int completedFileCount;

    @Column(name = "failed_file_count", nullable = false)
    private int failedFileCount;

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


    public static DocumentProcessingGroup createPending(UserExam userExam, int totalFileCount) {

        DocumentProcessingGroup processingGroup = new DocumentProcessingGroup();

        processingGroup.userExam = userExam;
        processingGroup.totalFileCount = totalFileCount;
        processingGroup.status = DocumentProcessingStatus.PENDING;

        return processingGroup;

    }




















    public void startProcessing() {
        validateNotFinished();

        this.status = DocumentProcessingStatus.PROCESSING;
        this.currentStep = DocumentProcessingStep.TEXT_EXTRACTING;
        this.progressRate =
                DocumentProcessingStep.TEXT_EXTRACTING.getProgressRate();

        if (this.startedAt == null) {
            this.startedAt = LocalDateTime.now();
        }
    }

    public void updateStep(DocumentProcessingStep nextStep) {
        validateNotFinished();

        if (nextStep == null) {
            throw new IllegalArgumentException(
                    "문서 처리 단계는 필수입니다."
            );
        }

        this.currentStep = nextStep;
        this.progressRate = nextStep.getProgressRate();
    }

    public void increaseCompletedFileCount() {
        validateNotFinished();
        validateFileCountCanIncrease();

        this.completedFileCount++;
    }

    public void increaseFailedFileCount() {
        validateNotFinished();
        validateFileCountCanIncrease();

        this.failedFileCount++;
    }

    public void finish(
            int completedFileCount,
            int failedFileCount
    ) {
        validateResultCounts(
                completedFileCount,
                failedFileCount
        );

        this.completedFileCount = completedFileCount;
        this.failedFileCount = failedFileCount;
        this.completedAt = LocalDateTime.now();
        this.currentStep = DocumentProcessingStep.COMPLETED;
        this.progressRate =
                DocumentProcessingStep.COMPLETED.getProgressRate();

        if (completedFileCount == totalFileCount) {
            this.status =
                    DocumentProcessingStatus.COMPLETED;
            this.errorMessage = null;
            return;
        }

        if (failedFileCount == totalFileCount) {
            this.status =
                    DocumentProcessingStatus.FAILED;
            return;
        }

        if (completedFileCount + failedFileCount
                == totalFileCount) {
            this.status =
                    DocumentProcessingStatus.PARTIALLY_COMPLETED;
            return;
        }

        throw new IllegalStateException(
                "모든 문서 처리가 종료되지 않았습니다."
        );
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

    private void validateFileCountCanIncrease() {
        int processedFileCount =
                completedFileCount + failedFileCount;

        if (processedFileCount >= totalFileCount) {
            throw new IllegalStateException(
                    "전체 파일 개수를 초과할 수 없습니다."
            );
        }
    }

    private void validateNotFinished() {
        if (status == DocumentProcessingStatus.COMPLETED
                || status == DocumentProcessingStatus.PARTIALLY_COMPLETED
                || status == DocumentProcessingStatus.FAILED) {

            throw new IllegalStateException(
                    "이미 종료된 문서 처리 작업입니다."
            );
        }
    }

    private void validateResultCounts(
            int completedFileCount,
            int failedFileCount
    ) {
        if (completedFileCount < 0
                || failedFileCount < 0) {

            throw new IllegalArgumentException(
                    "문서 처리 개수는 0 이상이어야 합니다."
            );
        }

        if (completedFileCount + failedFileCount
                > totalFileCount) {

            throw new IllegalArgumentException(
                    "완료 및 실패 파일 수가 전체 파일 수를 초과할 수 없습니다."
            );
        }
    }
}