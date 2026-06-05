package com.aha.domain.workbook.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    name = "workbook_type",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_wt_code", columnNames = {"code"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkbookType {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 50)
  private String code;

  @Column(name = "name", nullable = false, length = 50)
  private String name;

  @Column(name = "display_order", nullable = false)
  private Integer displayOrder = 0;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Builder
  public WorkbookType(String code, String name, int displayOrder) {
    this.code = code;
    this.name = name;
    this.displayOrder = displayOrder;
  }
}