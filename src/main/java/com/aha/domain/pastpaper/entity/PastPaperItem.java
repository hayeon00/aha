package com.aha.domain.pastpaper.entity;

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
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Table(
    name = "past_paper_item",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_past_paper_item_past_paper_problem",
            columnNames = {"past_paper_id", "problem_id"}
        ),
        @UniqueConstraint(
            name = "uk_past_paper_item_past_paper_sort_order",
            columnNames = {"past_paper_id", "sort_order"}
        )
    }
)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PastPaperItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "past_paper_id", nullable = false, updatable = false, foreignKey = @ForeignKey(name="fk_past_paper_item_past_paper_id"))
    private PastPaper pastPaper;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "problem_id", updatable = false, nullable = false, foreignKey = @ForeignKey(name = "fk_past_paper_item_problem_id"))
    private Problem problem;

    @Positive
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
