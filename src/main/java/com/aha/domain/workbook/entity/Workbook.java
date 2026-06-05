package com.aha.domain.workbook.entity;

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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "workbook",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_workbook_exam_version_type_id_no",
            columnNames = {"exam_workbook_type_id", "no"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Workbook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "exam_workbook_type_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_workbook_exam_workbook_type_id")
    )
    private ExamWorkbookType examWorkbookType;

    @Column(nullable = false)
    private Integer no;

    @Column(name = "exam_year")
    private Integer examYear;

    @Column(name = "total_question_count")
    private Integer totalQuestionCount;

    @Column(name = "time_limit")
    private Integer timeLimit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WorkbookStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "workbook", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WorkbookItem> workbookItems = new ArrayList<>();

    @Builder
    public Workbook(ExamWorkbookType examWorkbookType, Integer no, Integer examYear,
        Integer totalQuestionCount, Integer timeLimit, WorkbookStatus status) {
        this.examWorkbookType = examWorkbookType;
        this.no = no;
        this.examYear = examYear;
        this.totalQuestionCount = totalQuestionCount;
        this.timeLimit = timeLimit;
        this.status = status;
    }

}
