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
                        name = "idx_processing_note",
                        columnList = "learning_note_id"
                ),
                @Index(
                        name = "idx_processing_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_processing_status_step",
                        columnList = "status, current_step"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_processing_note_attempt",
                        columnNames = {
                                "learning_note_id",
                                "attempt_no"
                        }
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class DocumentProcessing {

    private static final int MAX_PIPELINE_VERSION_LENGTH = 50;
    private static final int MAX_ERROR_CODE_LENGTH = 100;
    private static final int MAX_ERROR_MESSAGE_LENGTH = 2000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "learning_note_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_processing_learning_note"
            )
    )
    private LearningNote learningNote;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DocumentProcessingStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", length = 50)
    private DocumentProcessingStep currentStep;

    @Column(name = "pipeline_version", nullable = false, length = 50)
    private String pipelineVersion;

    @Column(name = "attempt_no", nullable = false)
    private int attemptNo;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

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
            LearningNote learningNote,
            String pipelineVersion,
            int attemptNo
    ) {
        validateLearningNote(learningNote);
        validateAttemptNo(attemptNo);

        return DocumentProcessing.builder()
                .learningNote(learningNote)
                .status(DocumentProcessingStatus.PENDING)
                .currentStep(null)
                .pipelineVersion(
                        normalizePipelineVersion(pipelineVersion)
                )
                .attemptNo(attemptNo)
                .build();
    }

    public void start() {
        if (status != DocumentProcessingStatus.PENDING) {
            throw new IllegalStateException(
                    "대기 중인 문서 처리만 시작할 수 있습니다."
            );
        }

        this.status = DocumentProcessingStatus.PROCESSING;
        this.currentStep =
                DocumentProcessingStep.DOCUMENT_PARSING;
        this.startedAt = LocalDateTime.now();
        this.completedAt = null;

        clearError();
    }

    public void changeStep(
            DocumentProcessingStep nextStep
    ) {
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

    public void moveToNextStep() {
        if (status != DocumentProcessingStatus.PROCESSING) {
            throw new IllegalStateException(
                    "처리 중인 작업만 다음 단계로 이동할 수 있습니다."
            );
        }

        if (currentStep == null) {
            throw new IllegalStateException(
                    "현재 처리 단계가 설정되어 있지 않습니다."
            );
        }

        this.currentStep = currentStep.next();
    }

    public void complete() {
        if (status != DocumentProcessingStatus.PROCESSING) {
            throw new IllegalStateException(
                    "처리 중인 작업만 완료할 수 있습니다."
            );
        }

        if (currentStep != DocumentProcessingStep.COMPLETED) {
            throw new IllegalStateException(
                    "모든 처리 단계를 완료한 후 작업을 완료할 수 있습니다."
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

        if (status == DocumentProcessingStatus.CANCELLED) {
            throw new IllegalStateException(
                    "취소된 문서 처리는 실패 상태로 변경할 수 없습니다."
            );
        }

        this.status = DocumentProcessingStatus.FAILED;
        this.errorCode = normalizeNullableText(
                errorCode,
                MAX_ERROR_CODE_LENGTH
        );
        this.errorMessage =
                normalizeErrorMessage(errorMessage);
        this.completedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (status == DocumentProcessingStatus.COMPLETED) {
            throw new IllegalStateException(
                    "완료된 문서 처리는 취소할 수 없습니다."
            );
        }

        if (status == DocumentProcessingStatus.FAILED) {
            throw new IllegalStateException(
                    "실패한 문서 처리는 취소할 수 없습니다."
            );
        }

        this.status = DocumentProcessingStatus.CANCELLED;
        this.completedAt = LocalDateTime.now();
    }

    private void validateStepTransition(
            DocumentProcessingStep nextStep
    ) {
        if (currentStep == null) {
            if (nextStep
                    != DocumentProcessingStep.DOCUMENT_PARSING) {
                throw new IllegalStateException(
                        "문서 처리는 문서 파싱 단계부터 시작해야 합니다."
                );
            }

            return;
        }

        DocumentProcessingStep expectedNextStep =
                currentStep.next();

        if (nextStep != expectedNextStep) {
            throw new IllegalStateException(
                    "잘못된 처리 단계 전이입니다. 현재 단계="
                            + currentStep
                            + ", 다음 가능한 단계="
                            + expectedNextStep
            );
        }
    }

    private void clearError() {
        this.errorCode = null;
        this.errorMessage = null;
    }

    private static void validateLearningNote(
            LearningNote learningNote
    ) {
        if (learningNote == null) {
            throw new IllegalArgumentException(
                    "학습 노트는 필수입니다."
            );
        }
    }

    private static void validateAttemptNo(
            int attemptNo
    ) {
        if (attemptNo < 1) {
            throw new IllegalArgumentException(
                    "처리 시도 번호는 1 이상이어야 합니다."
            );
        }
    }

    private static String normalizePipelineVersion(
            String pipelineVersion
    ) {
        if (pipelineVersion == null
                || pipelineVersion.isBlank()) {
            throw new IllegalArgumentException(
                    "파이프라인 버전은 필수입니다."
            );
        }

        String normalized =
                pipelineVersion.trim();

        if (normalized.length()
                > MAX_PIPELINE_VERSION_LENGTH) {
            throw new IllegalArgumentException(
                    "파이프라인 버전은 50자 이하여야 합니다."
            );
        }

        return normalized;
    }

    private static String normalizeErrorMessage(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return "문서 처리 중 오류가 발생했습니다.";
        }

        String normalized = value.trim();

        return normalized.length()
                <= MAX_ERROR_MESSAGE_LENGTH
                ? normalized
                : normalized.substring(
                0,
                MAX_ERROR_MESSAGE_LENGTH
        );
    }

    private static String normalizeNullableText(
            String value,
            int maxLength
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        return normalized.length() <= maxLength
                ? normalized
                : normalized.substring(
                0,
                maxLength
        );
    }




}