package com.aha.domain.exam.entity;

import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import jakarta.persistence.CascadeType;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "exam_version",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_exam_id_version_no", columnNames = {"exam_id", "version_no"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExamVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false, foreignKey = @ForeignKey(name = "fk_exam_version_exam_id"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Exam exam;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    @Column(name = "version_name", nullable = false, length = 100)
    private String versionName;

    @Column(name = "default_question_count", nullable = false)
    private Integer defaultQuestionCount;

    @Column(name = "duration_type", nullable = false, length = 50)
    private String durationType;

    @Column(name = "default_duration_seconds")
    private Integer defaultDurationSeconds;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore;

    @Column(name = "passing_rule_type", nullable = false, length = 50)
    private String passingRuleType;

    @Column(name = "passing_score", nullable = false)
    private Integer passingScore;

    @Column(name = "has_subject_fail_rule", nullable = false)
    private boolean hasSubjectFailRule;

    @Column(name = "subject_fail_threshold")
    private Integer subjectFailThreshold;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExamVersionStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "examVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExamPart> parts = new ArrayList<>();

    @Builder
    public ExamVersion(Exam exam, Integer versionNo, String versionName,
        Integer defaultQuestionCount,
        String durationType, Integer defaultDurationSeconds, Integer totalScore,
        String passingRuleType, Integer passingScore, boolean hasSubjectFailRule,
        Integer subjectFailThreshold, ExamVersionStatus status) {
        this.exam = exam;
        this.versionNo = versionNo;
        this.versionName = versionName;
        this.defaultQuestionCount = defaultQuestionCount;
        this.durationType = durationType;
        this.defaultDurationSeconds = defaultDurationSeconds;
        this.totalScore = totalScore;
        this.passingRuleType = passingRuleType;
        this.passingScore = passingScore;
        this.hasSubjectFailRule = hasSubjectFailRule;
        this.subjectFailThreshold = subjectFailThreshold;
        this.status = status;
        if (exam != null) {
            exam.getVersions().add(this);
        }
    }

    public void validateActive() {
        if (status != ExamVersionStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.EXAM_VERSION_NOT_ACTIVE);
        }
    }
}
