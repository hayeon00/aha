package com.aha.domain.problemsetgenerationjob.entity;

import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.exam.entity.ExamVersion;
import com.aha.domain.problem.entity.DomainType;
import com.aha.domain.user.entity.User;
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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "problem_set_generation_job")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProblemSetGenerationJob {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_psgj_user_id"))
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exam_version_id", nullable = false, foreignKey = @ForeignKey(name = "fk_psgj_exam_version_id"))
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ExamVersion examVersion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exam_scope_node_id", foreignKey = @ForeignKey(name = "fk_psgj_exam_scope_node_id"))
  @OnDelete(action = OnDeleteAction.SET_NULL)
  private ExamScopeNode examScopeNode;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "domain_type_id", foreignKey = @ForeignKey(name = "fk_psgj_domain_type_id"))
  @OnDelete(action = OnDeleteAction.SET_NULL)
  private DomainType domainType;

  @Column(name = "workbook_type_id")
  private Long workbookTypeId;

  @Column(name = "workbook_id")
  private Long workbookId;

  @Column(name = "retry_count", nullable = false)
  private Integer retryCount;

  @Column(name = "requested_count", nullable = false)
  private Integer requestedCount;

  @Column(nullable = false, length = 30)
  private String status;

  @Column(name = "current_step", length = 50)
  private String currentStep;

  @Column(name = "failure_code", length = 100)
  private String failureCode;

  @Column(name = "started_at")
  private LocalDateTime startedAt;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Builder
  public ProblemSetGenerationJob(User user, ExamVersion examVersion, ExamScopeNode examScopeNode,
      DomainType domainType, Long workbookTypeId, Long workbookId,
      Integer retryCount, Integer requestedCount, String status, String currentStep) {
    this.user = user;
    this.examVersion = examVersion;
    this.examScopeNode = examScopeNode;
    this.domainType = domainType;
    this.workbookTypeId = workbookTypeId;
    this.workbookId = workbookId;
    this.requestedCount = requestedCount;
    this.currentStep = currentStep;
    this.retryCount = (retryCount != null) ? retryCount : 0;
    this.status = (status != null) ? status : "PENDING";
  }

  public void startJob(String initialStep) {
    this.status = "PROCESSING";
    this.currentStep = initialStep;
    this.startedAt = LocalDateTime.now();
  }

  public void updateProgress(String step) {
    this.currentStep = step;
  }

  public void completeJob(Long workbookId) {
    this.status = "SUCCESS";
    this.currentStep = "COMPLETED";
    this.workbookId = workbookId;
    this.completedAt = LocalDateTime.now();
  }

  public void failJob(String failureCode, String lastStep) {
    this.status = "FAIL";
    this.failureCode = failureCode;
    this.currentStep = lastStep;
    this.completedAt = LocalDateTime.now();
  }

  public void increaseRetryCount() {
    this.retryCount += 1;
  }
}
