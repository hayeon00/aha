package com.aha.domain.exam.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "exam_scope_node",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_exam_scope_node_version_code", columnNames = {"exam_version_id", "code"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExamScopeNode {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exam_version_id", nullable = false, foreignKey = @ForeignKey(name = "fk_exam_scope_node_exam_version_id"))
  private ExamVersion examVersion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exam_part_id", nullable = false, foreignKey = @ForeignKey(name = "fk_exam_scope_node_exam_part_id"))
  private ExamPart examPart;

  // 계층형 트리 구조 매핑 (Parent)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name = "fk_exam_scope_node_parent_id"))
  private ExamScopeNode parent;

  // 계층형 트리 구조 매핑 (Children)
  @OneToMany(mappedBy = "parent")
  private List<ExamScopeNode> children = new ArrayList<>();

  @Column(nullable = false, length = 100)
  private String code;

  @Column(name = "node_type", nullable = false, length = 30)
  private String nodeType; // CHAPTER, SECTION 등

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
  public ExamScopeNode(ExamVersion examVersion, ExamPart examPart, ExamScopeNode parent, String code,
      String nodeType, Integer depth, String title, boolean isLeaf, boolean isActive,
      Integer displayOrder) {
    this.examVersion = examVersion;
    this.examPart = examPart;
    this.parent = parent;
    this.code = code;
    this.nodeType = nodeType;
    this.depth = depth;
    this.title = title;
    this.isLeaf = isLeaf;
    this.isActive = isActive;
    this.displayOrder = displayOrder;
    if (parent != null) {
      parent.getChildren().add(this);
    }
  }
}