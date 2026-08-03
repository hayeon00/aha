package com.aha.domain.study.repository;

import com.aha.domain.study.entity.ActiveStudyRoomParticipation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActiveStudyRoomParticipationRepository extends JpaRepository<ActiveStudyRoomParticipation,Long> {

    boolean existsByUserId(Long userId);
}
