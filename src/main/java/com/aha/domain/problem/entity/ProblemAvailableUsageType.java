package com.aha.domain.problem.entity;

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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
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
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Problem problem;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "domain_type_id", nullable = false, foreignKey = @ForeignKey(name = "fk_paut_domain_type_id"))
  @OnDelete(action = OnDeleteAction.CASCADE)
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
