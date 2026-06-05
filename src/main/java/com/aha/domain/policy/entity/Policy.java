package com.aha.domain.policy.entity;

import com.aha.domain.exam.entity.ExamVersion;
import com.aha.domain.problem.entity.DomainType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "policy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Policy {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exam_version_id", nullable = false, foreignKey = @ForeignKey(name = "fk_policy_exam_version_id"))
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ExamVersion examVersion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "domain_type_id", foreignKey = @ForeignKey(name = "fk_policy_domain_type_id"))
  @OnDelete(action = OnDeleteAction.CASCADE)
  private DomainType domainType;

  @Column(name = "version_no", nullable = false, length = 50)
  private String versionNo;

  @Column(name = "is_active", nullable = false)
  private boolean isActive;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PolicyValue> policyValues = new ArrayList<>();

  @Builder
  public Policy(ExamVersion examVersion, DomainType domainType, String versionNo, boolean isActive) {
    this.examVersion = examVersion;
    this.domainType = domainType;
    this.versionNo = versionNo;
    this.isActive = isActive;
  }
}
