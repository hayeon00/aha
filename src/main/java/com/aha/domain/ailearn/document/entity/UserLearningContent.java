package com.aha.domain.ailearn.document.entity;

import com.aha.domain.ailearn.document.enums.UserLearningContentStatus;
import com.aha.domain.exam.entity.ExamScopeNode;
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
        name = "user_learning_content",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ulc_user_exam_scope",
                        columnNames = {
                                "user_exam_id",
                                "exam_scope_node_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_ulc_user_exam_id",
                        columnList = "user_exam_id"
                ),
                @Index(
                        name = "idx_ulc_exam_scope_node_id",
                        columnList = "exam_scope_node_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLearningContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_exam_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_ulc_user_exam_id"
            )
    )
    private UserExam userExam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "exam_scope_node_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_ulc_exam_scope_node_id"
            )
    )
    private ExamScopeNode examScopeNode;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(
            name = "content",
            nullable = false,
            columnDefinition = "LONGTEXT"
    )
    private String content;

    @Column(
            name = "keywords_json",
            columnDefinition = "json"
    )
    private String keywordsJson;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private UserLearningContentStatus status;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @Builder
    private UserLearningContent(
            UserExam userExam,
            ExamScopeNode examScopeNode,
            String title,
            String content,
            String keywordsJson
    ) {
        if (userExam == null) {
            throw new IllegalArgumentException(
                    "사용자 시험 정보는 필수입니다."
            );
        }

        if (examScopeNode == null) {
            throw new IllegalArgumentException(
                    "시험 목차 정보는 필수입니다."
            );
        }

        validateContent(title, content);

        this.userExam = userExam;
        this.examScopeNode = examScopeNode;
        this.title = title.trim();
        this.content = content.trim();
        this.keywordsJson = keywordsJson;
        this.status = UserLearningContentStatus.COMPLETED;
    }

    public void updateContent(
            String title,
            String content,
            String keywordsJson
    ) {
        validateContent(title, content);

        this.title = title.trim();
        this.content = content.trim();
        this.keywordsJson = keywordsJson;
        this.status = UserLearningContentStatus.COMPLETED;
    }

    public void fail() {
        this.status = UserLearningContentStatus.FAILED;
    }

    private void validateContent(
            String title,
            String content
    ) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "개념 설명 제목은 필수입니다."
            );
        }

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "개념 설명 본문은 필수입니다."
            );
        }
    }
}