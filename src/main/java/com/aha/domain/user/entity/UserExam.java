package com.aha.domain.user.entity;

import com.aha.domain.exam.entity.Exam;
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
        name = "user_exam",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_exam_user_exam",
                        columnNames = {"user_id", "exam_id"}
                )
        },
        indexes = {
                @Index(name = "idx_user_exam_user_id", columnList = "user_id"),
                @Index(name = "idx_user_exam_exam_id", columnList = "exam_id"),
                @Index(name = "idx_user_exam_user_main", columnList = "user_id, is_main")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserExam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_exam_user_id")
    )
    private User user;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "exam_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_exam_exam_id")
    )
    private Exam exam;


    @Column(name = "is_main", nullable = false)
    private boolean isMain;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public UserExam(User user, Exam exam, boolean isMain) {
        this.user = user;
        this.exam = exam;
        this.isMain = isMain;
    }

    public void changeMain(boolean main) {
        this.isMain = main;
    }
}