package com.aha.domain.problem.entity;

import com.aha.domain.exam.entity.ExamScopeNode;
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
    name = "problem",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_problem_code", columnNames = "code")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Problem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exam_scope_node_id", nullable = false, foreignKey = @ForeignKey(name = "fk_problem_exam_scope_node_id"))
  private ExamScopeNode examScopeNode;

  @Column(nullable = false, length = 50)
  private String code;

  @Column(name = "problem_type", nullable = false, length = 30)
  private String problemType;

  @Column(name = "question_content_json", nullable = false, columnDefinition = "TEXT")
  private String questionContentJson;

  @Column(name = "explanation_json", nullable = false, columnDefinition = "TEXT")
  private String explanationJson;

  @Column(name = "default_score", nullable = false)
  private Integer defaultScore;

  @Column(name = "is_active", nullable = false)
  private boolean isActive;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProblemChoice> choices = new ArrayList<>();

  @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProblemAvailableUsageType> availableUsageTypes = new ArrayList<>();

  @Builder
  public Problem(ExamScopeNode examScopeNode, String code, String problemType,
      String questionContentJson, String explanationJson, Integer defaultScore, Boolean isActive) {
    this.examScopeNode = examScopeNode;
    this.code = code;
    this.problemType = problemType;
    this.questionContentJson = questionContentJson;
    this.explanationJson = explanationJson;
    this.defaultScore = defaultScore;
    this.isActive = (isActive != null) ? isActive : true;
  }
}