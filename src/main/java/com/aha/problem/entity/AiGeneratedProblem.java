package com.aha.problem.entity;

import com.aha.exam.entity.ExamScopeNode;
import com.aha.user.entity.User;
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
    name = "ai_generated_problem",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_agp_code", columnNames = "code")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiGeneratedProblem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_agp_user_id"))
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exam_scope_node_id", nullable = false, foreignKey = @ForeignKey(name = "fk_agp_exam_scope_node_id"))
  private ExamScopeNode examScopeNode;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "domain_type_id", nullable = false, foreignKey = @ForeignKey(name = "fk_agp_domain_type_id"))
  private DomainType domainType;

  @Column(nullable = false, length = 50)
  private String code;

  @Column(name = "problem_type", nullable = false, length = 30)
  private String problemType;

  @Column(name = "question_content_json", nullable = false, columnDefinition = "TEXT")
  private String questionContentJson;

  @Column(name = "explanation_json", nullable = false, columnDefinition = "TEXT")
  private String explanationJson;

  @Column(name = "difficulty_level", nullable = false, length = 20)
  private String difficultyLevel;

  @Column(name = "generation_status", nullable = false, length = 20)
  private String generationStatus;

  @Column(name = "raw_prompt_context", columnDefinition = "TEXT")
  private String rawPromptContext;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "aiGeneratedProblem", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<AiGeneratedProblemChoice> choices = new ArrayList<>();

  @Builder
  public AiGeneratedProblem(User user, ExamScopeNode examScopeNode, DomainType domainType, String code,
      String problemType, String questionContentJson, String explanationJson,
      String difficultyLevel, String generationStatus, String rawPromptContext) {
    this.user = user;
    this.examScopeNode = examScopeNode;
    this.domainType = domainType;
    this.code = code;
    this.problemType = problemType;
    this.questionContentJson = questionContentJson;
    this.explanationJson = explanationJson;
    this.difficultyLevel = difficultyLevel;
    this.generationStatus = (generationStatus != null) ? generationStatus : "PENDING";
    this.rawPromptContext = rawPromptContext;
  }
}