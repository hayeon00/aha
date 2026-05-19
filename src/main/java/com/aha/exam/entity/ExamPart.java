package com.aha.exam.entity;

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
    name = "exam_part",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_exam_version_id_code", columnNames = {"exam_version_id", "code"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExamPart {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exam_version_id", nullable = false, foreignKey = @ForeignKey(name = "fk_exam_part_exam_version_id"))
  private ExamVersion examVersion;

  @Column(nullable = false, length = 50)
  private String code;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(name = "default_question_count", nullable = false)
  private Integer defaultQuestionCount;

  @Column(name = "default_duration_seconds")
  private Integer defaultDurationSeconds;

  @Column(name = "total_score", nullable = false)
  private Integer totalScore;

  @Column(name = "is_subject_fail_target", nullable = false)
  private boolean isSubjectFailTarget;

  @Column(name = "subject_fail_threshold_score")
  private Integer subjectFailThresholdScore;

  @Column(name = "is_active", nullable = false)
  private boolean isActive;

  @Column(name = "display_order", nullable = false)
  private Integer displayOrder;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Builder
  public ExamPart(ExamVersion examVersion, String code, String name, Integer defaultQuestionCount,
      Integer defaultDurationSeconds, Integer totalScore, boolean isSubjectFailTarget,
      Integer subjectFailThresholdScore, boolean isActive, Integer displayOrder) {
    this.examVersion = examVersion;
    this.code = code;
    this.name = name;
    this.defaultQuestionCount = defaultQuestionCount;
    this.defaultDurationSeconds = defaultDurationSeconds;
    this.totalScore = totalScore;
    this.isSubjectFailTarget = isSubjectFailTarget;
    this.subjectFailThresholdScore = subjectFailThresholdScore;
    this.isActive = isActive;
    this.displayOrder = displayOrder;
    if (examVersion != null) {
      examVersion.getParts().add(this);
    }
  }
}
