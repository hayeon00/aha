package com.aha.domain.workbook.entity;

import com.aha.domain.exam.entity.Exam;
import com.aha.domain.exam.entity.ExamVersion;
import com.aha.domain.exam.enums.ExamStatus;
import com.aha.domain.workbook.enums.WorkbookStatus;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.UpdateTimestamp;

@Table(name = "workbook")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
public class Workbook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_version_id", nullable = false, foreignKey = @ForeignKey(name = "fk_workbook_exam_version_id"))
    private ExamVersion examVersion;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "workbook")
    private PastExamWorkbook pastExamWorkbook;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "workbook", orphanRemoval = true)
    private List<WorkbookItem> workbookItems = new ArrayList<>();


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workbook_type_id", nullable = false, foreignKey = @ForeignKey(name = "fk_workbook_workbook_type_id"))
    private WorkbookType workbookType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @ColumnDefault("'DRAFT'")
    private WorkbookStatus status;

    @Column(name = "total_problem_count", nullable = false)
    private Integer totalProblemCount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;



    public void validateStartAttempt() {
        if (!isPublished()) {
            throw new BusinessException(ErrorCode.WORKBOOK_NOT_PUBLISHED);
        }
        if (!getExamVersion().isActive()) {
            throw new BusinessException(ErrorCode.EXAM_VERSION_NOT_ACTIVE);
        }
        Exam exam = examVersion.getExam();
        if(exam.getStatus() != ExamStatus.ACTIVE){
            throw new BusinessException(ErrorCode.EXAM_NOT_ACTIVE);
        }
    }
    public void validateGetItems() {
        if (!isPublished()) {
            throw new BusinessException(ErrorCode.WORKBOOK_NOT_PUBLISHED);
        }
        ExamVersion examVersion = getExamVersion();
        if (!examVersion.isActive()) {
            throw new BusinessException(ErrorCode.EXAM_VERSION_NOT_ACTIVE);
        }
        Exam exam = examVersion.getExam();
        if(exam.getStatus() != ExamStatus.ACTIVE){
            throw new BusinessException(ErrorCode.EXAM_NOT_ACTIVE);
        }
        if(!checkItemCount()){
            throw new BusinessException((ErrorCode.WORKBOOK_ITEM_COUNT_MISMATCH));
        }
    }

    public void validateGetUserAnswers() {
        if (!isPublished()) {
            throw new BusinessException(ErrorCode.WORKBOOK_NOT_PUBLISHED);
        }
        if (!getExamVersion().isActive()) {
            throw new BusinessException(ErrorCode.EXAM_VERSION_NOT_ACTIVE);
        }
        Exam exam = examVersion.getExam();
        if(exam.getStatus() != ExamStatus.ACTIVE){
            throw new BusinessException(ErrorCode.EXAM_NOT_ACTIVE);
        }
    }
    public void validateSaveUserAnswers() {
        if (!isPublished()) {
            throw new BusinessException(ErrorCode.WORKBOOK_NOT_PUBLISHED);
        }
        if (!getExamVersion().isActive()) {
            throw new BusinessException(ErrorCode.EXAM_VERSION_NOT_ACTIVE);
        }
        Exam exam = examVersion.getExam();
        if(exam.getStatus() != ExamStatus.ACTIVE){
            throw new BusinessException(ErrorCode.EXAM_NOT_ACTIVE);
        }
    }


    private boolean checkItemCount(){
        return totalProblemCount == workbookItems.size();
    }

    private boolean isPublished() {
        return status == WorkbookStatus.PUBLISHED;
    }

}
