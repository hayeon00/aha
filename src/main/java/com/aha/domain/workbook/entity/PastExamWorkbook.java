package com.aha.domain.workbook.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.UpdateTimestamp;

@Table(name = "past_exam_workbook",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_past_year_no",
        columnNames = {"year", "round_no"}
    )
)
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@DynamicInsert
public class PastExamWorkbook {

    @Id
    @Column(name = "workbook_id", nullable = false)
    private Long workbookId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true, optional = false)
    @JoinColumn(name = "workbook_id", foreignKey = @ForeignKey(name = "fk_past_exam_workbook_workbook_id"), nullable = false)
    private Workbook workbook;

    @Column(name = "is_reviewed", nullable = false)
    @ColumnDefault("0")
    private Boolean isReviewed;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "round_no", nullable = false)
    private Integer roundNo;

    @Column(name = "time_limit", nullable = false)
    private Integer timeLimit;

    @Column(name = "exam_date", nullable = false)
    private LocalDateTime examDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
