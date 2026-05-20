package com.aha.domain.problemreview.entity;

import com.aha.domain.policy.entity.Policy;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ProblemReview problemReview;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "policy_id", nullable = false, foreignKey = @ForeignKey(name = "fk_prd_policy_id"))
  @OnDelete(action = OnDeleteAction.CASCADE)
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
