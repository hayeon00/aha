package com.aha.domain.workbook.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "workbook_result"
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkbookResult {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "is_passed", nullable = false)
  private boolean isPassed;

  @Enumerated(EnumType.STRING)
  @Column(name = "fail_reason_code", length = 100)
  private FailReasonCode failReasonCode;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Builder
  public WorkbookResult(boolean isPassed, FailReasonCode failReasonCode) {
    this.isPassed = isPassed;
    this.failReasonCode = failReasonCode;
  }
}