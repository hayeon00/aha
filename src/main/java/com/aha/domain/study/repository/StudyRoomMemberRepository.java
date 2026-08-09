package com.aha.domain.study.repository;

import com.aha.domain.study.entity.StudyRoomMember;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyRoomMemberRepository extends JpaRepository<StudyRoomMember,Long> {

    Optional<StudyRoomMember> findByStudyRoom_IdAndUser_Id(Long studyRoomId, Long userId);

    @Query("""
            select sm from StudyRoomMember sm
              join fetch sm.user
              where sm.id = :memberId
                and sm.studyRoom.id = :studyRoomId
        """)
    Optional<StudyRoomMember> findByIdAndStudyRoom_IdWithUser(@Param("memberId") Long memberId,
        @Param("studyRoomId") Long studyRoomId);

    long countByStudyRoom_Id(Long studyRoomId);

    boolean existsByStudyRoom_IdAndUser_Id(Long studyRoomId, Long userId);
}
