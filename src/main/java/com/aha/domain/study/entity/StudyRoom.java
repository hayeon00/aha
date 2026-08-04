package com.aha.domain.study.entity;

import com.aha.domain.pastpaper.entity.PastPaper;
import com.aha.domain.study.enums.StudyRoomStatus;
import com.aha.domain.user.entity.User;
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
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "study_room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
public class StudyRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
        name = "past_paper_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_study_room_past_paper_id")
    )
    private PastPaper pastPaper;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
        name = "created_by",
        nullable = false,
        updatable = false,
        foreignKey = @ForeignKey(name = "fk_study_room_created_by")
    )
    private User createdBy;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "time_limit", nullable = false)
    private int timeLimit;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StudyRoomStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "studyRoom")
    private List<StudyRoomMember> members = new ArrayList<>();

    public static StudyRoom create(PastPaper paper, User createdBy, String title,
        String description, int capacity, int timeLimit) {

        return StudyRoom.builder()
            .pastPaper(paper)
            .createdBy(createdBy)
            .title(title)
            .description(description)
            .capacity(capacity)
            .timeLimit(timeLimit)
            .status(StudyRoomStatus.WAITING)
            .build();
    }

    public void validateNotCanceled() {

        if(status==StudyRoomStatus.CANCELED){
            throw new BusinessException(ErrorCode.STUDY_ROOM_ALREADY_CANCELED);
        }
    }
}
