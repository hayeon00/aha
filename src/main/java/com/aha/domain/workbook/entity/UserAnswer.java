package com.aha.domain.workbook.entity;

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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.UpdateTimestamp;

@Table(
    name = "user_answer",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_user_answer_attempt_problem",
        columnNames = {"workbook_attempt_id", "problem_id"}
    )
)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@DynamicInsert
public class UserAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workbook_attempt_id", nullable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_user_answer_workbook_attempt_id"))
    private WorkbookAttempt workbookAttempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_user_answer_problem_id"))
    private Problem problem;

    @Column(name = "answer", length = 500)
    private String answer;

    @Column(name = "is_correct")
    private boolean isCorrect;

    @Column(name = "is_checked")
    private boolean isChecked;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    public static UserAnswer create(WorkbookAttempt workbookAttempt, Problem problem,
        String userAnswer, boolean isChecked) {
        return UserAnswer.builder()
            .workbookAttempt(workbookAttempt)
            .problem(problem)
            .answer(userAnswer != null ? userAnswer.strip() : null)
            .isChecked(isChecked)
            .build();
    }

    public void update(String userAnswer) {
        answer = userAnswer.strip();
    }

    public void update() {
        isChecked = !isChecked;
    }

}
