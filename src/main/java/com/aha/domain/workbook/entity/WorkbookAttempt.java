package com.aha.domain.workbook.entity;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "workbook_attempt",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_workbook_attempt_user_id_active_workbook_id",
            columnNames = {"user_id", "active_workbook_id"}
        )
        ,@UniqueConstraint(
        name = "uk_workbook_attempt_workbook_result_id",
        columnNames = {"workbook_result_id"}
    )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkbookAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "workbook_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_workbook_attempt_workbook_id")
    )
    private Workbook workbook;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "workbook_result_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_workbook_attempt_workbook_result_id")
    )
    private WorkbookResult workbookResult;

    @Column(name = "active_workbook_id")
    private Long activeWorkbookId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AttemptStatus status;

    @Column(name = "due_at", nullable = false)
    private LocalDateTime dueAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    @OneToMany(mappedBy = "workbookAttempt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WorkbookUserAnswer> answers = new ArrayList<>();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public WorkbookAttempt(Long userId, Workbook workbook, Long activeWorkbookId, AttemptStatus status,
        LocalDateTime dueAt, LocalDateTime submittedAt) {
        this.userId = userId;
        this.workbook = workbook;
        this.activeWorkbookId = activeWorkbookId;
        this.status = status;
        this.dueAt = dueAt;
        this.submittedAt = submittedAt;
    }

    public static WorkbookAttempt startAttempt(Long userId, Workbook workbook, int limitSeconds) {

        return WorkbookAttempt.builder()
            .userId(userId)
            .workbook(workbook)
            .activeWorkbookId(workbook.getId())
            .status(AttemptStatus.IN_PROGRESS)
            .dueAt(LocalDateTime.now().plusSeconds(limitSeconds))
            .build();
    }
}