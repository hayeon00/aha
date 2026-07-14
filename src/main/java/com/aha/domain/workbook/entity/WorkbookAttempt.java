package com.aha.domain.workbook.entity;

import com.aha.domain.workbook.enums.AttemptStatus;
import com.aha.domain.workbook.enums.WorkbookTypeCode;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
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
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Table(
    name = "workbook_attempt"
)
@Entity
@Getter
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
public class WorkbookAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(1)
    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workbook_id", nullable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_workbook_attempt_workbook_id"))
    private Workbook workbook;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @ColumnDefault(("'SOLVING'"))
    private AttemptStatus status;

    @Min(0)
    @Column(name = "total_score")
    private Integer totalScore;

    @Column(name = "is_passed")
    private Boolean isPassed;

    @Column(name = "result_message", length = 255)
    private String resultMessage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> statJson;

    @Min(0)
    @Column(name = "elapsed_time")
    private Integer elapsedTime;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static WorkbookAttempt create(Long userId, Workbook workbook) {
        return WorkbookAttempt.builder()
            .userId(userId)
            .workbook(workbook)
            .status(AttemptStatus.SOLVING)
            .build();
    }

    public void validateSaveUserAnswer(Workbook workbook) {
        if(status!=AttemptStatus.SOLVING){
            throw new BusinessException(ErrorCode.ATTEMPT_ALREADY_GRADED);
        }
        WorkbookTypeCode workbookTypeCode = workbook.getWorkbookType().getCode();
        if(workbookTypeCode==WorkbookTypeCode.PAST){
            LocalDateTime now = LocalDateTime.now();
            PastExamWorkbook pastExamWorkbook = workbook.getPastExamWorkbook();
            LocalDateTime dueAt = createdAt.plusSeconds(pastExamWorkbook.getTimeLimit());
            if(now.isAfter(dueAt)){
                throw new BusinessException(ErrorCode.ATTEMPT_EXCEEDED_TIME_LIMIT);
            }
        }
    }
}
