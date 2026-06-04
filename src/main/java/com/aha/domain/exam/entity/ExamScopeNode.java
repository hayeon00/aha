package com.aha.domain.exam.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "exam_scope_node",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_exam_version_id_code",
                        columnNames = {"exam_version_id", "code"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExamScopeNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "exam_version_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_exam_scope_node_exam_version_id")
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ExamVersion examVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "exam_part_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_exam_scope_node_exam_part_id")
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ExamPart examPart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "parent_id",
            foreignKey = @ForeignKey(name = "fk_exam_scope_node_parent_id")
    )
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private ExamScopeNode parent;

    @OneToMany(mappedBy = "parent")
    @OrderBy("displayOrder ASC, id ASC")
    private List<ExamScopeNode> children = new ArrayList<>();

    @Column(nullable = false, length = 100)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "node_type", nullable = false, length = 30)
    private ScopeNodeType nodeType;

    @Column(nullable = false)
    private Integer depth;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "is_leaf", nullable = false)
    private boolean isLeaf;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public ExamScopeNode(
            ExamVersion examVersion,
            ExamPart examPart,
            ExamScopeNode parent,
            String code,
            ScopeNodeType nodeType,
            Integer depth,
            String title,
            boolean isLeaf,
            boolean isActive,
            Integer displayOrder
    ) {
        this.examVersion = examVersion;
        this.examPart = examPart;
        this.parent = parent;
        this.code = code;
        this.nodeType = nodeType;
        this.depth = depth;
        this.title = title;
        this.isLeaf = isLeaf;
        this.isActive = isActive;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
    }

    public void assignParent(ExamScopeNode parent) {
        this.parent = parent;

        if (parent != null && !parent.getChildren().contains(this)) {
            parent.getChildren().add(this);
        }
    }


    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }
}