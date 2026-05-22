package com.aha.domain.ailearn.session.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "learning_problem_attempt")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LearningProblemAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "learning_session_id")
    private Long learningSessionId;

    @Column(name = "exam_scope_node_id", nullable = false)
    private Long examScopeNodeId;

    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    @Column(name = "selected_choice_id")
    private Long selectedChoiceId;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    public LearningProblemAttempt(Long userId, Long learningSessionId, Long examScopeNodeId,
                                  Long problemId, Long selectedChoiceId, Boolean isCorrect) {
        this.userId = userId;
        this.learningSessionId = learningSessionId;
        this.examScopeNodeId = examScopeNodeId;
        this.problemId = problemId;
        this.selectedChoiceId = selectedChoiceId;
        this.isCorrect = isCorrect;
        this.submittedAt = LocalDateTime.now();
    }
}