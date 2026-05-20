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
@Table(
    name = "domain_type",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_domain_type_code", columnNames = "code")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DomainType {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 50)
  private String code;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(name = "is_active", nullable = false)
  private boolean isActive;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Builder
  public DomainType(String code, String name, Boolean isActive) {
    this.code = code;
    this.name = name;
    this.isActive = (isActive != null) ? isActive : true;
  }
}