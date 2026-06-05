package com.aha.domain.problem.entity;

import com.aha.domain.exam.entity.Exam;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.exam.entity.ExamVersion;
import com.aha.domain.problemsetgenerationjob.entity.AiGeneratedProblem;
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
import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "problem")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false, foreignKey = @ForeignKey(name = "fk_problem_exam_id"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_version_id", nullable = false, foreignKey = @ForeignKey(name = "fk_problem_exam_version_id"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ExamVersion examVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_scope_node_id", foreignKey = @ForeignKey(name = "fk_problem_exam_scope_node_id"))
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private ExamScopeNode examScopeNode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ai_generated_problem_id", foreignKey = @ForeignKey(name = "fk_problem_ai_generated_problem_id"))
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private AiGeneratedProblem aiGeneratedProblem;

    @Column(name = "expression_type", nullable = false, length = 30)
    private String expressionType;

    @Column(nullable = false, length = 20)
    private String difficulty;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "question_content_json", nullable = false, columnDefinition = "JSON")
    private Map<String, Object> questionContentJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "explanation_json", nullable = false, columnDefinition = "JSON")
    private Map<String, Object> explanationJson;

    @Column(name = "answer_type", nullable = false, length = 30)
    private String answerType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "answer_json", nullable = false, columnDefinition = "JSON")
    private Map<String, Object> answerJson;

    @Column(name = "choice_type", nullable = false, length = 30)
    private String choiceType;

    @Column(name = "source_type", nullable = false, length = 30)
    private String sourceType;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProblemChoice> choices = new ArrayList<>();

    @Builder
    public Problem(Exam exam, ExamVersion examVersion, ExamScopeNode examScopeNode,
        AiGeneratedProblem aiGeneratedProblem, String expressionType, String difficulty,
        Map<String, Object> questionContentJson, Map<String, Object> explanationJson,
        String answerType, Map<String, Object> answerJson,
        String choiceType, String sourceType, Boolean isActive) {
        this.exam = exam;
        this.examVersion = examVersion;
        this.examScopeNode = examScopeNode;
        this.aiGeneratedProblem = aiGeneratedProblem;
        this.expressionType = expressionType;
        this.difficulty = difficulty;
        this.questionContentJson = questionContentJson;
        this.explanationJson = explanationJson;
        this.answerType = answerType;
        this.answerJson = answerJson;
        this.choiceType = choiceType;
        this.sourceType = sourceType;
        this.isActive = (isActive != null) ? isActive : true;
    }
}
