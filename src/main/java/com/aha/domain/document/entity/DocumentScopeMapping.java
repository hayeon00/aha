package com.aha.domain.document.entity;

import com.aha.domain.document.enums.DocumentScopeMappingMethod;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "document_scope_mapping",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_mapping_chunk_scope",
                        columnNames = {
                                "document_chunk_id",
                                "exam_scope_node_id"
                        }
                ),
                @UniqueConstraint(
                        name = "uk_mapping_chunk_rank",
                        columnNames = {
                                "document_chunk_id",
                                "rank_no"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_mapping_scope",
                        columnList = "exam_scope_node_id"
                ),
                @Index(
                        name = "idx_mapping_scope_selected",
                        columnList = "exam_scope_node_id, is_selected"
                ),
                @Index(
                        name = "idx_mapping_confidence",
                        columnList = "confidence_score"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class DocumentScopeMapping {

    private static final BigDecimal MIN_CONFIDENCE_SCORE = BigDecimal.ZERO;
    private static final BigDecimal MAX_CONFIDENCE_SCORE = BigDecimal.ONE;

    private static final int CONFIDENCE_SCALE = 4;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "document_chunk_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_mapping_chunk"
            )
    )
    private DocumentChunk documentChunk;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "exam_scope_node_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_mapping_scope"
            )
    )
    private ExamScopeNode examScopeNode;

    @Column(name = "rank_no", nullable = false)
    private Integer rankNo;

    @Column(name = "confidence_score", nullable = false, precision = 5, scale = 4)
    private BigDecimal confidenceScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "mapping_method", nullable = false, length = 30)
    private DocumentScopeMappingMethod mappingMethod;

    @Lob
    @Column(name = "mapping_reason", columnDefinition = "TEXT")
    private String mappingReason;

    @Column(name = "is_selected", nullable = false)
    private boolean isSelected;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static DocumentScopeMapping create(
            DocumentChunk documentChunk,
            ExamScopeNode examScopeNode,
            int rankNo,
            BigDecimal confidenceScore,
            DocumentScopeMappingMethod mappingMethod,
            String mappingReason
    ) {
        validateDocumentChunk(documentChunk);
        validateExamScopeNode(examScopeNode);
        validateRankNo(rankNo);
        validateConfidenceScore(confidenceScore);
        validateMappingMethod(mappingMethod);

        return DocumentScopeMapping.builder()
                .documentChunk(documentChunk)
                .examScopeNode(examScopeNode)
                .rankNo(rankNo)
                .confidenceScore(
                        normalizeConfidenceScore(
                                confidenceScore
                        )
                )
                .mappingMethod(mappingMethod)
                .mappingReason(
                        normalizeMappingReason(
                                mappingReason
                        )
                )
                .isSelected(false)
                .build();
    }


    public void select() {
        this.isSelected = true;
    }

    public void deselect() {
        this.isSelected = false;
    }

    public void updateMappingResult(
            BigDecimal confidenceScore,
            DocumentScopeMappingMethod mappingMethod,
            String mappingReason
    ) {
        validateConfidenceScore(confidenceScore);
        validateMappingMethod(mappingMethod);

        this.confidenceScore =
                normalizeConfidenceScore(
                        confidenceScore
                );

        this.mappingMethod = mappingMethod;

        this.mappingReason =
                normalizeMappingReason(
                        mappingReason
                );
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

    private static void validateExamScopeNode(
            ExamScopeNode examScopeNode
    ) {
        if (examScopeNode == null) {
            throw new IllegalArgumentException(
                    "시험 목차 노드는 필수입니다."
            );
        }
    }

    private static void validateRankNo(
            int rankNo
    ) {
        if (rankNo < 1) {
            throw new IllegalArgumentException(
                    "매핑 순위는 1 이상이어야 합니다."
            );
        }
    }

    private static void validateConfidenceScore(
            BigDecimal confidenceScore
    ) {
        if (confidenceScore == null) {
            throw new IllegalArgumentException(
                    "매핑 신뢰도는 필수입니다."
            );
        }

        if (confidenceScore.compareTo(
                MIN_CONFIDENCE_SCORE
        ) < 0
                || confidenceScore.compareTo(
                MAX_CONFIDENCE_SCORE
        ) > 0) {

            throw new IllegalArgumentException(
                    "매핑 신뢰도는 0 이상 1 이하여야 합니다."
            );
        }
    }

    private static BigDecimal normalizeConfidenceScore(
            BigDecimal confidenceScore
    ) {
        return confidenceScore.setScale(
                CONFIDENCE_SCALE,
                RoundingMode.HALF_UP
        );
    }

    private static void validateMappingMethod(
            DocumentScopeMappingMethod mappingMethod
    ) {
        if (mappingMethod == null) {
            throw new IllegalArgumentException(
                    "매핑 방식은 필수입니다."
            );
        }
    }

    private static String normalizeMappingReason(
            String mappingReason
    ) {
        if (mappingReason == null
                || mappingReason.isBlank()) {
            return null;
        }

        return mappingReason.trim();
    }
}
