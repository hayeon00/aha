package com.aha.domain.learningnote.entity;

import com.aha.domain.document.entity.DocumentChunk;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "learning_content_reference",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_content_reference_chunk",
                        columnNames = {
                                "learning_note_content_id",
                                "document_chunk_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_reference_content",
                        columnList = "learning_note_content_id"
                ),
                @Index(
                        name = "idx_reference_chunk",
                        columnList = "document_chunk_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class LearningContentReference {

    private static final BigDecimal MIN_RELEVANCE_SCORE =
            BigDecimal.ZERO;

    private static final BigDecimal MAX_RELEVANCE_SCORE =
            BigDecimal.ONE;

    private static final int RELEVANCE_SCORE_SCALE = 4;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "learning_note_content_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_reference_content"
            )
    )
    private LearningNoteContent learningNoteContent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "document_chunk_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_reference_chunk"
            )
    )
    private DocumentChunk documentChunk;

    @Column(name = "reference_order", nullable = false)
    private int referenceOrder;

    @Column(name = "relevance_score", precision = 5, scale = 4)
    private BigDecimal relevanceScore;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static LearningContentReference create(
            LearningNoteContent learningNoteContent,
            DocumentChunk documentChunk,
            int referenceOrder,
            BigDecimal relevanceScore
    ) {
        validateLearningNoteContent(
                learningNoteContent
        );

        validateDocumentChunk(
                documentChunk
        );

        validateReferenceOrder(
                referenceOrder
        );

        validateRelevanceScore(
                relevanceScore
        );

        return LearningContentReference.builder()
                .learningNoteContent(
                        learningNoteContent
                )
                .documentChunk(
                        documentChunk
                )
                .referenceOrder(
                        referenceOrder
                )
                .relevanceScore(
                        normalizeRelevanceScore(
                                relevanceScore
                        )
                )
                .build();
    }

    public void changeReferenceOrder(
            int referenceOrder
    ) {
        validateReferenceOrder(
                referenceOrder
        );

        this.referenceOrder =
                referenceOrder;
    }

    public void updateRelevanceScore(
            BigDecimal relevanceScore
    ) {
        validateRelevanceScore(
                relevanceScore
        );

        this.relevanceScore =
                normalizeRelevanceScore(
                        relevanceScore
                );
    }

    private static void validateLearningNoteContent(
            LearningNoteContent learningNoteContent
    ) {
        if (learningNoteContent == null) {
            throw new IllegalArgumentException(
                    "학습 노트 콘텐츠는 필수입니다."
            );
        }
    }

    private static void validateDocumentChunk(
            DocumentChunk documentChunk
    ) {
        if (documentChunk == null) {
            throw new IllegalArgumentException(
                    "문서 청크는 필수입니다."
            );
        }
    }

    private static void validateReferenceOrder(
            int referenceOrder
    ) {
        if (referenceOrder < 1) {
            throw new IllegalArgumentException(
                    "참조 순서는 1 이상이어야 합니다."
            );
        }
    }

    private static void validateRelevanceScore(
            BigDecimal relevanceScore
    ) {
        if (relevanceScore == null) {
            return;
        }

        if (relevanceScore.compareTo(
                MIN_RELEVANCE_SCORE
        ) < 0
                || relevanceScore.compareTo(
                MAX_RELEVANCE_SCORE
        ) > 0) {

            throw new IllegalArgumentException(
                    "관련도 점수는 0 이상 1 이하여야 합니다."
            );
        }
    }

    private static BigDecimal normalizeRelevanceScore(
            BigDecimal relevanceScore
    ) {
        if (relevanceScore == null) {
            return null;
        }

        return relevanceScore.setScale(
                RELEVANCE_SCORE_SCALE,
                RoundingMode.HALF_UP
        );
    }
}