package com.aha.domain.workbook.entity;

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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "workbook_answer",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_workbook_answer_attempt_item", columnNames = {"workbook_attempt_id", "workbook_item_id"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkbookUserAnswer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "workbook_attempt_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_workbook_answer_workbook_attempt_id")
  )
  private WorkbookAttempt workbookAttempt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "workbook_item_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_workbook_answer_workbook_item_id")
  )
  private WorkbookItem workbookItem;

  @Column(name = "problem_choice_id")
  private Long problemChoiceId;

  @Column(name = "answer_content_json", columnDefinition = "JSON")
  private String answerContentJson;

  @Column(name = "is_answered", nullable = false)
  private boolean isAnswered;

  @Column(name = "is_marked", nullable = false)
  private boolean isMarked;

  @Column(name = "is_uncertain", nullable = false)
  private boolean isUncertain;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Builder
  public WorkbookUserAnswer(WorkbookAttempt workbookAttempt, WorkbookItem workbookItem, Long problemChoiceId,
      String answerContentJson, boolean isAnswered, boolean isMarked, boolean isUncertain) {
    this.workbookAttempt = workbookAttempt;
    this.workbookItem = workbookItem;
    this.problemChoiceId = problemChoiceId;
    this.answerContentJson = answerContentJson;
    this.isAnswered = isAnswered;
    this.isMarked = isMarked;
    this.isUncertain = isUncertain;
  }

  public static WorkbookUserAnswer createEmpty(WorkbookAttempt workbookAttempt, WorkbookItem workbookItem) {
    WorkbookUserAnswer answer = new WorkbookUserAnswer();
    answer.workbookAttempt = workbookAttempt;
    answer.workbookItem = workbookItem;
    answer.isAnswered = false;
    answer.isMarked = false;
    answer.isUncertain = false;
    return answer;
  }
}