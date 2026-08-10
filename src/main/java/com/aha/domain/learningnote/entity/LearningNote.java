package com.aha.domain.learningnote.entity;

import com.aha.domain.document.entity.SourceDocument;
import com.aha.domain.learningnote.enums.LearningNoteStatus;
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
        name = "learning_note",
        indexes = {
                @Index(
                        name = "idx_learning_note_user_exam",
                        columnList = "user_exam_id"
                ),
                @Index(
                        name = "idx_learning_note_source_document",
                        columnList = "source_document_id"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_learning_note_exam_document",
                        columnNames = {
                                "user_exam_id",
                                "source_document_id"
                        }
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class LearningNote {

    private static final int MAX_TITLE_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_exam_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_learning_note_user_exam"
            )
    )
    private UserExam userExam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "source_document_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_learning_note_source_document"
            )
    )
    private SourceDocument sourceDocument;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private LearningNoteStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static LearningNote create(
            UserExam userExam,
            SourceDocument sourceDocument,
            String title
    ) {
        validateUserExam(userExam);
        validateSourceDocument(sourceDocument);

        return LearningNote.builder()
                .userExam(userExam)
                .sourceDocument(sourceDocument)
                .title(normalizeTitle(title))
                .status(LearningNoteStatus.GENERATING)
                .build();
    }

    public void markReady() {
        this.status = LearningNoteStatus.READY;
    }

    public void markFailed() {
        this.status = LearningNoteStatus.FAILED;
    }

    private static void validateUserExam(UserExam userExam) {
        if (userExam == null) {
            throw new IllegalArgumentException(
                    "사용자 시험은 필수입니다."
            );
        }
    }

    private static void validateSourceDocument(
            SourceDocument sourceDocument
    ) {
        if (sourceDocument == null) {
            throw new IllegalArgumentException(
                    "원본 문서는 필수입니다."
            );
        }

        if (!sourceDocument.isActive()) {
            throw new IllegalArgumentException(
                    "활성화된 원본 문서만 학습 노트에 연결할 수 있습니다."
            );
        }
    }

    private static String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "학습 노트 제목은 필수입니다."
            );
        }

        String normalizedTitle = title.trim();

        if (normalizedTitle.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException(
                    "학습 노트 제목은 255자 이하여야 합니다."
            );
        }

        return normalizedTitle;
    }

}
