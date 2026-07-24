package com.aha.domain.ailearn.document.entity;

import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.ailearn.document.enums.LearningContentSourceType;
import com.aha.domain.exam.enums.ExamScopeNodeType;
import com.aha.domain.userexam.entity.UserExam;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
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

    @Column(
            name = "title",
            nullable = false,
            length = 255
    )
    private String title;

    @Lob
    @Column(
            name = "content",
            nullable = false,
            columnDefinition = "LONGTEXT"
    )
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private LearningContentSourceType sourceType;

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

    /**
     * 사용자의 특정 시험 목차에 대한 개념설명을 생성한다.
     */
    public static UserLearningContent create(
            UserExam userExam,
            ExamScopeNode examScopeNode,
            String title,
            String content
    ) {
        return create(userExam, examScopeNode, title, content,
                LearningContentSourceType.DOCUMENT);
    }

    public static UserLearningContent create(
            UserExam userExam,
            ExamScopeNode examScopeNode,
            String title,
            String content,
            LearningContentSourceType sourceType
    ) {
        validateCreationArguments(
                userExam,
                examScopeNode,
                title,
                content
        );
        if (sourceType == null) {
            throw new IllegalArgumentException("개념설명 출처는 필수입니다.");
        }

        return UserLearningContent.builder()
                .userExam(userExam)
                .examScopeNode(examScopeNode)
                .title(normalizeTitle(title))
                .content(normalizeContent(content))
                .sourceType(sourceType)
                .build();
    }

    /**
     * 기존 개념설명을 새 생성 결과로 교체한다.
     */
    public void replace(
            String title,
            String content
    ) {
        replace(title, content, LearningContentSourceType.DOCUMENT);
    }

    public void replace(
            String title,
            String content,
            LearningContentSourceType sourceType
    ) {
        validateText(title, content);
        if (sourceType == null) {
            throw new IllegalArgumentException("개념설명 출처는 필수입니다.");
        }

        this.title = normalizeTitle(title);
        this.content = normalizeContent(content);
        this.sourceType = sourceType;
    }

    private static void validateCreationArguments(
            UserExam userExam,
            ExamScopeNode examScopeNode,
            String title,
            String content
    ) {
        if (userExam == null) {
            throw new IllegalArgumentException(
                    "사용자 시험은 필수입니다."
            );
        }

        validateExamScopeNode(examScopeNode);
        validateText(title, content);
    }

    private static void validateExamScopeNode(
            ExamScopeNode examScopeNode
    ) {
        if (examScopeNode == null) {
            throw new IllegalArgumentException(
                    "시험 목차는 필수입니다."
            );
        }

        if (examScopeNode.getNodeType()
                != ExamScopeNodeType.TOPIC) {
            throw new IllegalArgumentException(
                    "개념설명은 Topic 목차에만 생성할 수 있습니다."
            );
        }
    }

    private static void validateText(
            String title,
            String content
    ) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "개념설명 제목은 필수입니다."
            );
        }

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "개념설명 내용은 필수입니다."
            );
        }
    }

    private static String normalizeTitle(
            String title
    ) {
        return title.trim();
    }

    private static String normalizeContent(
            String content
    ) {
        return content.trim();
    }
}
