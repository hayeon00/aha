package com.aha.domain.exam.entity;

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
    name = "exam",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_exam_code", columnNames = "code")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Exam {

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

  @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ExamVersion> versions = new ArrayList<>();

  @Builder
  public Exam(String code, String name, boolean isActive) {
    this.code = code;
    this.name = name;
    this.isActive = isActive;
  }

  public void updateInfo(String name, boolean isActive) {
    this.name = name;
    this.isActive = isActive;
  }
}