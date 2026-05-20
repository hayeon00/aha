package com.aha.domain.policy.entity;

import com.aha.domain.exam.entity.ExamVersion;
import com.aha.domain.problem.entity.DomainType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "policy",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_policy_version_domain_policy_no",
            columnNames = {"exam_version_id", "domain_type_id", "policy_version_no"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Policy {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exam_version_id", nullable = false, foreignKey = @ForeignKey(name = "fk_policy_exam_version_id"))
  private ExamVersion examVersion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "domain_type_id", nullable = false, foreignKey = @ForeignKey(name = "fk_policy_domain_type_id"))
  private DomainType domainType;

  @Column(name = "policy_version_no", nullable = false)
  private Integer policyVersionNo;

  @Column(name = "policy_name", nullable = false, length = 100)
  private String policyName;

  @Column(nullable = false, length = 20)
  private String status;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PolicyValue> policyValues = new ArrayList<>();

  @Builder
  public Policy(ExamVersion examVersion, DomainType domainType, Integer policyVersionNo,
      String policyName, String status) {
    this.examVersion = examVersion;
    this.domainType = domainType;
    this.policyVersionNo = policyVersionNo;
    this.policyName = policyName;
    this.status = (status != null) ? status : "ACTIVE";
  }
}