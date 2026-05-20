package com.aha.domain.problem.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "problem_available_usage_type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProblemAvailableUsageType {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "problem_id", nullable = false, foreignKey = @ForeignKey(name = "fk_paut_problem_id"))
  private Problem problem;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "domain_type_id", nullable = false, foreignKey = @ForeignKey(name = "fk_paut_domain_type_id"))
  private DomainType domainType;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Builder
  public ProblemAvailableUsageType(Problem problem, DomainType domainType) {
    this.problem = problem;
    this.domainType = domainType;
    if (problem != null) {
      problem.getAvailableUsageTypes().add(this);
    }
  }
}