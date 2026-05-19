package com.aha.problem.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "problem_review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProblemReview {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ai_generated_problem_id", nullable = false, foreignKey = @ForeignKey(name = "fk_problem_review_agp_id"))
  private AiGeneratedProblem aiGeneratedProblem;

  @Column(name = "review_status", nullable = false, length = 30)
  private String reviewStatus;

  @Column(name = "fail_reason", length = 500)
  private String failReason;

  @Column(name = "reviewed_at")
  private LocalDateTime reviewedAt;

  @OneToMany(mappedBy = "problemReview", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProblemReviewDetail> reviewDetails = new ArrayList<>();

  @Builder
  public ProblemReview(AiGeneratedProblem aiGeneratedProblem, String reviewStatus, String failReason) {
    this.aiGeneratedProblem = aiGeneratedProblem;
    this.reviewStatus = (reviewStatus != null) ? reviewStatus : "PENDING";
    this.failReason = failReason;
  }

  public void passReview() {
    this.reviewStatus = "PASSED";
    this.reviewedAt = LocalDateTime.now();
  }

  public void failReview(String failReason) {
    this.reviewStatus = "FAILED";
    this.failReason = failReason;
    this.reviewedAt = LocalDateTime.now();
  }
}