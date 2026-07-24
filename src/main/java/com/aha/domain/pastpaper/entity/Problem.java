package com.aha.domain.pastpaper.entity;

import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.pastpaper.enums.ProblemFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Table(name = "problem")
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_scope_node_id", nullable = false, foreignKey = @ForeignKey(name = "fk_problem_exam_scope_node_id"))
    private ExamScopeNode examScopeNode;

    @OneToMany(fetch = FetchType.LAZY,mappedBy = "problem")
    private List<ProblemChoice> problemChoices = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false, length = 30)
    private ProblemFormat format;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @Positive
    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "answer", nullable = false, length = 500)
    private String answer;

    @Column(name = "explanation", nullable = false, columnDefinition = "TEXT")//length 뭘로 잡아야하지
    private String explanation;

    @PositiveOrZero
    @Column(name = "choice_count", nullable = false)
    private Integer choiceCount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void grade(UserAnswer userAnswer) {
        String target = userAnswer.getUserAnswer();
        if(target!=null && target.strip().equals(answer)){
            userAnswer.right();
        }else{
            userAnswer.wrong();
        }
    }
}
