package com.aha.domain.workbook.entity;

import com.aha.domain.exam.entity.Exam;
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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "exam_workbook_type",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_exam_workbook_type_exam_id_workbook_type_id",
            columnNames = {"exam_id", "workbook_type_id"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExamWorkbookType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "exam_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_exam_workbook_type_id")
    )
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "workbook_type_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_exam_workbook_type_workbook_type_id")
    )
    private WorkbookType workbookType;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public ExamWorkbookType(Exam exam, WorkbookType workbookType, Boolean isActive) {
        this.exam=exam;
        this.workbookType = workbookType;
        this.isActive = (isActive != null) ? isActive : true;
    }

}