package com.aha.domain.workbook.entity;

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
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Table(
    name = "workbook_item",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_workbook_item_pair",
            columnNames = {"workbook_id", "problem_id"}
        ),
        @UniqueConstraint(
            name = "uk_workbook_item_order",
            columnNames = {"workbook_id", "sort_order"}
        )
    }
)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkbookItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "workbook_id", nullable = false, updatable = false)
    private Workbook workbook;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "problem_id", updatable = false, nullable = false, foreignKey = @ForeignKey(name = "fk_workbook_item_problem_id"))
    private Problem problem;

    @Min(1)
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
