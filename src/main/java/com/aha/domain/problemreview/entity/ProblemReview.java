package com.aha.domain.problemreview.entity;

import com.aha.domain.problemsetgenerationjob.entity.AiGeneratedProblem;
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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
  @OnDelete(action = OnDeleteAction.CASCADE)
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
    this.reviewStatus = reviewStatus;
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
