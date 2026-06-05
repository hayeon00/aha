package com.aha.domain.workbook.entity;

import com.aha.domain.problem.entity.Problem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.ForeignKey;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "workbook_item",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_workbook_item_workbook_id_problem_id", columnNames = {"workbook_id", "problem_id"}),
        @UniqueConstraint(name = "uk_workbook_item_workbook_id_item_no", columnNames = {"workbook_id", "item_no"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkbookItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "workbook_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_workbook_item_workbook_id")
  )
  private Workbook workbook;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "problem_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_workbook_item_problem_id" )
  )
  private Problem problem;

  @Column(name = "item_no", nullable = false)
  private Integer itemNo;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Builder
  public WorkbookItem(Workbook workbook, Problem problem, Integer itemNo) {
    this.workbook = workbook;
    this.problem = problem;
    this.itemNo = itemNo;
  }
}