package com.aha.domain.problemsetgenerationjob.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "ai_generated_problem_choice",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_agpc_id_choice_no", columnNames = {"ai_generated_problem_id", "choice_no"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiGeneratedProblemChoice {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ai_generated_problem_id", nullable = false, foreignKey = @ForeignKey(name = "fk_agpc_problem_id"))
  private AiGeneratedProblem aiGeneratedProblem;

  @Column(name = "choice_no", nullable = false)
  private Integer choiceNo;

  @Column(name = "choice_type", nullable = false, length = 30)
  private String choiceType;

  @Column(name = "choice_content_json", nullable = false, columnDefinition = "TEXT")
  private String choiceContentJson;

  @Column(name = "is_answer", nullable = false)
  private boolean isAnswer;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Builder
  public AiGeneratedProblemChoice(AiGeneratedProblem aiGeneratedProblem, Integer choiceNo,
      String choiceType, String choiceContentJson, Boolean isAnswer) {
    this.aiGeneratedProblem = aiGeneratedProblem;
    this.choiceNo = choiceNo;
    this.choiceType = choiceType;
    this.choiceContentJson = choiceContentJson;
    this.isAnswer = (isAnswer != null) ? isAnswer : false;
    if (aiGeneratedProblem != null) {
      aiGeneratedProblem.getChoices().add(this);
    }
  }
}