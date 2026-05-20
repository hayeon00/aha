package com.aha.domain.problemsetgenerationjob.entity;

import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.exam.entity.ExamVersion;
import com.aha.domain.problem.entity.DomainType;
import com.aha.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "problem_set_generation_job")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자 통제
public class ProblemSetGenerationJob {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_psgj_user_id"))
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exam_version_id", nullable = false, foreignKey = @ForeignKey(name = "fk_psgj_exam_version_id"))
  private ExamVersion examVersion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exam_scope_node_id", foreignKey = @ForeignKey(name = "fk_psgj_exam_scope_node_id"))
  private ExamScopeNode examScopeNode;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "domain_type_id", foreignKey = @ForeignKey(name = "fk_psgj_domain_type_id"))
  private DomainType domainType;

  // 현재 스키마 상 FK 제약조건이 없으므로 기본 Long 타입으로 매핑합니다.
  @Column(name = "workbook_type_id")
  private Long workbookTypeId;

  @Column(name = "workbook_id")
  private Long workbookId;

  @Column(name = "retry_count", nullable = false)
  private Integer retryCount;

  @Column(name = "requested_count", nullable = false)
  private Integer requestedCount;

  @Column(nullable = false, length = 30)
  private String status; // PENDING, PROCESSING, SUCCESS, FAIL 등

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

    // 자바 레벨 초기값(Default) 방어 설계
    this.retryCount = (retryCount != null) ? retryCount : 0;
    this.status = (status != null) ? status : "PENDING";
  }

  // 💡 비동기 상태 변화를 제어할 도메인 비즈니스 메서드 (핵심 수정자)
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