package com.aha.domain.problemsetgenerationjob.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

@Entity
@Table(
    name = "ai_generated_problem_choice",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_agp_id_choice_no", columnNames = {"ai_generated_problem_id", "choice_no"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiGeneratedProblemChoice {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ai_generated_problem_id", nullable = false, foreignKey = @ForeignKey(name = "fk_agpc_ai_generated_problem_id"))
  @OnDelete(action = OnDeleteAction.CASCADE)
  private AiGeneratedProblem aiGeneratedProblem;

  @Column(name = "choice_no", nullable = false)
  private Integer choiceNo;

  @Column(name = "choice_content_json", nullable = false, columnDefinition = "JSON")
  private String choiceContentJson;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Builder
  public AiGeneratedProblemChoice(AiGeneratedProblem aiGeneratedProblem, Integer choiceNo,
      String choiceContentJson) {
    this.aiGeneratedProblem = aiGeneratedProblem;
    this.choiceNo = choiceNo;
    this.choiceContentJson = choiceContentJson;
    if (aiGeneratedProblem != null) {
      aiGeneratedProblem.getChoices().add(this);
    }
  }
}
