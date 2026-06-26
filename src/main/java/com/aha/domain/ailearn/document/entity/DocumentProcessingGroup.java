package com.aha.domain.ailearn.document.entity;

import com.aha.domain.ailearn.document.enums.DocumentProcessingStatus;
import com.aha.domain.ailearn.document.enums.DocumentProcessingStep;
import com.aha.domain.user.entity.UserExam;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
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
        processingGroup.status = DocumentProcessingStatus.PENDING;
        processingGroup.currentStep = DocumentProcessingStep.UPLOAD_PENDING;
        processingGroup.progressRate = DocumentProcessingStep.UPLOAD_PENDING.getProgressRate();

        processingGroup.totalFileCount = totalFileCount;
        processingGroup.completedFileCount = 0;
        processingGroup.failedFileCount = 0;

        return processingGroup;

    }

    public void startProcessing() {
        validateNotFinished();

        if(this.status != DocumentProcessingStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_PROCESSING_STATUS);
        }

        this.status = DocumentProcessingStatus.PROCESSING;
        this.currentStep = DocumentProcessingStep.TEXT_EXTRACTING;
        this.progressRate = DocumentProcessingStep.TEXT_EXTRACTING.getProgressRate();

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

        if(this.status != DocumentProcessingStatus.PROCESSING){
            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_PROCESSING_STATUS);
        }

        if(nextStep == DocumentProcessingStep.UPLOAD_PENDING || nextStep == DocumentProcessingStep.FILE_UPLOADED
                || nextStep == DocumentProcessingStep.COMPLETED){

            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_PROCESSING_STATUS);
        }

        if(nextStep.getProgressRate() <= this.currentStep.getProgressRate()){
            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_PROCESSING_STATUS);
        }

        this.currentStep = nextStep;
        this.progressRate = nextStep.getProgressRate();
    }

    public void markFilesUploaded() {
        validateNotFinished();

        if (this.status != DocumentProcessingStatus.PENDING) {
            throw new IllegalStateException(
                    "처리 대기 상태에서만 파일 업로드를 완료할 수 있습니다."
            );
        }

        if (this.currentStep
                != DocumentProcessingStep.UPLOAD_PENDING) {
            throw new IllegalStateException(
                    "업로드 대기 단계에서만 파일 업로드를 완료할 수 있습니다."
            );
        }

        this.currentStep = DocumentProcessingStep.FILE_UPLOADED;
        this.progressRate = DocumentProcessingStep.FILE_UPLOADED.getProgressRate();
    }

    public void fail(String errorMessage) {

        validateNotFinished();

        if (errorMessage == null || errorMessage.isBlank()) {
            throw new IllegalArgumentException(
                    "실패 메시지는 필수입니다."
            );
        }

        this.status = DocumentProcessingStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }

    public void complete() {

        validateNotFinished();

        if(this.status != DocumentProcessingStatus.PROCESSING){
            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_PROCESSING_STATUS);
        }

        if(this.currentStep != DocumentProcessingStep.LEARNING_CONTENT_GENERATING){
            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_PROCESSING_STATUS);
        }

        this.status = DocumentProcessingStatus.COMPLETED;
        this.currentStep = DocumentProcessingStep.COMPLETED;
        this.progressRate = DocumentProcessingStep.COMPLETED.getProgressRate();

        this.completedFileCount = this.totalFileCount;
        this.failedFileCount = 0;

        this.completedAt = LocalDateTime.now();
        this.errorMessage = null;

    }

    private void validateNotFinished() {
        if (status == DocumentProcessingStatus.COMPLETED
                || status == DocumentProcessingStatus.FAILED) {

            throw new IllegalStateException(
                    "이미 종료된 문서 처리 작업입니다."
            );
        }
    }
}