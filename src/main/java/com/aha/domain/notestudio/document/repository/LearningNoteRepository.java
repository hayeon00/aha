package com.aha.domain.notestudio.document.repository;

import com.aha.domain.notestudio.document.entity.LearningNote;
import com.aha.domain.notestudio.document.enums.LearningNoteStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LearningNoteRepository extends JpaRepository<LearningNote, Long> {

    Optional<LearningNote> findByProcessingGroup_Id(Long processingGroupId);

    boolean existsByProcessingGroup_IdAndStatus(
            Long processingGroupId, LearningNoteStatus status);

    @EntityGraph(attributePaths = {
            "processingGroup",
            "processingGroup.userExam",
            "processingGroup.userExam.examVersion",
            "processingGroup.userExam.examVersion.exam"
    })
    List<LearningNote> findAllByProcessingGroup_UserExam_User_IdAndStatusOrderByCompletedAtDesc(
            Long userId, LearningNoteStatus status);
}
