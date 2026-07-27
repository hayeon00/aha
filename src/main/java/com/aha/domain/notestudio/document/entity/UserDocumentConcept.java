package com.aha.domain.notestudio.document.entity;

import com.aha.domain.notestudio.document.enums.UserDocumentConceptSourceType;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.notestudio.document.entity.SourceDocument;
import com.aha.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "user_document_concept",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_document_concept_tenant_document_toc",
                columnNames = {"user_id", "document_id", "toc_id"}),
        indexes = {
                @Index(name = "idx_udc_document_source", columnList = "document_id,source_type"),
                @Index(name = "idx_udc_user_document", columnList = "user_id,document_id")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDocumentConcept {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_udc_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_udc_document"))
    private SourceDocument document;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "toc_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_udc_toc"))
    private ExamScopeNode toc;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private UserDocumentConceptSourceType sourceType;

    @Column(nullable = false, length = 255)
    private String title;

    @Lob @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private UserDocumentConcept(User user, SourceDocument document, ExamScopeNode toc,
                                UserDocumentConceptSourceType sourceType,
                                String title, String content) {
        if (user == null || document == null || toc == null || sourceType == null
                || title == null || title.isBlank() || content == null || content.isBlank()) {
            throw new IllegalArgumentException("문서 개념 캐시의 필수 값이 누락되었습니다.");
        }
        this.user = user;
        this.document = document;
        this.toc = toc;
        this.sourceType = sourceType;
        this.title = title.trim();
        this.content = content.trim();
    }

    public static UserDocumentConcept aiGenerated(User user, SourceDocument document,
                                                   ExamScopeNode toc, String title, String content) {
        return new UserDocumentConcept(user, document, toc,
                UserDocumentConceptSourceType.AI_GENERATED, title, content);
    }

    public void replaceGeneratedContent(String title, String content) {
        if (title == null || title.isBlank() || content == null || content.isBlank()) {
            throw new IllegalArgumentException("생성 개념의 제목과 내용은 필수입니다.");
        }
        this.title = title.trim();
        this.content = content.trim();
        this.sourceType = UserDocumentConceptSourceType.AI_GENERATED;
    }

    public void updateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("개념 설명 내용은 필수입니다.");
        }
        this.content = content.trim();
    }
}
