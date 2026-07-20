package com.aha.domain.exam.entity;

import com.aha.domain.exam.enums.ExamPartCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
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
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ExamVersion examVersion;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private ExamPartCode code;

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

  @OneToMany(fetch = FetchType.LAZY,mappedBy = "examPart")
  private List<ExamScopeNode> examScopeNodes = new ArrayList<>();
}
