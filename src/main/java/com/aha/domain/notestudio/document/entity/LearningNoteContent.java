package com.aha.domain.notestudio.document.entity;

import com.aha.domain.notestudio.document.enums.LearningContentSourceType;
import com.aha.domain.exam.entity.ExamScopeNode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "learning_note_content",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_learning_note_content_note_scope",
                columnNames = {"learning_note_id", "exam_scope_node_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LearningNoteContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learning_note_id", nullable = false)
    private LearningNote learningNote;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_scope_node_id", nullable = false)
    private ExamScopeNode examScopeNode;

    @Column(nullable = false, length = 255)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
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

    public static LearningNoteContent create(
            LearningNote note, ExamScopeNode scopeNode, String title, String content) {
        LearningNoteContent result = new LearningNoteContent();
        result.learningNote = note;
        result.examScopeNode = scopeNode;
        result.replace(title, content);
        return result;
    }

    public void replace(String title, String content) {
        if (title == null || title.isBlank() || content == null || content.isBlank()) {
            throw new IllegalArgumentException("개념 설명의 제목과 내용은 필수입니다.");
        }
        this.title = title.trim();
        this.content = content.trim();
        this.sourceType = LearningContentSourceType.DOCUMENT;
    }
}
