package com.aha.domain.study.repository;

import com.aha.domain.study.entity.ActiveStudyRoomParticipation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActiveStudyRoomParticipationRepository extends JpaRepository<ActiveStudyRoomParticipation,Long> {

    boolean existsByUserId(Long userId);

    @Query("""
    select asrp from ActiveStudyRoomParticipation asrp
      join fetch asrp.studyRoom
      where asrp.userId = :userId
""")
    Optional<ActiveStudyRoomParticipation> findByUserIdWithStudyRoom(@Param("userId") Long userId);

    Optional<ActiveStudyRoomParticipation> findByStudyRoom_IdAndUserId(Long studyRoomId, Long userId);
}
