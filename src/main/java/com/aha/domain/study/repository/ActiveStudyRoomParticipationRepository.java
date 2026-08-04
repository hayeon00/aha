package com.aha.domain.study.repository;

import com.aha.domain.study.entity.ActiveStudyRoomParticipation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActiveStudyRoomParticipationRepository extends JpaRepository<ActiveStudyRoomParticipation,Long> {

    boolean existsByUserId(Long userId);

    Optional<ActiveStudyRoomParticipation> findByUserId(Long userId);
}
