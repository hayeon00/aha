package com.aha.domain.study.repository;

import com.aha.domain.study.entity.StudyRoom;
import com.aha.domain.study.enums.StudyRoomStatus;
import com.aha.domain.study.repository.projection.StudyRoomProjection;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyRoomRepository extends JpaRepository<StudyRoom, Long> {


    @Query(
        value= """
          select
            sr as studyRoom,
            host.nickname as hostNickname,
            host.profileImageUrl as hostProfileImageUrl,
            e.name as examName,
            pp.year as pastPaperYear,
            pp.roundNo as pastPaperRoundNo,
            count(member.id) as memberCount
                from StudyRoom sr
                join sr.pastPaper pp
                join pp.examVersion ev
                join ev.exam e
                join sr.members hostMember
                    ON hostMember.role =
                        com.aha.domain.study.enums.StudyRoomMemberRole.HOST
                join hostMember.user host
                left join sr.members member
                where ev.id = :examVersionId
                              and sr.status = :status
                group by
                    sr,
                    host.nickname,
                    host.profileImageUrl,
                    e.name,
                    pp.year,
                    pp.roundNo
        """
        , countQuery = """
        select count(studyRoom.id)
            from StudyRoom studyRoom
            where studyRoom.pastPaper.examVersion.id = :examVersionId
              and studyRoom.status = :status
        """
    )
    Page<StudyRoomProjection> findStudyRooms(
        @Param("examVersionId") long examVersionId,
        @Param("status")StudyRoomStatus status,
        Pageable pageable
    );


    @Query("""
    select sr from StudyRoom sr
        join fetch sr.pastPaper pp
        join fetch pp.examVersion ev
        join fetch ev.exam e
        join fetch sr.members m
        join fetch m.user
          where sr.id = :studyRoomId
             order by case m.role
                 when 'HOST' then 1
                 when 'MEMBER' then 2
                 else 3
             end asc
""")
    Optional<StudyRoom> findStudyRoom(@Param("studyRoomId") Long studyRoomId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select sr from StudyRoom sr
    join fetch sr.members
    where sr.id = :studyRoomId
""")
    Optional<StudyRoom> findByIdForJoin(@Param("studyRoomId") Long studyRoomId);
}
