package com.aha.domain.problem.entity;

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
import org.hibernate.type.SqlTypes;

@Entity
@Table(
    name = "problem_choice",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_problem_id_choice_no", columnNames = {"problem_id",
            "choice_no"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProblemChoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false, foreignKey = @ForeignKey(name = "fk_problem_choice_problem_id"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Problem problem;

    @Column(name = "choice_no", nullable = false)
    private Integer choiceNo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "choice_content_json", nullable = false, columnDefinition = "JSON")
    private Map<String, Object> choiceContentJson;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public ProblemChoice(Problem problem, Integer choiceNo, Map<String, Object> choiceContentJson) {
        this.problem = problem;
        this.choiceNo = choiceNo;
        this.choiceContentJson = choiceContentJson;
        if (problem != null) {
            problem.getChoices().add(this);
        }
    }
}
