package com.aha.domain.problemsetgenerationjob.entity;

import com.aha.domain.exam.entity.Exam;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.exam.entity.ExamVersion;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
@Table(name = "ai_generated_problem")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiGeneratedProblem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "problem_set_generation_job_id", nullable = false, foreignKey = @ForeignKey(name = "fk_agp_problem_set_generation_job_id"))
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ProblemSetGenerationJob problemSetGenerationJob;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exam_id", nullable = false, foreignKey = @ForeignKey(name = "fk_agp_exam_id"))
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Exam exam;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exam_version_id", nullable = false, foreignKey = @ForeignKey(name = "fk_agp_exam_version_id"))
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ExamVersion examVersion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exam_scope_node_id", nullable = false, foreignKey = @ForeignKey(name = "fk_agp_exam_scope_node_id"))
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ExamScopeNode examScopeNode;

  @Column(name = "expression_type", nullable = false, length = 50)
  private String expressionType;

  @Column(nullable = false, length = 20)
  private String difficulty;

  @Column(name = "question_content_json", nullable = false, columnDefinition = "JSON")
  private String questionContentJson;

  @Column(name = "explanation_json", nullable = false, columnDefinition = "JSON")
  private String explanationJson;

  @Column(name = "answer_type", nullable = false, length = 30)
  private String answerType;

  @Column(name = "answer_json", nullable = false, columnDefinition = "JSON")
  private String answerJson;

  @Column(name = "choice_type", nullable = false, length = 30)
  private String choiceType;

  @Column(name = "review_status", nullable = false, length = 30)
  private String reviewStatus;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "aiGeneratedProblem", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<AiGeneratedProblemChoice> choices = new ArrayList<>();

  @Builder
  public AiGeneratedProblem(ProblemSetGenerationJob problemSetGenerationJob, Exam exam,
      ExamVersion examVersion, ExamScopeNode examScopeNode, String expressionType,
      String difficulty, String questionContentJson, String explanationJson,
      String answerType, String answerJson, String choiceType, String reviewStatus) {
    this.problemSetGenerationJob = problemSetGenerationJob;
    this.exam = exam;
    this.examVersion = examVersion;
    this.examScopeNode = examScopeNode;
    this.expressionType = expressionType;
    this.difficulty = difficulty;
    this.questionContentJson = questionContentJson;
    this.explanationJson = explanationJson;
    this.answerType = answerType;
    this.answerJson = answerJson;
    this.choiceType = choiceType;
    this.reviewStatus = reviewStatus;
  }
}
