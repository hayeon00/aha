package com.aha.domain.learningnote.entity;

import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.learningnote.enums.LearningContentSourceType;
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
        name = "learning_note_content",
        indexes = {
                @Index(
                        name = "idx_learning_note_content_scope_node",
                        columnList = "exam_scope_node_id"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_learning_note_content_note_scope",
                        columnNames = {
                                "learning_note_id",
                                "exam_scope_node_id"
                        }
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class LearningNoteContent {

    private static final int MAX_TITLE_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "learning_note_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_learning_note_content_learning_note"
            )
    )
    private LearningNote learningNote;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "exam_scope_node_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_learning_note_content_exam_scope_node"
            )
    )
    private ExamScopeNode examScopeNode;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private LearningContentSourceType sourceType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static LearningNoteContent createDocumentBased(
            LearningNote learningNote,
            ExamScopeNode examScopeNode,
            String title,
            String content
    ) {
        validateAssociations(learningNote, examScopeNode);

        return LearningNoteContent.builder()
                .learningNote(learningNote)
                .examScopeNode(examScopeNode)
                .title(normalizeTitle(title))
                .content(normalizeContent(content))
                .sourceType(LearningContentSourceType.DOCUMENT_BASED)
                .build();
    }

    public static LearningNoteContent createUserWritten(
            LearningNote learningNote,
            ExamScopeNode examScopeNode,
            String content
    ) {
        validateAssociations(learningNote, examScopeNode);

        return LearningNoteContent.builder()
                .learningNote(learningNote)
                .examScopeNode(examScopeNode)
                .title(normalizeTitle(examScopeNode.getTitle()))
                .content(normalizeContent(content))
                .sourceType(LearningContentSourceType.USER_WRITTEN)
                .build();
    }

    public void update(
            String title,
            String content
    ) {
        this.title = normalizeTitle(title);
        this.content = normalizeContent(content);
    }

    public void updateTitle(String title) {
        this.title = normalizeTitle(title);
    }

    public void updateContent(String content) {
        this.content = normalizeContent(content);
    }

    private static void validateAssociations(
            LearningNote learningNote,
            ExamScopeNode examScopeNode
    ) {
        if (learningNote == null) {
            throw new IllegalArgumentException(
                    "학습 노트는 필수입니다."
            );
        }

        if (examScopeNode == null) {
            throw new IllegalArgumentException(
                    "시험 목차는 필수입니다."
            );
        }
    }

    private static String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "개념 설명 제목은 필수입니다."
            );
        }

        String normalizedTitle = title.trim();

        if (normalizedTitle.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException(
                    "개념 설명 제목은 255자 이하여야 합니다."
            );
        }

        return normalizedTitle;
    }

    private static String normalizeContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "개념 설명 내용은 필수입니다."
            );
        }

        return content.trim();
    }
}