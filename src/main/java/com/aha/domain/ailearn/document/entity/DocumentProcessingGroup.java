package com.aha.domain.ailearn.document.entity;

import com.aha.domain.ailearn.document.enums.DocumentProcessingStatus;
import com.aha.domain.ailearn.document.enums.DocumentProcessingStep;
import com.aha.domain.userexam.entity.UserExam;
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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class DocumentProcessingGroup {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;
    private static final String DEFAULT_ERROR_MESSAGE = "문서 처리에 실패했습니다.";

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

    @Column(name = "total_file_count", nullable = false)
    private int totalFileCount;

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
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    public static DocumentProcessingGroup createPending(
            UserExam userExam,
            int totalFileCount
    ) {
        validateCreationArguments(
                userExam,
                totalFileCount
        );

        return DocumentProcessingGroup.builder()
                .userExam(userExam)
                .status(DocumentProcessingStatus.PENDING)
                .currentStep(DocumentProcessingStep.UPLOAD_PENDING)
                .totalFileCount(totalFileCount)
                .errorMessage(null)
                .startedAt(null)
                .completedAt(null)
                .build();
    }


    public void markFilesUploaded() {

        validateNotFinished();

        if (this.status != DocumentProcessingStatus.PENDING) {
            throw new IllegalStateException("처리 대기 상태에서만 파일 업로드를 완료할 수 있습니다.");
        }

        if (this.currentStep != DocumentProcessingStep.UPLOAD_PENDING) {
            throw new IllegalStateException("업로드 대기 단계에서만 파일 업로드를 완료할 수 있습니다.");
        }

        this.currentStep = DocumentProcessingStep.UPLOAD_COMPLETED;
        this.errorMessage = null;
    }

    public void updateStep(DocumentProcessingStep nextStep) {

        validateNotFinished();
        validateProcessingStatus();
        validateNextStep(nextStep);

        this.currentStep = nextStep;
    }

    public void complete() {
        validateNotFinished();
        validateProcessingStatus();

        if (this.currentStep != DocumentProcessingStep.LEARNING_CONTENT_GENERATED) {
            throw new IllegalStateException("학습 콘텐츠 생성이 완료된 작업만 최종 완료 처리할 수 있습니다.");
        }

        this.status = DocumentProcessingStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.errorMessage = null;
    }


    public void fail(String errorMessage) {
        validateNotFinished();

        this.status = DocumentProcessingStatus.FAILED;
        this.errorMessage = normalizeErrorMessage(errorMessage);
        this.completedAt = LocalDateTime.now();
    }

    public void prepareRetry() {
        if (this.status != DocumentProcessingStatus.FAILED) {
            throw new IllegalStateException("실패한 문서 처리 작업만 재시도할 수 있습니다.");
        }

        this.status = DocumentProcessingStatus.PENDING;
        this.currentStep = DocumentProcessingStep.UPLOAD_COMPLETED;
        this.startedAt = null;
        this.completedAt = null;
        this.errorMessage = null;
    }

    private static void validateCreationArguments(UserExam userExam, int totalFileCount) {

        if (userExam == null) {
            throw new IllegalArgumentException("사용자 시험은 필수입니다.");
        }

        if (totalFileCount <= 0) {
            throw new IllegalArgumentException("전체 문서 파일 수는 1개 이상이어야 합니다.");
        }
    }

    private void validateProcessingStatus() {

        if (this.status != DocumentProcessingStatus.PROCESSING) {
            throw new IllegalStateException("문서 처리 중인 작업만 처리 단계를 변경할 수 있습니다.");
        }
    }

    private void validateNextStep(DocumentProcessingStep nextStep) {
        if (nextStep == null) {
            throw new IllegalArgumentException("다음 문서 처리 단계는 필수입니다.");
        }

        if (nextStep == DocumentProcessingStep.UPLOAD_PENDING || nextStep == DocumentProcessingStep.UPLOAD_COMPLETED) {
            throw new IllegalArgumentException("비동기 처리 중 업로드 단계로 변경할 수 없습니다.");
        }

        DocumentProcessingStep expectedNextStep = resolveExpectedNextStep();

        if (nextStep != expectedNextStep) {
            throw new IllegalStateException("현재 단계에서 요청한 단계로 이동할 수 없습니다. "
                            + "currentStep=" + this.currentStep
                            + ", nextStep=" + nextStep
            );
        }
    }


    private DocumentProcessingStep resolveExpectedNextStep() {
        return switch (this.currentStep) {
            case TEXT_EXTRACTING -> DocumentProcessingStep.TEXT_EXTRACTED;
            case TEXT_EXTRACTED -> DocumentProcessingStep.CONTENT_ANALYZING;
            case CONTENT_ANALYZING -> DocumentProcessingStep.CONTENT_ANALYZED;
            case CONTENT_ANALYZED -> DocumentProcessingStep.SCOPE_MAPPING;
            case SCOPE_MAPPING -> DocumentProcessingStep.LEARNING_CONTENT_GENERATING;
            case LEARNING_CONTENT_GENERATING -> DocumentProcessingStep.LEARNING_CONTENT_GENERATED;

            case UPLOAD_PENDING, UPLOAD_COMPLETED ->
                    throw new IllegalStateException("업로드 단계에서는 비동기 처리 단계를 변경할 수 없습니다.");

            case LEARNING_CONTENT_GENERATED ->
                    throw new IllegalStateException("이미 마지막 문서 처리 단계에 도달했습니다.");
        };
    }

    private void validateNotFinished() {
        if (this.status == DocumentProcessingStatus.COMPLETED
                || this.status == DocumentProcessingStatus.FAILED) {
            throw new IllegalStateException(
                    "이미 종료된 문서 처리 작업입니다."
            );
        }
    }

    private String normalizeErrorMessage(
            String errorMessage
    ) {
        if (errorMessage == null
                || errorMessage.isBlank()) {
            return DEFAULT_ERROR_MESSAGE;
        }

        String normalizedMessage =
                errorMessage.trim();

        if (normalizedMessage.length()
                <= MAX_ERROR_MESSAGE_LENGTH) {
            return normalizedMessage;
        }

        return normalizedMessage.substring(
                0,
                MAX_ERROR_MESSAGE_LENGTH
        );
    }
}
