package com.aha.domain.userexam.entity;

import com.aha.domain.exam.entity.ExamVersion;
import com.aha.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "user_exam",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_exam_user_exam_version",
                        columnNames = {"user_id", "exam_version_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserExam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_version_id", nullable = false)
    private ExamVersion examVersion;

    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden;

    @Column(name = "last_studied_at")
    private LocalDateTime lastStudiedAt;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;

    @Builder
    private UserExam(
            User user,
            ExamVersion examVersion,
            Boolean isHidden,
            LocalDateTime lastStudiedAt
    ) {
        this.user = user;
        this.examVersion = examVersion;
        this.isHidden = isHidden != null ? isHidden : false;
        this.lastStudiedAt = lastStudiedAt;
    }

    public void hide() {
        this.isHidden = true;
    }

    public void show() {
        this.isHidden = false;
    }

    public void updateHidden(Boolean hidden) {
        this.isHidden = hidden;
    }

    public void updateLastStudiedAt() {
        this.lastStudiedAt = LocalDateTime.now();
    }
}
