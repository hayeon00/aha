package com.aha.domain.ailearn.ai.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "ai_message")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "learning_session_id")
    private Long learningSessionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "exam_scope_node_id")
    private Long examScopeNodeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private AiMessageRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", length = 50)
    private AiQuestionType questionType;

    @Lob
    @Column(name = "message_text", nullable = false)
    private String messageText;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public AiMessage(Long learningSessionId, Long userId, Long examScopeNodeId,
                     AiMessageRole role, AiQuestionType questionType, String messageText) {
        this.learningSessionId = learningSessionId;
        this.userId = userId;
        this.examScopeNodeId = examScopeNodeId;
        this.role = role;
        this.questionType = questionType;
        this.messageText = messageText;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}