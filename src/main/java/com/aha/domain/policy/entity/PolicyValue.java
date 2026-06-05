package com.aha.domain.policy.entity;

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
@Table(name = "policy_value")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PolicyValue {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "policy_id", nullable = false, foreignKey = @ForeignKey(name = "fk_policy_value_policy_id"))
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Policy policy;

  @Column(name = "value_key", nullable = false, length = 100)
  private String valueKey;

  @Column(name = "value_type", nullable = false, length = 30)
  private String valueType;

  @Column(name = "value", nullable = false, columnDefinition = "TEXT")
  private String value;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Builder
  public PolicyValue(Policy policy, String valueKey, String valueType, String value) {
    this.policy = policy;
    this.valueKey = valueKey;
    this.valueType = valueType;
    this.value = value;
    if (policy != null) {
      policy.getPolicyValues().add(this);
    }
  }
}
