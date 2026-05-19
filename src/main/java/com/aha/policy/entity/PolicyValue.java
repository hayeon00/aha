package com.aha.policy.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "policy_value",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_policy_id_value_key", columnNames = {"policy_id", "value_key"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PolicyValue {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "policy_id", nullable = false, foreignKey = @ForeignKey(name = "fk_policy_value_policy_id"))
  private Policy policy;

  @Column(name = "value_key", nullable = false, length = 50)
  private String valueKey;

  @Column(name = "value_content", nullable = false, columnDefinition = "TEXT")
  private String valueContent;

  @Column(name = "value_type", nullable = false, length = 30)
  private String valueType;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Builder
  public PolicyValue(Policy policy, String valueKey, String valueContent, String valueType) {
    this.policy = policy;
    this.valueKey = valueKey;
    this.valueContent = valueContent;
    this.valueType = valueType;
    if (policy != null) {
      policy.getPolicyValues().add(this);
    }
  }
}