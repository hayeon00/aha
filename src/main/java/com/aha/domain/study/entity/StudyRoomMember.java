package com.aha.domain.study.entity;

import com.aha.domain.pastpaper.entity.PastPaperAttempt;
import com.aha.domain.study.enums.StudyRoomMemberRole;
import com.aha.domain.user.entity.User;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "study_room_member",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_study_room_member_room_user",
            columnNames = {"study_room_id", "user_id"}),
        @UniqueConstraint(
            name = "uk_study_room_member_past_paper_attempt",
            columnNames = "past_paper_attempt_id"
        )
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
public class StudyRoomMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        updatable = false,
        foreignKey = @ForeignKey(name = "fk_study_room_member_user_id")
    )
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
        name = "study_room_id",
        nullable = false,
        updatable = false,
        foreignKey = @ForeignKey(name = "fk_study_room_member_study_room_id")
    )
    private StudyRoom studyRoom;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "past_paper_attempt_id",
        foreignKey = @ForeignKey(name = "fk_study_room_member_past_paper_attempt_id")
    )
    private PastPaperAttempt pastPaperAttempt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 30, nullable = false)
    private StudyRoomMemberRole role;

    @Column(name = "is_ready", nullable = false)
    private boolean isReady;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static StudyRoomMember create(User createdBy, StudyRoom studyRoom) {

        return StudyRoomMember.builder()
            .user(createdBy)
            .studyRoom(studyRoom)
            .pastPaperAttempt(null)
            .role(StudyRoomMemberRole.HOST)
            .isReady(false)
            .build();

    }
}
