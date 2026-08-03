package com.aha.domain.study.entity;

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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "active_study_room_participation",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_active_study_room_participation_user_id",
        columnNames = "user_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
public class ActiveStudyRoomParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "study_room_id",
        updatable = false,
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_active_study_room_participation_study_room_id")
    )
    private StudyRoom studyRoom;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ActiveStudyRoomParticipation create(Long userId, StudyRoom studyRoom) {

        return ActiveStudyRoomParticipation.builder()
            .userId(userId)
            .studyRoom(studyRoom)
            .build();

    }
}
