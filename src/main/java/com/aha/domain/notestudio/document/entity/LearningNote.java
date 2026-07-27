package com.aha.domain.notestudio.document.entity;

import com.aha.domain.notestudio.document.enums.LearningNoteStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "learning_note",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_learning_note_processing_group",
                columnNames = "processing_group_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LearningNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "processing_group_id", nullable = false)
    private DocumentProcessingGroup processingGroup;

    @Column(nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LearningNoteStatus status;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static LearningNote start(DocumentProcessingGroup processingGroup, String title) {
        if (processingGroup == null || title == null || title.isBlank()) {
            throw new IllegalArgumentException("처리 그룹과 학습노트 제목은 필수입니다.");
        }
        LearningNote note = new LearningNote();
        note.processingGroup = processingGroup;
        note.title = title.trim();
        note.status = LearningNoteStatus.GENERATING;
        return note;
    }

    public void complete() {
        this.status = LearningNoteStatus.COMPLETED;
        this.errorMessage = null;
        this.completedAt = LocalDateTime.now();
    }

    public void restart() {
        this.status = LearningNoteStatus.GENERATING;
        this.errorMessage = null;
        this.completedAt = null;
    }

    public void fail(String errorMessage) {
        this.status = LearningNoteStatus.FAILED;
        this.errorMessage = errorMessage == null ? "학습노트 생성에 실패했습니다." : errorMessage;
    }
}
