package com.aha.problem.entity;

import com.aha.policy.entity.Policy;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "problem_review_detail")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProblemReviewDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "problem_review_id", nullable = false, foreignKey = @ForeignKey(name = "fk_prd_problem_review_id"))
  private ProblemReview problemReview;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "policy_id", nullable = false, foreignKey = @ForeignKey(name = "fk_prd_policy_id"))
  private Policy policy;

  @Column(name = "check_result", nullable = false, length = 20)
  private String checkResult;

  @Column(length = 500)
  private String message;

  @Column(name = "checked_at")
  private LocalDateTime checkedAt;

  @Builder
  public ProblemReviewDetail(ProblemReview problemReview, Policy policy, String checkResult, String message) {
    this.problemReview = problemReview;
    this.policy = policy;
    this.checkResult = checkResult;
    this.message = message;
    this.checkedAt = LocalDateTime.now();

    if (problemReview != null) {
      problemReview.getReviewDetails().add(this);
    }
  }
}