package com.aha.domain.document.entity;

import com.aha.domain.document.enums.DocumentProcessingStatus;
import com.aha.domain.document.enums.DocumentProcessingStep;
import com.aha.domain.learningnote.entity.LearningNote;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
                        name = "idx_document_processing_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_document_processing_current_step",
                        columnList = "current_step"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_document_processing_learning_note",
                        columnNames = "learning_note_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class DocumentProcessing {

    private static final int MAX_ERROR_CODE_LENGTH = 50;
    private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "learning_note_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(
                    name = "fk_document_processing_learning_note"
            )
    )
    private LearningNote learningNote;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DocumentProcessingStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", length = 50)
    private DocumentProcessingStep currentStep;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    public static DocumentProcessing createPending(
            LearningNote learningNote
    ) {
        if (learningNote == null) {
            throw new IllegalArgumentException(
                    "학습 노트는 필수입니다."
            );
        }

        return DocumentProcessing.builder()
                .learningNote(learningNote)
                .status(DocumentProcessingStatus.PENDING)
                .currentStep(null)
                .retryCount(0)
                .build();
    }

    public void start() {
        if (status != DocumentProcessingStatus.PENDING) {
            throw new IllegalStateException(
                    "대기 중인 문서 처리만 시작할 수 있습니다."
            );
        }

        this.status = DocumentProcessingStatus.PROCESSING;
        this.currentStep = DocumentProcessingStep.TEXT_EXTRACTING;
        this.startedAt = LocalDateTime.now();
        this.completedAt = null;
        clearError();
    }

    public void changeStep(DocumentProcessingStep nextStep) {
        if (status != DocumentProcessingStatus.PROCESSING) {
            throw new IllegalStateException(
                    "처리 중인 작업만 단계를 변경할 수 있습니다."
            );
        }

        if (nextStep == null) {
            throw new IllegalArgumentException(
                    "다음 처리 단계는 필수입니다."
            );
        }

        validateStepTransition(nextStep);
        this.currentStep = nextStep;
    }

    public void complete() {
        if (status != DocumentProcessingStatus.PROCESSING) {
            throw new IllegalStateException(
                    "처리 중인 작업만 완료할 수 있습니다."
            );
        }

        this.status = DocumentProcessingStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        clearError();
    }

    public void fail(
            String errorCode,
            String errorMessage
    ) {
        if (status == DocumentProcessingStatus.COMPLETED) {
            throw new IllegalStateException(
                    "완료된 문서 처리는 실패 상태로 변경할 수 없습니다."
            );
        }

        this.status = DocumentProcessingStatus.FAILED;
        this.errorCode = normalizeNullableText(
                errorCode,
                MAX_ERROR_CODE_LENGTH
        );
        this.errorMessage = normalizeErrorMessage(errorMessage);
        this.completedAt = LocalDateTime.now();
    }

    public void prepareRetry() {
        if (status != DocumentProcessingStatus.FAILED) {
            throw new IllegalStateException(
                    "실패한 문서 처리만 재시도할 수 있습니다."
            );
        }

        this.status = DocumentProcessingStatus.PENDING;
        this.currentStep = null;
        this.retryCount++;
        this.startedAt = null;
        this.completedAt = null;
        clearError();
    }

    private void validateStepTransition(
            DocumentProcessingStep nextStep
    ) {
        if (currentStep == null) {
            if (nextStep != DocumentProcessingStep.TEXT_EXTRACTING) {
                throw new IllegalStateException(
                        "문서 처리는 텍스트 추출 단계부터 시작해야 합니다."
                );
            }
            return;
        }

        if (nextStep.ordinal() <= currentStep.ordinal()) {
            throw new IllegalStateException(
                    "현재 단계보다 이전 또는 동일한 단계로 변경할 수 없습니다."
            );
        }
    }

    private void clearError() {
        this.errorCode = null;
        this.errorMessage = null;
    }

    private String normalizeErrorMessage(String value) {
        if (value == null || value.isBlank()) {
            return "문서 처리 중 오류가 발생했습니다.";
        }

        String normalized = value.trim();

        return normalized.length() <= MAX_ERROR_MESSAGE_LENGTH
                ? normalized
                : normalized.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }

    private String normalizeNullableText(
            String value,
            int maxLength
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        return normalized.length() <= maxLength
                ? normalized
                : normalized.substring(0, maxLength);
    }
}